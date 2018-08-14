package com.nike.moirairiposteexample.testutils;

import com.nike.riposte.server.channelpipeline.ChannelAttributes;
import com.nike.riposte.server.http.HttpProcessingState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

import java.io.IOException;
import java.net.ServerSocket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {

    /**
     * Finds an unused port on the machine hosting the currently running JVM.
     */
    public static int findFreePort() {
        ServerSocket serverSocket = null;
        try {
            try {
                serverSocket = new ServerSocket(0);
                return serverSocket.getLocalPort();
            } finally {
                if (serverSocket != null)
                    serverSocket.close();
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ChannelHandlerContext mockChannelHandlerContext() {
        ChannelHandlerContext mockContext = mock(ChannelHandlerContext.class);
        when(mockContext.channel()).thenReturn(mock(Channel.class));
        @SuppressWarnings("unchecked")
        Attribute<HttpProcessingState> mockAttribute = mock(Attribute.class);
        when(mockContext.channel().attr(ChannelAttributes.HTTP_PROCESSING_STATE_ATTRIBUTE_KEY)).thenReturn(mockAttribute);
        when(mockContext.channel().attr(ChannelAttributes.HTTP_PROCESSING_STATE_ATTRIBUTE_KEY).get()).thenReturn(mock(HttpProcessingState.class));

        return mockContext;
    }
}
