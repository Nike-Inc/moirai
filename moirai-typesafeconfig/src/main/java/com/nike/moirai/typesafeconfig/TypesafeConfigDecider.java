package com.nike.moirai.typesafeconfig;

import com.nike.moirai.config.ConfigDecisionInput;
import com.nike.moirai.config.EnabledCustomDimensionConfigDecider;
import com.nike.moirai.config.EnabledUsersConfigDecider;
import com.nike.moirai.config.ProportionOfUsersConfigDecider;
import com.nike.moirai.config.WhitelistedUsersConfigDecider;
import com.typesafe.config.Config;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
     * @deprecated use {@link #ENABLED_USERS} instead (you must change your config key from whitelistedUserIds to enabledUserIds)
     */
    @Deprecated
    public static final Predicate<ConfigDecisionInput<Config>> WHITELISTED_USERS = new WhitelistedUsersConfigDecider<Config>() {
        @Override
        protected Collection<String> whitelistedUsers(Config config, String featureIdentifier) {
            String path = String.format("moirai.%s.whitelistedUserIds", featureIdentifier);
            return TypesafeConfigExtractor.extractCollection(config, path, Config::getStringList);
        }
    };

    /**
     * Reads the enabled list from the config at a path of "moirai.[featureIdentifier].enabledUserIds". For instance, for a
     * feature identifier of "foo.service.myfeature", the config value "moirai.foo.service.myfeature.enabledUserIds" will be read.
     * If that config path does not exist, an empty list of users will be provided.
     *
     * @see EnabledUsersConfigDecider
     */
    public static final Predicate<ConfigDecisionInput<Config>> ENABLED_USERS = new EnabledUsersConfigDecider<Config>() {
        @Override
        protected Collection<String> enabledUsers(Config config, String featureIdentifier) {
            String path = String.format("moirai.%s.enabledUserIds", featureIdentifier);
            return TypesafeConfigExtractor.extractCollection(config, path, Config::getStringList);
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
            return TypesafeConfigExtractor.extractOptional(config, path, Config::getDouble);
        }
    };

    /**
     * Reads the enabled values from the config at a path of of "moirai.[featureIdentifier].[configKey]". For instance, for a
     * feature identifier of "foo.service.myfeature" and a configKey of "enabledCountries", the config value "moirai.foo.service.myfeature.enabledCountries"
     * will be read. If that config path does not exist, an empty list of users will be provided.
     *
     * Your custom dimension values must match the provided type. If they do not, then the predicate will return false and log a warning message.
     *
     * @param dimensionKey the key used for the dimension; this should match how you construct your FeatureCheckInput
     * @param configKey the key used for the enabled values for the dimension
     * @param conversion a function to convert the values in the config from strings to the data-type used in your custom dimension
     * @param <V> the data-type of your custom dimension values
     * @return a Predicate that will return true if the FeatureCheckInput has a value for your custom dimension that matches the values read from the Config
     */
    public static <V> Predicate<ConfigDecisionInput<Config>> enabledCustomDimension(String dimensionKey, String configKey, Function<String, V> conversion) {
        return new EnabledCustomDimensionConfigDecider<Config, V>() {
            @Override
            protected String dimensionKey() {
                return dimensionKey;
            }

            @Override
            protected Collection<V> enabledValues(Config config, String featureIdentifier) {
                String path = String.format("moirai.%s.%s", featureIdentifier, configKey);

                return TypesafeConfigExtractor.extractCollection(config, path, Config::getStringList)
                    .stream().map(conversion).collect(Collectors.toList());
            }
        };
    }

    /**
     * Reads the enabled values from the config at a path of of "moirai.[featureIdentifier].[configKey]". For instance, for a
     * feature identifier of "foo.service.myfeature" and a configKey of "enabledCountries", the config value "moirai.foo.service.myfeature.enabledCountries"
     * will be read. If that config path does not exist, an empty list of users will be provided.
     *
     * Your custom dimension values must be strings. If the values are not strings, then the predicate will return false and log a warning message.
     *
     * @param dimensionKey the key used for the dimension; this should match how you construct your FeatureCheckInput
     * @return a Predicate that will return true if the FeatureCheckInput has a value for your custom dimension that matches the values read from the Config
     */
    public static Predicate<ConfigDecisionInput<Config>> enabledCustomStringDimension(String dimensionKey, String configKey) {
        return enabledCustomDimension(dimensionKey, configKey, Function.identity());
    }

    private TypesafeConfigDecider() {
        // prevent instantiation
    }
}
