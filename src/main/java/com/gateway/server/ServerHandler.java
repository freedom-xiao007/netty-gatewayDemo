package com.gateway.server;

import com.gateway.client.Client;
import com.gateway.client.ClientInitializer;
import com.gateway.route.RouteTable;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import jdk.javadoc.internal.doclets.toolkit.taglets.UserTaglet;
import org.asynchttpclient.Response;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * @author lw
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private Channel outboundChannel = null;
    private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel serverOutbound = ctx.channel();
    }

    /**
     * 读取用户请求，调用client，发送请求到目标服务
     * @param ctx
     * @param msg
     * @throws InterruptedException
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException, ExecutionException {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
//            showRequest(request);

            String source = request.uri();
            Map<String, String> target = RouteTable.getTarget(source);
//            System.out.println(target.toString());
            request.setUri(target.get("url"));
            String address = target.get("address");
            int port = Integer.parseInt(target.get("port"));

            String url = "http://" + address + ":" + port + target.get("url");

            boolean keepAlive = HttpUtil.isKeepAlive(request);
            FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), OK,
                    Unpooled.wrappedBuffer(Client.getResponse(url)));
            response.headers()
                    .set(CONTENT_TYPE, TEXT_PLAIN)
                    .setInt(CONTENT_LENGTH, response.content().readableBytes());

            if (keepAlive) {
                if (!request.protocolVersion().isKeepAliveDefault()) {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                }
            } else {
                // Tell the client we're going to close the connection.
                response.headers().set(CONNECTION, CLOSE);
            }

            ctx.channel().writeAndFlush(response);

//            Bootstrap bootstrap = new Bootstrap();
//            bootstrap.group(ctx.channel().eventLoop())
//                    .channel(ctx.channel().getClass())
//                    .handler(new ClientInitializer(ctx.channel(), request));
//            ChannelFuture future = bootstrap.connect(address, port);
//            future.addListener(new ChannelFutureListener() {
//                @Override
//                public void operationComplete(ChannelFuture future) throws Exception {
//                    if (future.isSuccess()) {
//                        future.channel().writeAndFlush(msg);
//                    } else {
//                        ctx.channel().close();
//                    }
//                }
//            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void showRequest(HttpRequest request) {
        System.out.println("Server::==========================");
        System.out.println(request.toString());
        System.out.println("method:" + request.method());
        System.out.println("uri :" + request.uri());
    }
}
