package com.gateway.client;

import com.gateway.Util.ThreadInfo;
import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.checkerframework.checker.units.qual.C;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class ClientSynTest {

    final String URL = System.getProperty("url", "http://127.0.0.1:8081/");

    @Test
    public void test() throws URISyntaxException, InterruptedException {
        ThreadInfo threadInfo = new ThreadInfo();
        threadInfo.start();

        EventLoopGroup group = new NioEventLoopGroup();
        ClientSyn.getInstance().init(group);

        HttpRequest request = createRequest();
        System.out.println("send 1");
        HttpResponse response = ClientSyn.getInstance().sendAndGet("localhost", "8081", request);
        System.out.println(response.toString());

        System.out.println("send 2");
        response = ClientSyn.getInstance().sendAndGet("localhost", "8081", request);
        System.out.println(response.toString());

        System.out.println("send 3");
        response = ClientSyn.getInstance().sendAndGet("localhost", "8443", request);
        System.out.println(response.toString());
    }

    private HttpRequest createRequest() throws URISyntaxException {
        URI uri = new URI(URL);
        String host = "localhost";
        HttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath(), Unpooled.EMPTY_BUFFER);
        request.headers().set(HttpHeaderNames.HOST, host);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

        // Set some example cookies.
        request.headers().set(
                HttpHeaderNames.COOKIE,
                io.netty.handler.codec.http.cookie.ClientCookieEncoder.STRICT.encode(
                        new io.netty.handler.codec.http.cookie.DefaultCookie("my-cookie", "foo"),
                        new DefaultCookie("another-cookie", "bar")));
        return request;
    }
}

