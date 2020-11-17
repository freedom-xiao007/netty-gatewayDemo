package com.gateway.server;

import com.gateway.client.ClientCenter;
import com.gateway.common.CreateRequest;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

/**
 * @author lw
 */
public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 传入server outbound,在客户端中执行获取结果后，直接写回
        ClientCenter.getInstance().execute(CreateRequest.create(request), ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
