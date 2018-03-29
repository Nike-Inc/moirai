package com.nike.moirai.config;

import com.nike.moirai.FeatureCheckInput;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Returns true for a configured list of users.
 *
 * @param <C> the type of config
 * @param <V> the type of the
 */
public abstract class EnabledValuesConfigDecider<C, V> implements Predicate<ConfigDecisionInput<C>> {
    @Override
    public boolean test(ConfigDecisionInput<C> configDecisionInput) {
        return checkValue(configDecisionInput.getFeatureCheckInput(), value ->
            enabledValues(configDecisionInput.getConfig(), configDecisionInput.getFeatureIdentifier()).contains(value)
        );
    }

    /**
     * Provide the collection of users that should have the given feature enabled. Return an empty list if no configuration is provided for the feature.
     *
     * @param config the config source
     * @param featureIdentifier the feature
     * @return the collection of userIds that should be enabled for the feature
     */
    protected abstract Collection<V> enabledValues(C config, String featureIdentifier);

    protected abstract boolean checkValue(FeatureCheckInput featureCheckInput, Predicate<V> check);
}
