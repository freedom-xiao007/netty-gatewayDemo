package com.gateway.client;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.asynchttpclient.Dsl.*;

public class ClientTest {

//    private HttpRequest request;
//    private String host = "192.168.101.105";
//    private int port = 8080;
//    private EventLoopGroup clientGroup = new NioEventLoopGroup();
//
//    @Before
//    public void setUp() throws URISyntaxException {
////        Client.init(clientGroup);
////        Client.initFuture(clientGroup);
//
//        String url = "http://192.168.101.105:8080/";
//        URI uri = new URI(url);
//        // Prepare the HTTP request.
//        request = new DefaultFullHttpRequest(
//                HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath(), Unpooled.EMPTY_BUFFER);
//        request.headers().set(HttpHeaderNames.HOST, host);
//        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
//        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
//
//        // Set some example cookies.
//        request.headers().set(
//                HttpHeaderNames.COOKIE,
//                io.netty.handler.codec.http.cookie.ClientCookieEncoder.STRICT.encode(
//                        new io.netty.handler.codec.http.cookie.DefaultCookie("my-cookie", "foo"),
//                        new DefaultCookie("another-cookie", "bar")));
//    }

    @Test
    public void sendTest() throws InterruptedException, URISyntaxException {
        URI uri = new URI("http://127.0.0.1:8080/");
        String host = "127.0.0.1";
        HttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath(), Unpooled.EMPTY_BUFFER);
        request.headers().set(HttpHeaderNames.HOST, host);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

        // Set some example cookies.
        request.headers().set(
                HttpHeaderNames.COOKIE,
                ClientCookieEncoder.STRICT.encode(
                        new DefaultCookie("my-cookie", "foo"),
                        new DefaultCookie("another-cookie", "bar")));

        Thread.sleep(3000);
    }

    @Test
    public void asyncClientTest() throws ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        Future<Response> responseFuture = asyncHttpClient.prepareGet("http://192.168.101.105:8080/").execute();
        Response response = responseFuture.get();
        System.out.println(response.toString());
        System.out.println(response.getStatusCode());
        System.out.println(response.getHeaders().toString());
        System.out.println(response.getResponseBody().toString().getBytes());
    }
}
