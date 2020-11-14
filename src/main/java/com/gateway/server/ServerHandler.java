package com.gateway.server;

import com.gateway.client.ClientCenter;
import com.gateway.filter.Filter;
import com.gateway.route.RouteTable;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.net.URI;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

/**
 * @author lw
 */
public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
//        System.out.println("server receive request");
        // 路由转发处理,负载均衡
        String source = request.uri();
        String target = RouteTable.getTargetUrl(source);
        URI uri = new URI(target);
        String address = uri.getHost();
        String port = String.valueOf(uri.getPort());
        String url = uri.getPath() + "/";
        request.setUri(url);

        // 请求过滤处理
        Filter.requestProcess(request);

        request.retain();
        FullHttpResponse response = ClientCenter.getInstance().sendAndGet(address, port, request);
//        System.out.println("server get response::" + response.toString());

        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            if (!request.protocolVersion().isKeepAliveDefault()) {
                response.headers().set(CONNECTION, KEEP_ALIVE);
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
        }

        // 相应过滤处理
        Filter.responseProcess(response);

        // 返回Response数据给用户
        response.retain();
        ctx.channel().writeAndFlush(response).addListeners((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                ctx.channel().read();
            } else {
                future.channel().close();
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
