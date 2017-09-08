package com.nike.moirai.config;

import com.nike.moirai.FeatureCheckInput;

import java.util.Objects;

/**
 * The input to a Predicate for making feature flag decisions based on some config type T.
 *
 * The config type could be, for example, {@link java.util.Properties}, com.typesafe.config.Config, or some custom type
 *
 * @param <T> the type of configuration object
 */
public class ConfigDecisionInput<T> {
    private final T config;
    private final String featureIdentifier;
    private final FeatureCheckInput featureCheckInput;

    /**
     * @param config the config to use for the decision
     * @param featureIdentifier the feature to decide upon
     * @param featureCheckInput the input data for the decision
     */
    public ConfigDecisionInput(T config, String featureIdentifier, FeatureCheckInput featureCheckInput) {
        this.config = config;
        this.featureIdentifier = featureIdentifier;
        this.featureCheckInput = featureCheckInput;
    }

    /**
     * @return the config
     */
    public T getConfig() {
        return config;
    }

    /**
     * @return the feature identifier
     */
    public String getFeatureIdentifier() {
        return featureIdentifier;
    }

    /**
     * @return the feature check input
     */
    public FeatureCheckInput getFeatureCheckInput() {
        return featureCheckInput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConfigDecisionInput<?> that = (ConfigDecisionInput<?>) o;
        return Objects.equals(config, that.config) &&
            Objects.equals(featureIdentifier, that.featureIdentifier) &&
            Objects.equals(featureCheckInput, that.featureCheckInput);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, featureIdentifier, featureCheckInput);
    }

    @Override
    public String toString() {
        return "ConfigDecisionInput{" +
            "config=" + config +
            ", featureIdentifier='" + featureIdentifier + '\'' +
            ", featureCheckInput=" + featureCheckInput +
            '}';
    }
}
