package com.gateway.filter;

import io.netty.handler.codec.http.HttpRequest;

/**
 * @author lw
 */
public interface RequestFilter {

    /**
     * 对请求进行处理
     * @param request 请求
     */
    void filter(HttpRequest request);
}
