package com.nike.moirai.config;

import com.nike.moirai.FeatureCheckInput;

import java.util.Collection;
import java.util.function.Predicate;

import static com.nike.moirai.config.ConfigDeciders.userIdCheck;

/**
 * Returns true for a configured list of users.
 *
 * @param <T> the type of config
 */
public abstract class EnabledUsersConfigDecider<T> extends EnabledValuesConfigDecider<T, String> {
    @Override
    protected boolean checkValue(FeatureCheckInput featureCheckInput, Predicate<String> check) {
        return userIdCheck(featureCheckInput, check);
    }

    @Override
    protected Collection<String> enabledValues(T config, String featureIdentifier) {
        return enabledUsers(config, featureIdentifier);
    }

    /**
     * Provide the collection of users that should have the given feature enabled. Return an empty list if no configuration is provided for the feature.
     *
     * @param config the config source
     * @param featureIdentifier the feature
     * @return the collection of userIds that should be enabled for the feature
     */
    protected abstract Collection<String> enabledUsers(T config, String featureIdentifier);
}
