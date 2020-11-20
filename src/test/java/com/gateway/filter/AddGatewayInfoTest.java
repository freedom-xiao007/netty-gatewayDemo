package com.gateway.filter;

import com.gateway.common.CreatResponse;
import io.netty.handler.codec.http.*;
import org.junit.Test;

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
