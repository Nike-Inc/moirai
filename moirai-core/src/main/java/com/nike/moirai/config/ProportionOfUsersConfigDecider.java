package com.nike.moirai.config;

import java.util.Optional;
import java.util.function.Predicate;

import static com.nike.moirai.config.ConfigDeciders.userIdCheck;

/**
 * Returns true for a configured proportion of users. The proportion is based on the hashCode() of the userId concatenated with the featureIdentifier,
 * so a consistent answer will be returned for each userId for a feature. Returns false if no proportion configuration is provided for the feature identifier.
 *
 * @param <T> the type of config
 */
public abstract class ProportionOfUsersConfigDecider<T> implements Predicate<ConfigDecisionInput<T>> {
    @Override
    public boolean test(ConfigDecisionInput<T> configDecisionInput) {
        return userIdCheck(configDecisionInput.getFeatureCheckInput(), userId ->
            enabledProportion(configDecisionInput.getConfig(), configDecisionInput.getFeatureIdentifier()).map(enabledProportion ->
                userHashEnabled(userId, configDecisionInput.getFeatureIdentifier(), enabledProportion)
            ).orElse(false)
        );
    }

    private boolean userHashEnabled(String userId, String featureIdentifier, double proportion) {
        return (Math.abs((userId + featureIdentifier).hashCode()) % 100) / 100.0 < proportion;
    }

    /**
     * Provide the proportion of users that should be enabled for the given feature. The proportion should be a double from 0.0 to 1.0.
     * 0.0 means no users will have the feature enabled, and 1.0 will mean that all users will have the feature enabled.
     * Values below zero will be treated the same as 0.0 and values above 1.0 will be treated the same as 1.0.
     * Returning Optional.empty() is equivalent to 0.0; both will return false for all users.
     *
     * @param config the config source
     * @param featureIdentifier the feature
     * @return some proportion between 0.0 and 1.0, or {@link Optional#empty()}
     */
    protected abstract Optional<Double> enabledProportion(T config, String featureIdentifier);
}
