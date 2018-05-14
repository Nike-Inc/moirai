package com.nike.moirai.config;

import com.nike.moirai.FeatureCheckInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

import static com.nike.moirai.config.ConfigDeciders.customDimensionCheck;

/**
 * Returns true for a configured collection of values for some custom dimension.
 *
 * @param <C> the type of config
 * @param <V> the type of value for the dimension
 */
public abstract class EnabledCustomDimensionConfigDecider<C, V> extends EnabledValuesConfigDecider<C, V> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected boolean checkValue(FeatureCheckInput featureCheckInput, Predicate<V> check) {
        return customDimensionCheck(featureCheckInput, dimensionKey(), cast(check));
    }

    @SuppressWarnings("unchecked")
    private Predicate<Object> cast(Predicate<V> check) {
        return o -> {
            try {
                return check.test((V)o);
            } catch (ClassCastException e) {
                logger.warn("Mismatched type found, got: " + o, e);
                return false;
            }
        };
    }

    /**
     * The key used for the custom dimension on the feature input that will be checked.
     *
     * @return the key  of the dimension
     */
    protected abstract String dimensionKey();
}
