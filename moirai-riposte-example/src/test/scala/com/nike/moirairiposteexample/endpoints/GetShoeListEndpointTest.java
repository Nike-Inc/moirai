package com.nike.moirairiposteexample.endpoints;

import com.google.common.io.Resources;
import com.nike.backstopper.exception.ApiException;
import com.nike.moirai.ConfigFeatureFlagChecker;
import com.nike.moirai.Suppliers;
import com.nike.moirai.config.ConfigDecisionInput;
import com.nike.moirai.typesafeconfig.TypesafeConfigDecider;
import com.nike.moirai.typesafeconfig.TypesafeConfigReader;
import com.nike.moirairiposteexample.testutils.TestUtils;
import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.ResponseInfo;
import com.typesafe.config.Config;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.nike.moirairiposteexample.error.ProjectApiError.MISSING_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetShoeListEndpointTest {
    GetShoeListEndpoint underTest;
    ChannelHandlerContext mockContext;
    Executor executor;
    String upmId;
    ConfigFeatureFlagChecker<Config> featureFlagChecker;

    @Before
    public void setup() {
        Predicate<ConfigDecisionInput<Config>> whiteListedUsersDecider = TypesafeConfigDecider.ENABLED_USERS
                .or(TypesafeConfigDecider.PROPORTION_OF_USERS);
        String conf;
        try {
            conf = Resources.toString(Resources.getResource("moirai.conf"), Charset.forName("UTF-8"));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        Supplier<Config> supp = Suppliers.supplierAndThen(() -> conf, TypesafeConfigReader.FROM_STRING);
        featureFlagChecker = ConfigFeatureFlagChecker.forConfigSupplier(supp, whiteListedUsersDecider);

        underTest = new GetShoeListEndpoint(featureFlagChecker);

        upmId = UUID.randomUUID().toString();

        mockContext = TestUtils.mockChannelHandlerContext();
        executor = Executors.newCachedThreadPool();
    }

    protected static String getFile(String filename) {
        try {
            return Resources.toString(Resources.getResource("shoes/" + filename), Charset.forName("utf-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void happy_path_feature_flag_on() {
        @SuppressWarnings("unchecked")
        RequestInfo<Void> requestInfo = mock(RequestInfo.class);
        when(requestInfo.getHeaders()).thenReturn(mock(HttpHeaders.class));
        when(requestInfo.getHeaders().get("id")).thenReturn("id1");

        String responseString = getFile("all_nike_shoes.json");

        CompletableFuture<ResponseInfo<String>> responseFuture = underTest.execute(requestInfo, executor, mockContext);
        ResponseInfo<String> responseInfo = responseFuture.join();
        assertThat(responseInfo.getHttpStatusCode()).isEqualTo(200);
        assertThat(responseInfo.getContentForFullResponse()).isEqualTo(responseString);
    }

    @Test
    public void happy_path_feature_flag_off() {
        @SuppressWarnings("unchecked")
        RequestInfo<Void> requestInfo = mock(RequestInfo.class);
        when(requestInfo.getHeaders()).thenReturn(mock(HttpHeaders.class));
        when(requestInfo.getHeaders().get("upmid")).thenReturn(upmId);
        when(requestInfo.getHeaders().get("id")).thenReturn("99");

        String responseString = getFile("badNonNikeShoesList.json");

        CompletableFuture<ResponseInfo<String>> responseFuture = underTest.execute(requestInfo, executor, mockContext);
        ResponseInfo<String> responseInfo = responseFuture.join();
        assertThat(responseInfo.getHttpStatusCode()).isEqualTo(200);
        assertThat(responseInfo.getContentForFullResponse()).isEqualTo(responseString);
    }

    @Test
    public void null_upm_id() {
        @SuppressWarnings("unchecked")
        RequestInfo<Void> requestInfo = mock(RequestInfo.class);
        when(requestInfo.getHeaders()).thenReturn(mock(HttpHeaders.class));
        when(requestInfo.getHeaders().get("upmid")).thenReturn(null);
        when(requestInfo.getHeaders().get("id")).thenReturn(null);

        Throwable ex = catchThrowable(() -> underTest.execute(requestInfo, executor, mockContext).join().getContentForFullResponse());
        assertThat(ex).isInstanceOf(ApiException.class);
        ApiException apiEx = (ApiException)ex;
        assertThat(apiEx.getApiErrors().size()).isEqualTo(1);
        assertThat(apiEx.getApiErrors().get(0).getErrorCode()).isEqualTo(MISSING_ID_HEADER.getErrorCode());
    }
}
