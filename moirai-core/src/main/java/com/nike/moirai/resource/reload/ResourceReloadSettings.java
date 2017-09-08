package com.nike.moirai.resource.reload;

import java.time.Duration;
import java.util.Objects;

/**
 * Settings for reloading a resource.
 */
public class ResourceReloadSettings {

    private final Duration reloadFrequency;
    private final Duration resourceLoadTimeout;

    /**
     * @param reloadFrequency the duration between calls to load the resource
     * @param resourceLoadTimeout the duration to wait for loading the resource to return
     */
    public ResourceReloadSettings(Duration reloadFrequency, Duration resourceLoadTimeout) {
        this.reloadFrequency = reloadFrequency;
        this.resourceLoadTimeout = resourceLoadTimeout;
    }

    /**
     * @return the duration to wait between requests to load the resource
     */
    public Duration getReloadFrequency() {
        return reloadFrequency;
    }

    /**
     * @return the duration to wait for a resource load attempt to finish
     */
    public Duration getResourceLoadTimeout() {
        return resourceLoadTimeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResourceReloadSettings that = (ResourceReloadSettings) o;
        return Objects.equals(reloadFrequency, that.reloadFrequency) &&
            Objects.equals(resourceLoadTimeout, that.resourceLoadTimeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reloadFrequency, resourceLoadTimeout);
    }

    @Override
    public String toString() {
        return "ResourceReloadSettings{" +
            "reloadFrequency=" + reloadFrequency +
            ", resourceLoadTimeout=" + resourceLoadTimeout +
            '}';
    }
}
