package com.nike.moirai;

import com.nike.moirai.config.ConfigDecisionInput;
import com.nike.moirai.resource.reload.ResourceReloader;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Checks if a feature is enabled using a given supplier of configuration and a predicate for config decisions for that configuration type.
 *
 * @param <C> the type of config
 */
public class ConfigFeatureFlagChecker<C> implements FeatureFlagChecker {
    private final Supplier<C> configSupplier;
    private final Predicate<ConfigDecisionInput<C>> configDecider;

    /**
     * Given a resource reloader, gets the current config value and calls the configDecider using that config value.
     *
     * @param resourceReloader a reloader for the config
     * @param configDecider a predicate for input using the config
     * @param <C>  the type of config
     * @return a checker for the config from the resource reloader and the provided predicate
     */
    public static <C> ConfigFeatureFlagChecker<C> forReloadableResource(ResourceReloader<C> resourceReloader, Predicate<ConfigDecisionInput<C>> configDecider) {
        return new ConfigFeatureFlagChecker<>(resourceReloader::getValue, configDecider);
    }

    /**
     * Given a supplier of a config, calls the supplier for each decision and then calls the configDecider using that config value.
     *
     * @param configSupplier a supplier of config
     * @param configDecider a predicate for input using the config
     * @param <C> the type of config
     * @return a checker for the config from the supplier and the provided predicate
     */
    public static <C> ConfigFeatureFlagChecker<C> forConfigSupplier(Supplier<C> configSupplier, Predicate<ConfigDecisionInput<C>> configDecider) {
        return new ConfigFeatureFlagChecker<>(configSupplier, configDecider);
    }

    private ConfigFeatureFlagChecker(
        Supplier<C> configSupplier,
        Predicate<ConfigDecisionInput<C>> configDecider) {

        this.configSupplier = configSupplier;
        this.configDecider = configDecider;
    }

    @Override
    public boolean isFeatureEnabled(String featureIdentifier, FeatureCheckInput featureCheckInput) {
        return this.configDecider.test(new ConfigDecisionInput<>(this.configSupplier.get(), featureIdentifier, featureCheckInput));
    }
}
