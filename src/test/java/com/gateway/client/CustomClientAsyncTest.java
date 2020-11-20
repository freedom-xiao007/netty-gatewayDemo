package com.gateway.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class CustomClientAsyncTest {

    private final String URL = System.getProperty("url", "http://www.baidu.com/");

    /**
     * 检测客户端是否能获取响应
     * @throws URISyntaxException exception
     */
    @Test
    public void test() throws URISyntaxException {
        String address = "www.baidu.com";
        int port = 80;
        CustomClientAsync clientAsync = new CustomClientAsync(new NioEventLoopGroup());
        FullHttpResponse response = clientAsync.execute((FullHttpRequest) createRequest(address), address, port
                , new NioSocketChannel());
        System.out.println(response.toString());
        assert "HTTP/1.1".equals(response.protocolVersion().toString());
    }

    private HttpRequest createRequest(String address) throws URISyntaxException {
        URI uri = new URI(URL);
        HttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath(), Unpooled.EMPTY_BUFFER);
        request.headers().set(HttpHeaderNames.HOST, address);
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
