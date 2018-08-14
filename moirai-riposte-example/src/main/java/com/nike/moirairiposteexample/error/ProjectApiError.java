package com.nike.moirairiposteexample.error;

import com.nike.backstopper.apierror.ApiError;
import com.nike.backstopper.apierror.ApiErrorBase;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;
import java.util.UUID;

public enum ProjectApiError implements ApiError {

    MISSING_ID_HEADER(99154, "The request is missing a id header.", HttpResponseStatus.INTERNAL_SERVER_ERROR.code()),
    MISSING_SHOE_RECORD_FILE(99155, "File for show record missing", HttpResponseStatus.INTERNAL_SERVER_ERROR.code()),
    ;


    private final ApiError delegate;

    ProjectApiError(ApiError delegate) {
        this.delegate = delegate;
    }

    ProjectApiError(int errorCode, String message, int httpStatusCode) {
        this(errorCode, message, httpStatusCode, null);
    }

    ProjectApiError(int errorCode, String message, int httpStatusCode, Map<String, Object> metadata) {
        this(new ApiErrorBase(
            "delegated-to-enum-wrapper-" + UUID.randomUUID().toString(), errorCode, message, httpStatusCode, metadata
        ));
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getErrorCode() {
        return delegate.getErrorCode();
    }

    @Override
    public String getMessage() {
        return delegate.getMessage();
    }

    @Override
    public Map<String, Object> getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public int getHttpStatusCode() {
        return delegate.getHttpStatusCode();
    }

}
