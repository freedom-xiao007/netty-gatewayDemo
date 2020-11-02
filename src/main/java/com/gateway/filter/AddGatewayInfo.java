package com.gateway.filter;

import io.netty.handler.codec.http.HttpResponse;

/**
 * @author lw
 */
public class AddGatewayInfo implements ResponseFilter {

    /**
     * 添加Response的Header信息
     * @param response
     */
    @Override
    public void filter(HttpResponse response) {
        response.headers().add("GateWay", "Gateway");
    }
}
