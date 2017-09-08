package com.nike.moirai;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The input for checking if a feature is enabled.
 */
public class FeatureCheckInput {
    /** Built-in and reserved keys for input dimensions. */
    public enum DimensionKey {
        USER_ID, DATE_TIME;

        private static Set<String> KEYS = Arrays.stream(DimensionKey.values()).map(DimensionKey::name).collect(Collectors.toSet());
    }

    private final Map<String, ?> dimensions;

    /**
     * Builds a {@link FeatureCheckInput}.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Builder {
        private final Map<String, Object> dimensions;
        private Optional<String> userId = Optional.empty();
        private Optional<Instant> dateTime = Optional.empty();

        /**
         * Empty builder
         */
        public Builder() {
            this.dimensions = new HashMap<>();
        }

        /**
         * @param featureCheckInput existing input to copy from
         */
        public Builder(FeatureCheckInput featureCheckInput) {
            this.dimensions = new HashMap<>(featureCheckInput.dimensions);
        }

        /**
         * @param userId the id of the user accessing the feature
         * @return this
         */
        public Builder userId(String userId) {
            this.userId = Optional.ofNullable(userId);
            return this;
        }

        /**
         * Input a time for the feature check for date-based feature access.
         * <p>
         * This could be the current time of the request, or it could be relevant time for a piece of data that the
         * feature should apply to.
         *
         * @param dateTime the time associated with the feature check
         * @return this
         */
        public Builder dateTime(Instant dateTime) {
            this.dateTime = Optional.ofNullable(dateTime);
            return this;
        }

        /**
         * Input a custom dimension using a key that a provided ConfigDecider knows about
         * and the appropriate data type for that value that the ConfigDecider expects.
         *
         * @param dimensionKey   custom key identifying the input dimension
         * @param dimensionValue custom value for the input dimension
         * @return this
         */
        public Builder dimension(String dimensionKey, Object dimensionValue) {
            if (DimensionKey.KEYS.contains(dimensionKey)) {
                throw new IllegalArgumentException(String.format("Dimension key '%s' conflicts with built-in dimension key", dimensionKey));
            }

            this.dimensions.put(dimensionKey, dimensionValue);
            return this;
        }

        /**
         * Input several custom dimensions
         *
         * @param additionalDimensions additional dimensions
         * @return this
         */
        public Builder dimensions(Map<String, ?> additionalDimensions) {
            for (String key : additionalDimensions.keySet()) {
                this.dimension(key, additionalDimensions.get(key));
            }

            return this;
        }

        /**
         * @return a new FeatureCheckInput
         */
        public FeatureCheckInput build() {
            this.userId.ifPresent(v -> this.dimensions.put(DimensionKey.USER_ID.name(), v));
            this.dateTime.ifPresent(v -> this.dimensions.put(DimensionKey.DATE_TIME.name(), v));

            return new FeatureCheckInput(this.dimensions);
        }
    }

    /**
     * Create input for a feature check of the given feature and user at the current time.
     * Sets the dateTime dimension to now.
     *
     * @param userId the id of the user accessing the feature
     * @return a new FeatureCheckInput
     */
    public static FeatureCheckInput forUser(String userId) {
        return new Builder().userId(userId).dateTime(Instant.now()).build();
    }

    /**
     * Create input for a feature check of the given user at the current time.
     * Sets the dateTime dimension to now.
     *
     * @param userId   the id of the user accessing the feature
     * @param dateTime the time associated with the feature check
     * @return a new FeatureCheckInput
     */
    public static FeatureCheckInput forUserAtTime(String userId, Instant dateTime) {
        return new Builder().userId(userId).dateTime(dateTime).build();
    }

    FeatureCheckInput(Map<String, ?> dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * @return builder based on this input
     */
    public Builder builder() {
        return new Builder(this);
    }

    /**
     * @param additionalDimensions additional dimensions to add to the input
     * @return input with additional dimensions added
     */
    public FeatureCheckInput withAdditionalDimensions(Map<String, ?> additionalDimensions) {
        return this.builder().dimensions(additionalDimensions).build();
    }

    /**
     * @return the id of the user accessing the feature
     */
    public Optional<String> getUserId() {
        return Optional.ofNullable((String) this.dimensions.get(DimensionKey.USER_ID.name()));
    }

    /**
     * @return the time at which to check if the feature is enabled
     */
    public Optional<Instant> getDateTime() {
        return Optional.ofNullable((Instant) this.dimensions.get(DimensionKey.DATE_TIME.name()));
    }

    /**
     * @param dimensionKey the identifier of a custom dimension
     * @return the value for the custom dimension
     */
    public Optional<?> getDimension(String dimensionKey) {
        return Optional.ofNullable(this.dimensions.get(dimensionKey));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FeatureCheckInput that = (FeatureCheckInput) o;
        return Objects.equals(dimensions, that.dimensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimensions);
    }

    @Override
    public String toString() {
        return "FeatureCheckInput{" +
            "dimensions=" + dimensions +
            '}';
    }
}
