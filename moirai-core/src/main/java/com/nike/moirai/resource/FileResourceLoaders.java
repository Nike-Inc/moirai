package com.nike.moirai.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
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
        return forFile(classpathFile(path));
    }

    /**
     * Supplies the contents of a file as a string. Will read the file as UTF-8.
     *
     * @param file the file to read
     * @return a supplier that will read the file into a string when called
     */
    public static Supplier<String> forFile(File file) {
        return () -> loadResourceBlocking(file);
    }

    private static File classpathFile(String path) {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url != null) {
                return new File(url.toURI());
            } else {
                throw new RuntimeException("Could not find resource from classpath://" + path);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error loading resource from classpath://" + path, e);
        }
    }

    private static String loadResourceBlocking(File file) {
        try (
            InputStream in = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr)) {

            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException | UncheckedIOException e) {
            throw new RuntimeException("Error loading resource from file: " + file, e);
        }
    }

    private FileResourceLoaders() {
        // Prevent instantiation
    }
}
