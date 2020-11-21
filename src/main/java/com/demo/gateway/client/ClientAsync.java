package com.demo.gateway.client;

import io.netty.handler.codec.http.FullHttpRequest;


/**
 * @author lw
 */
public interface ClientAsync {

    /**
     * 发送request到后台服务器
     * @param request 请求
     * @param messageHashCode 请求信息的hashcode
     */
    void sendRequest(FullHttpRequest request, int messageHashCode);
}
