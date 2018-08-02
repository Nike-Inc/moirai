package com.nike.moirai.config;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Returns the boolean value provided in the configuration. Returns false if no configuration is provided.
 *
 * @param <T> the type of config
 */
public abstract class FeatureEnabledConfigDecider<T> implements Predicate<ConfigDecisionInput<T>> {
    @Override
    public boolean test(ConfigDecisionInput<T> configDecisionInput) {
        return featureEnabled(configDecisionInput.getConfig(), configDecisionInput.getFeatureIdentifier()).orElse(false);
    }

    /**
     * Provide the boolean value on whether the feature should be enabled.
     * Returning Optional.empty() is equivalent to false.
     *
     * @param config the config source
     * @param featureIdentifier the feature
     * @return true, false, or {@link Optional#empty()}
     */
    protected abstract Optional<Boolean> featureEnabled(T config, String featureIdentifier);
}
