package com.gateway.common;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * @author lw
 */
public class CreatResponse {

    static public FullHttpResponse createResponse(FullHttpResponse fullHttpResponse) {
        FullHttpResponse response = new DefaultFullHttpResponse(fullHttpResponse.protocolVersion(), OK,
                Unpooled.wrappedBuffer(getByteBuf(fullHttpResponse)));
        response.headers()
                .set(CONTENT_TYPE, TEXT_PLAIN)
                .setInt(CONTENT_LENGTH, response.content().readableBytes());

        boolean keepAlive = HttpUtil.isKeepAlive(fullHttpResponse);
        if (keepAlive) {
            if (!fullHttpResponse.protocolVersion().isKeepAliveDefault()) {
                response.headers().set(CONNECTION, KEEP_ALIVE);
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
        }
        return response;
    }

    private static byte[] getByteBuf(FullHttpResponse msg) {
        if (msg != null) {
            return msg.content().toString(CharsetUtil.UTF_8).getBytes();
        }
        return new byte[0];
    }

    public static FullHttpResponse creat404(FullHttpRequest request) {
        FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), NOT_FOUND,
                Unpooled.EMPTY_BUFFER);
        response.headers()
                .set(CONTENT_TYPE, TEXT_PLAIN)
                .setInt(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(CONNECTION, CLOSE);
        return response;
    }
}
