package com.gateway.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;

/**
 * 这里使用并发的等待-通知机制来拿到结果
 * @author lw
 */
public class ClientSynHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private volatile FullHttpResponse response = null;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        lock.lock();
        try {
            response = new DefaultFullHttpResponse(msg.protocolVersion(), msg.status(),
                    Unpooled.wrappedBuffer(getByteBuf(msg)));
            response.headers()
                    .set(CONTENT_TYPE, TEXT_PLAIN)
                    .setInt(CONTENT_LENGTH, response.content().readableBytes());
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    private byte[] getByteBuf(FullHttpResponse msg) {
        if (msg != null) {
            return msg.content().toString(CharsetUtil.UTF_8).getBytes();
        }
        return new byte[0];
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public FullHttpResponse getResponse() {
        lock.lock();
        try {
            while (response == null) {
                condition.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
//        System.out.println("client get response");
        return response;
    }
}
