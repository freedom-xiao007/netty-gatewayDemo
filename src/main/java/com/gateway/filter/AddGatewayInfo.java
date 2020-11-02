package com.gateway.filter;

import io.netty.handler.codec.http.HttpResponse;

/**
 * @author lw
 */
public class AddGatewayInfo implements ResponseFilter {
    @Override
    public void filter(HttpResponse response) {
        response.headers().add("GateWay", "Gateway");
    }
}
