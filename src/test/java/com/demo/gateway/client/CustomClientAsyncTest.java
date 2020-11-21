package com.demo.gateway.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class CustomClientAsyncTest {

    private CustomClientSync clientAsync = new CustomClientSync();

    private final String URL = System.getProperty("url", "http://www.baidu.com:80/");

    /**
     * 检测客户端是否能获取响应
     * @throws URISyntaxException exception
     */
    @Test
    public void test() throws URISyntaxException {
        FullHttpResponse response = clientAsync.execute((FullHttpRequest) createRequest(), new NioSocketChannel());
        System.out.println(response.toString());
        assert "HTTP/1.1".equals(response.protocolVersion().toString());
        clientAsync.destroy();
    }

    private HttpRequest createRequest() throws URISyntaxException {
        URI uri = new URI(URL);
        HttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, URL, Unpooled.EMPTY_BUFFER);
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        return request;
    }
}
