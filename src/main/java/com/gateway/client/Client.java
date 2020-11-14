package com.gateway.client;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

/**
 * 客户端接口
 * @author lw
 */
public interface Client {

    /**
     * 发送请求到后台服务器，并经结果返回
     * @param address ip地址
     * @param port 端口
     * @param request 请求
     * @return response 后端服务器响应
     */
    FullHttpResponse sendAndGet(String address, String port, HttpRequest request);
}
