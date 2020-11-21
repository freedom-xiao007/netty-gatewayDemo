package com.demo.gateway.server;

import com.demo.gateway.jms.MessageCenter;
import com.demo.gateway.jms.RequestProducerController;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.util.HashMap;
import java.util.Map;

import static com.demo.gateway.common.Constant.*;

/**
 * 异步非阻塞，配置异步非阻塞客户端进行使用{CustomClientAsync}
 * @author lw
 */
public class ServerAsyncHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final RequestProducerController producer;

    ServerAsyncHandler(RequestProducerController producer) {
        this.producer = producer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        Map<String, String> message = new HashMap<>(4);
        message.put(REQUEST_METHOD, request.method().toString());
        message.put(REQUEST_URI, request.uri());
        message.put(REQUEST_VERSION, request.protocolVersion().toString());
        message.put(CHANNEL_HASH_CODE, String.valueOf(ctx.channel().hashCode()));
        MessageCenter.add(ctx.channel().hashCode(), ctx.channel());
        producer.sendMessage(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
