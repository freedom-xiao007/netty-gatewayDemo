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
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * @author lw
 */
public class ClientSynHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private volatile FullHttpResponse response = null;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        lock.lock();
        try {
            System.out.println(msg.headers().toString());
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
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            return content.content().toString(CharsetUtil.UTF_8).getBytes();
        }
        return new byte[0];
    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        condition.signal();
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public FullHttpResponse getResponse() {
//        System.out.println("client syn wait return response");
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
//        System.out.println("return response");
        return response;
    }

    private FullHttpResponse showResponse(Object msg) {
        FullHttpResponse fullHttpResponse = null;
        System.out.println("Client::================================");
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            fullHttpResponse = new DefaultFullHttpResponse(response.protocolVersion(), response.status());

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
            fullHttpResponse.replace(content.content());

            if (content instanceof LastHttpContent) {
                System.err.println("} END OF CONTENT");
            }
        }
        return fullHttpResponse;
    }


}
