package com.gateway.server;

import com.gateway.client.Client;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpRequest;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            System.out.println("Server::==========================");
            System.out.println(request.toString());
            System.out.println("method:" + request.method());
            System.out.println("uri :" + request.uri());

//            try {
//                Client.send(request, ctx.channel());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

//            final Channel outboundChannel = ctx.channel();
//            Bootstrap b = new Bootstrap();
//            b.group(outboundChannel.eventLoop())
//                    .channel(ctx.channel().getClass())
//                    .handler(new ClientInitializer(outboundChannel));
//            Channel channel = b.connect("localhost", 8080).channel();
//            channel.writeAndFlush(request);
//            channel.closeFuture();

            Client.send(request, ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
