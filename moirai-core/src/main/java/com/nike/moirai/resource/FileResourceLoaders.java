package com.nike.moirai.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Methods for loading resources from the file system.
 */
public class FileResourceLoaders {
    /**
     * Supplies the contents of a resource on the classpath as a string. Will read the resource as UTF-8.
     *
     * @param path a classpath resource path, as specified in {@link ClassLoader#getResource(String)}
     * @return a supplier that will read the resource into a string when called
     */
    public static Supplier<String> forClasspathResource(String path) {
        return () -> loadResourceBlocking(classpathFileStream(path), "classpath://" + path);
    }

    /**
     * Supplies the contents of a file as a string. Will read the file as UTF-8.
     *
     * @param file the file to read
     * @return a supplier that will read the file into a string when called
     */
    public static Supplier<String> forFile(File file) {
        return () -> loadResourceBlocking(fileStream(file), "file://" + file.getAbsolutePath());
    }

    private static InputStream fileStream(File file) {
        try {
            return new FileInputStream(file);
        } catch(IOException e) {
            throw new RuntimeException("Error loading resource from file: " + file, e);
        }
    }

    private static InputStream classpathFileStream(String path) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (stream == null) {
            throw new RuntimeException("Error loading resource from classpath://" + path);
        }

        return stream;
    }

    private static String loadResourceBlocking(InputStream in, String path) {
        try (
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr)) {

            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException | UncheckedIOException e) {
            throw new RuntimeException("Error loading resource from: " + path, e);
        }
    }

    private FileResourceLoaders() {
        // Prevent instantiation
    }
}
