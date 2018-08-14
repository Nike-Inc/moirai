package com.nike.moirai.config;

import com.nike.moirai.FeatureCheckInput;

import java.util.function.Predicate;

/**
 * Useful methods for implementing a Predicate&lt;ConfigDecisionInput&lt;T&gt;&gt;
 */
public class ConfigDeciders {
    /**
     * Apply a predicate to the userId from a feature check input.
     *
     * @param featureCheckInput the input to check
     * @param userIdCheck the predicate to apply
     * @return false if there is no userId in the input, otherwise the result of the predicate applied to the userId
     */
    public static boolean userIdCheck(FeatureCheckInput featureCheckInput, Predicate<String> userIdCheck) {
        return featureCheckInput.getUserId().map(userIdCheck::test).orElse(false);
    }

    /**
     * Apply a predicate to the dimension value from a feature check input.
     *
     * @param featureCheckInput the input to check
     * @param dimensionKey the dimension to check
     * @param dimensionCheck the predicate to apply
     * @return false if there is no userId in the input, otherwise the result of the predicate applied to the userId
     */
    public static boolean customDimensionCheck(FeatureCheckInput featureCheckInput, String dimensionKey, Predicate<Object> dimensionCheck) {
        return featureCheckInput.getDimension(dimensionKey).map(dimensionCheck::test).orElse(false);
    }

    private ConfigDeciders() {
        // Prevent instantiation
    }
}
