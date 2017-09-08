package com.nike.moirai.config;

import java.util.Collection;
import java.util.function.Predicate;

import static com.nike.moirai.config.ConfigDeciders.userIdCheck;

/**
 * Returns true for a configured list of users.
 *
 * @param <T> the type of config
 */
public abstract class WhitelistedUsersConfigDecider<T> implements Predicate<ConfigDecisionInput<T>> {
    @Override
    public boolean test(ConfigDecisionInput<T> configDecisionInput) {
        return userIdCheck(configDecisionInput.getFeatureCheckInput(), userId ->
            whitelistedUsers(configDecisionInput.getConfig(), configDecisionInput.getFeatureIdentifier()).contains(userId)
        );
    }

    /**
     * Provide the collection of users that should have the given feature enabled. Return an empty list if no configuration is provided for the feature.
     *
     * @param config the config source
     * @param featureIdentifier the feature
     * @return the collection of userIds that should be enabled for the feature
     */
    protected abstract Collection<String> whitelistedUsers(T config, String featureIdentifier);
}
