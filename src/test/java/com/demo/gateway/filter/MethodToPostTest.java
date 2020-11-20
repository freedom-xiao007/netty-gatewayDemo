package com.demo.gateway.filter;

import com.demo.gateway.common.CreateRequest;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Test;

public class MethodToPostTest {

    @Test
    public void MethodToPostTest() {
        Filter.initRequestFilter();
        FullHttpRequest request = CreateRequest.create(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));
        Filter.requestProcess(request);
        System.out.println(request.toString());
        assert request.method() == HttpMethod.POST;
    }
}
