package com.demo.gateway.server;

import com.demo.gateway.client.CustomClientSync;
import com.demo.gateway.common.CreatResponse;
import com.demo.gateway.common.CreateRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 同步非阻塞服务端handler，配置同步非阻塞客户端进行使用{CustomClientSync}
 * @author lw
 */
public class ServerSyncHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(ServerSyncHandler.class);

    private final CustomClientSync client;

    ServerSyncHandler(CustomClientSync clientAsync) {
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
