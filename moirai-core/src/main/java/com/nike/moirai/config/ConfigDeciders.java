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

    private ConfigDeciders() {
        // Prevent instantiation
    }
}
