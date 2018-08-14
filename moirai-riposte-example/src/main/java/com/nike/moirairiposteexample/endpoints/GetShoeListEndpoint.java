package com.nike.moirairiposteexample.endpoints;

import com.google.common.io.Resources;
import com.nike.backstopper.exception.ApiException;
import com.nike.moirai.ConfigFeatureFlagChecker;
import com.nike.moirai.FeatureCheckInput;
import com.nike.moirairiposteexample.error.ProjectApiError;
import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.ResponseInfo;
import com.nike.riposte.server.http.StandardEndpoint;
import com.nike.riposte.util.Matcher;
import com.typesafe.config.Config;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public class GetShoeListEndpoint extends StandardEndpoint<Void, String> implements EndpointConstants {
    public static final String MATCHING_PATH = String.format("%s/me/shoes", "/shoeretriever");
    protected final ConfigFeatureFlagChecker<Config> featureFlagChecker;

    public GetShoeListEndpoint(ConfigFeatureFlagChecker<Config> featureFlagChecker) {
        this.featureFlagChecker = featureFlagChecker;
    }


    @Override
    public CompletableFuture<ResponseInfo<String>> execute(RequestInfo<Void> request,
                                              Executor longRunningTaskExecutor,
                                              ChannelHandlerContext ctx) {
        String id = getAndValidateId(request);
        if (isThisFeatureEnabled(id)) {
                return CompletableFuture.completedFuture(ResponseInfo.<String>newBuilder().
                        withHttpStatusCode(200).
                        withDesiredContentWriterMimeType("application/json").
                        withContentForFullResponse(readFile("all_nike_shoes.json")).
                        build());
        } else {
            return CompletableFuture.completedFuture(
                    ResponseInfo.<String>newBuilder().
                            withHttpStatusCode(200).
                            withDesiredContentWriterMimeType("application/json").
                            withContentForFullResponse(readFile("badNonNikeShoesList.json")).
                            build());
        }
    }

    private String readFile(String fileName) {
        String file;
        try {
            file = Resources.toString(Resources.getResource("shoes/" + fileName), Charset.forName("utf-8"));
        } catch (Exception e) {
            throw new ApiException(ProjectApiError.MISSING_SHOE_RECORD_FILE);
        }
        return file;
    }

    boolean isThisFeatureEnabled(String id) {
        return featureFlagChecker.isFeatureEnabled("getshoelist.allowedIds", FeatureCheckInput.forUser(id));
    }

        @Override
        public Matcher requestMatcher() {
            return Matcher.match(MATCHING_PATH, HttpMethod.GET);
        }

}

