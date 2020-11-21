package com.demo.gateway.client;

import com.demo.gateway.common.CreatResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.net.URISyntaxException;

/**
 * 这里使用并发的等待-通知机制来拿到结果
 * @author lw
 */
public class CustomClientAsyncHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final CustomClientAsync clientAsync;
    private final int messageHashCode;
    private final FullHttpRequest request;

    public CustomClientAsyncHandler(CustomClientAsync clientAsync, int messageHashCode, FullHttpRequest request) {
        this.clientAsync = clientAsync;
        this.messageHashCode = messageHashCode;
        this.request = request;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws URISyntaxException, InterruptedException {
        FullHttpResponse response = CreatResponse.createResponse(msg);
        clientAsync.returnResponse(response, messageHashCode, request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
