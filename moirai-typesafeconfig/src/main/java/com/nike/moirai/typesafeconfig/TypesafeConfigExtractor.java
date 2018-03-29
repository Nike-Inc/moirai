package com.nike.moirai.typesafeconfig;

import com.typesafe.config.Config;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Support for safely extracting values from Typesafe config.
 */
public class TypesafeConfigExtractor {
    /**
     * Extracts a collection of values from a config, returning an empty collection if the path is not present.
     *
     * @param config  the config to extract from
     * @param path    the path to extract
     * @param extract the function to extract the collection if the path is present
     * @param <T>     the type of collection to extract
     * @return a collection with the values at the path or an empty collection
     */
    public static <T> Collection<T> extractCollection(Config config, String path, BiFunction<Config, String, Collection<T>> extract) {
        if (config.hasPath(path)) {
            return extract.apply(config, path);
        }

        return Collections.emptyList();
    }

    /**
     * Extracts a value as an option, returning empty if the path is not present.
     *
     * @param config the config to extract from
     * @param path the path to extract
     * @param extract the function to extract the collection if the path is present
     * @param <T> the type of optional to extract
     * @return an optional with the values at the path or an empty optional
     */
    public static <T> Optional<T> extractOptional(Config config, String path, BiFunction<Config, String, T> extract) {
        if (config.hasPath(path)) {
            return Optional.of(extract.apply(config, path));
        }

        return Optional.empty();
    }

    private TypesafeConfigExtractor() {
        // prevent instantiation
    }
}
