package com.demo.gateway.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
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
@Deprecated
public class ThirdClientAsync implements Client {

    private AsyncHttpClient asyncHttpClient = asyncHttpClient();

    /**
     * 调用第三方客户端，获取并返回响应结果
     * @param url 请求地址
     * @return 响应结果
     * @throws ExecutionException exception
     * @throws InterruptedException exception
     */
    private FullHttpResponse getResponse(String url) throws ExecutionException, InterruptedException {
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
    public FullHttpResponse execute(FullHttpRequest request, Channel serverOutbound) {
        try {
            return getResponse(request.uri());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error, get response");
        }
        return null;
    }

}
