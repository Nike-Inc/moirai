package com.nike.moirai.resource.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Given a supplier of some object, transparently reloads and replaces that object in the background.
 *
 * Uses a {@link ScheduledExecutorService} to periodically reload the resource. Uses a timeout for the calls for loading the resource.
 * Schedules the next reload after getting the success, failure, or timeout result for loading a resource, so there should not be overlapping resource requests
 * happening at the same time.
 *
 * @param <R> the type of object returned by the resource loader to provide and reload
 */
public class ResourceReloader<R> {
    private static final class ResourceReloaderThreadFactory implements ThreadFactory {
        private final ThreadFactory delegate = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = delegate.newThread(r);
            thread.setDaemon(true);
            thread.setName("ReloadableResourceMoiraiThread-" + thread.getName());

            return thread;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceReloader.class);

    /**
     * Given an asynchronous supplier of some resource and an initial value, will reload the resource once per minute. Each resource load attempt should
     * complete in less than 30 seconds, otherwise a new attempt to load the resource will be scheduled. The reloading schedule will be started immediately, and
     * a shutdown-hook is added to stop the executor service.
     *
     * @param resourceLoader supplier that will be called for each attempt to load the resource
     * @param initialValue an initial value to use before the first successful reload of the resource
     * @param <R> the type of resource
     * @return a resource reloader
     */
    public static <R> ResourceReloader<R> withDefaultSettings(
        Supplier<CompletableFuture<R>> resourceLoader,
        R initialValue) {

        return new ResourceReloader<>(
            resourceLoader,
            initialValue,
            new ResourceReloadSettings(Duration.of(1, MINUTES), Duration.of(30, SECONDS)),
            true);
    }

    /**
     * Given an asynchronous supplier of some resource and an initial value, will reload the resource based on the given ResourceReloadSettings.
     * Each resource load attempt should complete in the provided timeout specified by the ResourceReloadSettings, otherwise a new attempt to load the resource
     * will be scheduled. The reloading schedule will be started immediately, and a shutdown-hook is added to stop the executor service.
     *
     * @param resourceLoader supplier that will be called for each attempt to load the resource
     * @param initialValue an initial value to use before the first successful reload of the resource
     * @param resourceReloadSettings custom settings for how to reload the resource
     * @param <R> the type of resource
     * @return a resource reloader
     */
    public static <R> ResourceReloader<R> withCustomSettings(
        Supplier<CompletableFuture<R>> resourceLoader,
        R initialValue,
        ResourceReloadSettings resourceReloadSettings) {

        return new ResourceReloader<>(
            resourceLoader,
            initialValue,
            resourceReloadSettings,
            true);
    }

    /**
     * Given an asynchronous supplier of some resource and an initial value, will reload the resource based on the given ResourceReloadSettings.
     * Each resource load attempt should complete in the provided timeout specified by the ResourceReloadSettings, otherwise a new attempt to load the resource
     * will be scheduled. The reloading process will be started when {@link #init()} is called, and stopped when {@link #shutdown()} is called.
     *
     * @param resourceLoader supplier that will be called for each attempt to load the resource
     * @param initialValue an initial value to use before the first successful reload of the resource
     * @param resourceReloadSettings custom settings for how to reload the resource
     * @param <R> the type of resource
     * @return a resource reloader
     */
    public static <R> ResourceReloader<R> withCustomSettingsAndManualLifecycle(
        Supplier<CompletableFuture<R>> resourceLoader,
        R initialValue,
        ResourceReloadSettings resourceReloadSettings) {

        return new ResourceReloader<>(
            resourceLoader,
            initialValue,
            resourceReloadSettings,
            false);
    }

    private final Supplier<CompletableFuture<R>> resourceLoader;

    private final AtomicReference<R> valueReference;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, new ResourceReloaderThreadFactory());
    private final ResourceReloadSettings resourceReloadSettings;

    private ResourceReloader(
        Supplier<CompletableFuture<R>> resourceLoader,
        R initialValue,
        ResourceReloadSettings resourceReloadSettings,
        boolean managedLifecycle) {

        this.resourceLoader = resourceLoader;
        this.valueReference = new AtomicReference<R>(initialValue);
        this.resourceReloadSettings = resourceReloadSettings;

        if (managedLifecycle) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            init();
        }
    }

    private Runnable reload() {
        return () -> {
            R originalValue = this.valueReference.get();

            CompletableFuture<R> resource = this.resourceLoader.get();

            try {
                R value = resource.get(this.resourceReloadSettings.getResourceLoadTimeout().toMillis(), MILLISECONDS);

                // Only update if the original value has not changed, otherwise a cancellation or timeout may have occurred
                boolean updated = this.valueReference.compareAndSet(originalValue, value);
                if (updated) {
                    scheduleReload();
                }
            } catch (ExecutionException | TimeoutException e) {
                LOGGER.error("Error loading resource", e);
                scheduleReload();
            } catch (InterruptedException e) {
                // Interrupt thread and do not schedule a new reload
                Thread.currentThread().interrupt();
            }
        };
    }

    private void scheduleReload() {
        this.scheduledExecutorService.schedule(reload(), this.resourceReloadSettings.getReloadFrequency().toMillis(), MILLISECONDS);
    }

    /**
     * @return the current value for the resource
     */
    public R getValue() {
        return this.valueReference.get();
    }

    /**
     * Starts schedule the reload
     */
    public void init() {
        scheduleReload();
    }

    /**
     * Shuts down the executor service
     */
    public void shutdown() {
        this.scheduledExecutorService.shutdown();
    }
}
