package com.demo.gateway.filter;

import com.demo.gateway.common.CreatResponse;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Test;

public class AddGatewayInfoTest {

    @Test
    public void testAddHeader() {
        Filter.initResponseFilter();
        FullHttpResponse response = CreatResponse.creat404(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/"));
        Filter.responseProcess(response);
        System.out.println(response.toString());
        assert "GateWay".equals(response.headers().get("GateWay"));
    }
}
