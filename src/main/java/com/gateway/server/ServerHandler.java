package com.gateway.server;

import com.gateway.client.Client;
import com.gateway.route.RouteTableSingleton;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Map;

/**
 * @author lw
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * 读取用户请求，调用client，发送请求到目标服务
     * @param ctx
     * @param msg
     * @throws InterruptedException
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
//            System.out.println("Server::==========================");
//            System.out.println(request.toString());
//            System.out.println("method:" + request.method());
//            System.out.println("uri :" + request.uri());

            String source = request.uri();
            Map<String, String> target = RouteTableSingleton.getInstance().getTarget(source);
            System.out.println("route::" + target.toString());
            request.setUri(target.get("url"));
            Client.send(request, ctx.channel(), target.get("address"), Integer.parseInt(target.get("port")));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
