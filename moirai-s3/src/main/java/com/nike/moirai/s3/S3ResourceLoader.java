package com.nike.moirai.s3;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Supplies the contents of an object in S3 as a string using UTF-8 encoding.
 */
public class S3ResourceLoader implements Supplier<String> {
    private final AmazonS3 amazonS3Client;
    private final String bucket;
    private final String key;

    /**
     * Creates an S3ResourceLoader for the given S3 location
     *
     * @param bucket the bucket for the S3 resource
     * @param key the key within the bucket for the S3 resource
     * @return a supplier for the S3 resource as a string
     */
    public static S3ResourceLoader withDefaultCredentials(String bucket, String key) {
        return new S3ResourceLoader(new AmazonS3Client(new DefaultAWSCredentialsProviderChain()), bucket, key);
    }

    /**
     * Creates an S3ResourceLoader using the provided client for the given S3 location
     *
     * @param amazonS3 the S3 client to use
     * @param bucket the bucket for the S3 resource
     * @param key the key within the bucket for the S3 resource
     * @return a supplier for the S3 resource as a string
     */
    public static S3ResourceLoader withS3Client(AmazonS3 amazonS3, String bucket, String key) {
        return new S3ResourceLoader(amazonS3, bucket, key);
    }

    private S3ResourceLoader(AmazonS3 amazonS3, String bucket, String key) {
        this.amazonS3Client = amazonS3;
        this.bucket = bucket;
        this.key = key;
    }

    @Override
    public String get() {
        S3Object s3Object = amazonS3Client.getObject(bucket, key);

        try (
            InputStream in = s3Object.getObjectContent();
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr)) {

            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException | UncheckedIOException e) {
            throw new RuntimeException("Error loading resource from s3://" + bucket + "/" + key, e);
        }
    }
}
