package com.nike.moirai.typesafeconfig;

import com.nike.moirai.config.ConfigDecisionInput;
import com.nike.moirai.config.ProportionOfUsersConfigDecider;
import com.nike.moirai.config.WhitelistedUsersConfigDecider;
import com.typesafe.config.Config;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Predicate implementations that read from a Typesafe {@link Config}.
 */
public class TypesafeConfigDecider {
    /**
     * Reads the whitelist from the config at a path of "moirai.[featureIdentifier].whitelistedUserIds". For instance, for a
     * feature identifier of "foo.service.myfeature", the config value "moirai.foo.service.myfeature.whitelistedUserIds" will be read.
     * If that config path does not exist, an empty list of users will be provided.
     *
     * @see WhitelistedUsersConfigDecider
     */
    public static final Predicate<ConfigDecisionInput<Config>> WHITELISTED_USERS = new WhitelistedUsersConfigDecider<Config>() {
        @Override
        protected Collection<String> whitelistedUsers(Config config, String featureIdentifier) {
            String path = String.format("moirai.%s.whitelistedUserIds", featureIdentifier);

            if (config.hasPath(path)) {
                return config.getStringList(path);
            }

            return Collections.emptyList();
        }
    };

    /**
     * Reads the enabled proportion of users from the config at a path of "moirai.[featureIdentifier].enabledProportion". For instance, for a
     * feature identifier of "foo.service.myfeature", the config value "moirai.foo.service.myfeature.enabledProportion" will be read. If that config
     * path does not exist, {@link Optional#empty()} will be provided.
     *
     * @see ProportionOfUsersConfigDecider
     */
    public static final Predicate<ConfigDecisionInput<Config>> PROPORTION_OF_USERS = new ProportionOfUsersConfigDecider<Config>() {
        @Override
        protected Optional<Double> enabledProportion(Config config, String featureIdentifier) {
            String path = String.format("moirai.%s.enabledProportion", featureIdentifier);
            if (config.hasPath(path)) {
                return Optional.of(config.getDouble(path));
            }

            return Optional.empty();
        }
    };

    private TypesafeConfigDecider() {
        // prevent instantiation
    }
}
