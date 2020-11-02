package com.gateway.filter;

import io.netty.handler.codec.http.HttpRequest;

/**
 * @author lw
 */
public interface RequestFilter {

    /**
     * 对请求进行处理
     * @param request
     */
    public void filter(HttpRequest request);
}
