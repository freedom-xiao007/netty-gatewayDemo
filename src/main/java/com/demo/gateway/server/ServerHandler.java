package com.demo.gateway.server;

import com.demo.gateway.client.ClientCenter;
import com.demo.gateway.client.CustomClientAsync;
import com.demo.gateway.common.CreatResponse;
import com.demo.gateway.common.CreateRequest;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lw
 */
public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final CustomClientAsync client;

    ServerHandler(CustomClientAsync clientAsync) {
        this.client = clientAsync;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 传入server outbound,在客户端中执行获取结果后，直接写回
        FullHttpResponse response = client.execute(CreateRequest.create(request), ctx.channel());
        if (response == null) {
            logger.error("backend server return null");
            ctx.channel().writeAndFlush(CreatResponse.creat404(request));
        } else {
            ctx.channel().writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
