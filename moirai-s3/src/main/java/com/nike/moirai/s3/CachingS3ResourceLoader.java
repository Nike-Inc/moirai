package com.nike.moirai.s3;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Supplies the contents of an object in S3 as a string using UTF-8 encoding. Caches object content and uses S3's ETag
 * constraint to reduce object reads when the object contents have not changed. This is intended as a drop-in
 * replacement for {@link S3ResourceLoader}.
 */
public class CachingS3ResourceLoader implements Supplier<String> {
    private final AmazonS3 amazonS3Client;
    private final String bucket;
    private final String key;

    private static class CachedObject {
        final String content;
        final String eTag;

        CachedObject(String content, String eTag) {
            this.content = content;
            this.eTag = eTag;
        }
    }

    /**
     * the last successful read from S3. null when the object has never been read.
     */
    private volatile CachedObject cachedObject = null;

    /**
     * Creates an S3ResourceLoader for the given S3 location
     *
     * @param bucket the bucket for the S3 resource
     * @param key the key within the bucket for the S3 resource
     * @return a supplier for the S3 resource as a string
     */
    public static CachingS3ResourceLoader withDefaultCredentials(String bucket, String key) {
        return new CachingS3ResourceLoader(new AmazonS3Client(new DefaultAWSCredentialsProviderChain()), bucket, key);
    }

    /**
     * Creates an S3ResourceLoader using the provided client for the given S3 location
     *
     * @param amazonS3 the S3 client to use
     * @param bucket the bucket for the S3 resource
     * @param key the key within the bucket for the S3 resource
     * @return a supplier for the S3 resource as a string
     */
    public static CachingS3ResourceLoader withS3Client(AmazonS3 amazonS3, String bucket, String key) {
        return new CachingS3ResourceLoader(amazonS3, bucket, key);
    }

    private CachingS3ResourceLoader(AmazonS3 amazonS3, String bucket, String key) {
        this.amazonS3Client = amazonS3;
        this.bucket = bucket;
        this.key = key;
    }

    @Override
    public String get() {

        GetObjectRequest request = new GetObjectRequest(bucket, key);
        if (cachedObject != null) {
            request = request.withNonmatchingETagConstraint(cachedObject.eTag);
        }

        S3Object s3Object = amazonS3Client.getObject(request);
        if (s3Object == null) { //eTag hasn't changed
            return cachedObject.content;
        } else {
            return cacheAndReturnObject(s3Object);
        }
    }

    private String cacheAndReturnObject(S3Object s3Object) {

        try (
            InputStream in = s3Object.getObjectContent();
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr)) {

            String eTag = s3Object.getObjectMetadata().getETag();
            String content = br.lines().collect(Collectors.joining(System.lineSeparator()));

            cachedObject = new CachedObject(content, eTag);
            return content;
        } catch (IOException | UncheckedIOException e) {
            throw new RuntimeException("Error loading resource from s3://" + bucket + "/" + key, e);
        }
    }
}
