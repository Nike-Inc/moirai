package com.nike.moirairiposteexample.endpoints;

import com.nike.backstopper.exception.ApiException;
import com.nike.moirairiposteexample.error.ProjectApiError;
import com.nike.riposte.server.http.RequestInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public interface EndpointConstants {
    String ID_HEADER_KEY = "id";

    default String getAndValidateId(RequestInfo<?> request) {
        String idParam = request.getHeaders().get(ID_HEADER_KEY);
        if (idParam == null || idParam.trim().isEmpty()) {
            throw new ApiException(ProjectApiError.MISSING_ID_HEADER);
        }
        return urlDecode(idParam, StandardCharsets.UTF_8.name());
    }

    default String urlDecode(String valueToDecode, String encoding) {
        try {
            return URLDecoder.decode(valueToDecode, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
