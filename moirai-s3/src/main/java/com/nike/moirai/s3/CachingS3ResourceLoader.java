package com.nike.moirai.s3;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.function.Supplier;

/**
 * Supplies the contents of an object in S3 as a string using UTF-8 encoding. Caches object content and uses S3's ETag
 * constraint to reduce object reads when the object contents have not changed. This is intended as a drop-in
 * replacement for {@link S3ResourceLoader}.
 */
public class CachingS3ResourceLoader implements Supplier<String> {
    private final S3Client s3Client;
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
        return new CachingS3ResourceLoader(S3Client.create(), bucket, key);
    }

    /**
     * Creates an S3ResourceLoader using the provided client for the given S3 location
     *
     * @param s3Client the S3 client to use
     * @param bucket the bucket for the S3 resource
     * @param key the key within the bucket for the S3 resource
     * @return a supplier for the S3 resource as a string
     */
    public static CachingS3ResourceLoader withS3Client(S3Client s3Client, String bucket, String key) {
        return new CachingS3ResourceLoader(s3Client, bucket, key);
    }

    private CachingS3ResourceLoader(S3Client s3Client, String bucket, String key) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.key = key;
    }

    @Override
    public String get() {
        GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder().bucket(bucket).key(key);
        if (cachedObject != null) {
            requestBuilder.ifNoneMatch(cachedObject.eTag);
        }

        try {
            ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(requestBuilder.build());
            return cacheAndReturnObject(responseBytes);
        } catch (S3Exception s3Exception) {
            if (s3Exception.statusCode() == 304) {
                return cachedObject.content;
            }

            throw s3Exception;
        }
    }

    private String cacheAndReturnObject(ResponseBytes<GetObjectResponse> responseBytes) {
        String eTag = responseBytes.response().eTag();
        String content = responseBytes.asUtf8String();

        cachedObject = new CachedObject(content, eTag);
        return content;
    }
}
