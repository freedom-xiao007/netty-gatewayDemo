package com.gateway.server;

import com.gateway.client.Client;
import com.gateway.filter.Filter;
import com.gateway.route.RouteTable;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

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

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException, ExecutionException {
        if (msg instanceof HttpRequest) {
            // 获取Request，进行过滤器处理
            HttpRequest request = (HttpRequest) msg;
//            System.out.println("Origin Request");
//            System.out.println(request);
            Filter.requestProcess(request);
//            System.out.println("Filter Request");
//            System.out.println(request);

            // 路由转发处理,负载均衡
            String source = request.uri();
            String url = RouteTable.getTargetUrl(source);
//            System.out.println(source + "::" + url);

            // 调用客户端，发送请求到服务器，获取数据
            byte[] content = "Error, can't find server config".getBytes();
            if (url != null) {
                content = Client.getResponse(url);
            }

            // 构造Response
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), OK,
                    Unpooled.wrappedBuffer(content));
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

            // 调用Response过滤处理
//            System.out.println("Origin Response");
//            System.out.println(response);
            Filter.responseProcess(response);
//            System.out.println("Filter Response");
//            System.out.println(response);

            // 返回Response数据给用户
            ctx.channel().writeAndFlush(response).addListeners(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
