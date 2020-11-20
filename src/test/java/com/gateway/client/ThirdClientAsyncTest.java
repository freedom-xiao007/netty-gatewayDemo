package com.gateway.client;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.asynchttpclient.Dsl.asyncHttpClient;


/**
 * @author lw
 * 第三方的异步高性能客户端
 * https://github.com/AsyncHttpClient/async-http-client
 */
public class ThirdClientAsyncTest {

    /**
     * 检测客户端是否能获取响应
     */
    @Test
    public void test() throws ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        Future<Response> responseFuture = asyncHttpClient.prepareGet("http://www.baidu.com/").execute();
        Response response = responseFuture.get();
        System.out.println(response.toString());
        System.out.println(response.getStatusCode());
        System.out.println(response.getHeaders().toString());
        System.out.println(Arrays.toString(response.getResponseBody().getBytes()));
        assert response.getStatusCode() == 200;
    }
}
