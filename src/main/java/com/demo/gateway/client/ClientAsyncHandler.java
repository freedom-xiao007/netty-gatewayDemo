package com.demo.gateway.client;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 暂时不用，全链路异步的客户端目前还实现不了
 * 全链路异步，需要后台服务返回这边请求时配置的唯一id
 * @author lw
 */
@Deprecated
public class ClientAsyncHandler extends ChannelInboundHandlerAdapter {

    private Channel serverChannel;
    private volatile HttpResponse httpResponse;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public ClientAsyncHandler(Channel serverChannel, HttpRequest request) {
        this.serverChannel = serverChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        showResponse(msg);
        serverChannel.writeAndFlush(msg).addListeners(new ChannelFutureListener() {
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

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        serverChannel.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void showResponse(Object msg) {
        System.out.println("Client::================================");
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            System.err.println("STATUS: " + response.status());
            System.err.println("VERSION: " + response.protocolVersion());
            System.err.println();

            if (!response.headers().isEmpty()) {
                for (CharSequence name: response.headers().names()) {
                    for (CharSequence value: response.headers().getAll(name)) {
                        System.err.println("HEADER: " + name + " = " + value);
                    }
                }
                System.err.println();
            }

            if (HttpUtil.isTransferEncodingChunked(response)) {
                System.err.println("CHUNKED CONTENT {");
            } else {
                System.err.println("CONTENT {");
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;

            System.err.print(content.content().toString(CharsetUtil.UTF_8));
            System.err.flush();

            if (content instanceof LastHttpContent) {
                System.err.println("} END OF CONTENT");
            }
        }
    }
}
