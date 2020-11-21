package com.demo.gateway.client;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * @author lw
 */
public interface ClientSync {

    /**
     * 任务执行：将请求转发到后台服务器，获得响应后返回
     * @param request 请求
     * @param serverOutbound server outbound
     * @return 想要
     */
    FullHttpResponse execute(FullHttpRequest request, Channel serverOutbound);
}
