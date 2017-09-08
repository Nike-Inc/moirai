package com.nike.moirai;

/**
 * A FeatureFlagChecker allows checking of some feature (identified by a String) is enabled for some {@link FeatureCheckInput}.
 */
public interface FeatureFlagChecker {
    /**
     * Checks if the feature should be enabled given the provided input
     *
     * @param featureIdentifier the identifier of the feature to check on
     * @param featureCheckInput the input dimensions to base a decision on
     * @return whether the feature should be enabled
     */
    boolean isFeatureEnabled(String featureIdentifier, FeatureCheckInput featureCheckInput);
}
