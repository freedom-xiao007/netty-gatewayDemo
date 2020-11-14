package com.gateway.client;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static org.asynchttpclient.Dsl.asyncHttpClient;


/**
 * @author lw
 * 第三方的异步高性能客户端
 * https://github.com/AsyncHttpClient/async-http-client
 */
public class ClientAsync implements Client {

    private AsyncHttpClient asyncHttpClient = asyncHttpClient();

    public FullHttpResponse getResponse(String url) throws ExecutionException, InterruptedException {
        ListenableFuture<Response> responseFuture = asyncHttpClient.prepareGet(url).execute();
        Response originResponse = responseFuture.get();

        Charset charset = StandardCharsets.US_ASCII;
        byte[] responseBody = originResponse.getResponseBody().getBytes(charset);

        // 这里的返回码没有找到构造方法，暂时简单的使用200和404
        HttpResponseStatus status = HttpResponseStatus.OK;
        if (originResponse.getStatusCode() != 200) {
            status = HttpResponseStatus.NOT_FOUND;
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.wrappedBuffer(responseBody));
        response.headers()
                .set(CONTENT_TYPE, TEXT_PLAIN)
                .setInt(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    @Override
    public FullHttpResponse sendAndGet(String address, String port, HttpRequest request) {
        try {
            return getResponse("http://" + address + ":" + port + request.uri());
        } catch (Exception e) {
            System.out.println("Error, get response");
        }
        return null;
    }
}
