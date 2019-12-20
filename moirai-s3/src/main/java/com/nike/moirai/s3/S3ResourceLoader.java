package com.nike.moirai.s3;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.util.function.Supplier;

/**
 * Supplies the contents of an object in S3 as a string using UTF-8 encoding.
 */
public class S3ResourceLoader implements Supplier<String> {
    private final S3Client s3Client;
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
        return new S3ResourceLoader(S3Client.create(), bucket, key);
    }

    /**
     * Creates an S3ResourceLoader using the provided client for the given S3 location
     *
     * @param s3Client the S3 client to use
     * @param bucket the bucket for the S3 resource
     * @param key the key within the bucket for the S3 resource
     * @return a supplier for the S3 resource as a string
     */
    public static S3ResourceLoader withS3Client(S3Client s3Client, String bucket, String key) {
        return new S3ResourceLoader(s3Client, bucket, key);
    }

    private S3ResourceLoader(S3Client s3Client, String bucket, String key) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.key = key;
    }

    @Override
    public String get() {
        return s3Client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build()).asUtf8String();
    }
}
