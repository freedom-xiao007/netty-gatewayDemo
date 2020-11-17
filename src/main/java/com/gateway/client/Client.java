package com.gateway.client;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;


/**
 * 客户端接口
 * @author lw
 */
public interface Client {

    FullHttpResponse execute(FullHttpRequest request, String address, int port, Channel serverOutbound);
}
