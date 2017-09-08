package com.nike.moirai;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utilities for composing {@link Supplier} instances.
 */
public class Suppliers {
    /**
     * Transforms the supplier by applying the given function to the results from the supplied {@link CompletableFuture}
     *
     * @param input supplier to transform
     * @param after the function to transform the future values
     * @param <T> the input value type
     * @param <U> the result value type
     * @return the transformed supplier
     */
    public static <T, U> Supplier<CompletableFuture<U>> futureSupplierAndThen(Supplier<CompletableFuture<T>> input, Function<T, U> after) {
        return () -> input.get().thenApply(after);
    }

    /**
     * Transforms the supplier by applying the given function to the supplied result
     * @param input the supplier to transform
     * @param after the function to transform the supplied value
     * @param <T> the input value type
     * @param <U> the result value type
     * @return the transformed supplier
     */
    public static <T, U> Supplier<U> supplierAndThen(Supplier<T> input, Function<T, U> after) {
        return () -> after.apply(input.get());
    }

    /**
     * Transforms a supplier of a value to a supplier of that value as CompletableFuture for use in an asynchronous context.
     *
     * @param supplier the supplier to transform
     * @param <T> the supplier value type
     * @return a supplier that wraps the value in a {@link CompletableFuture}
     */
    public static <T> Supplier<CompletableFuture<T>> async(Supplier<T> supplier) {
        return () -> CompletableFuture.supplyAsync(supplier);
    }

    private Suppliers() {
        // Prevent instantiation
    }
}
