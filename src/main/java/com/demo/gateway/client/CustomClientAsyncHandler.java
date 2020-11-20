package com.demo.gateway.client;

import com.demo.gateway.common.CreatResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.concurrent.CountDownLatch;

/**
 * 这里使用并发的等待-通知机制来拿到结果
 * @author lw
 */
public class CustomClientAsyncHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private CountDownLatch latch;
    private FullHttpResponse response;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        // 拿到结果后再释放锁
        response = CreatResponse.createResponse(msg);
        latch.countDown();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 锁的初始化
     * @param latch CountDownLatch
     */
    void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    /**
     * 阻塞等待结果后返回
     * @return 后台服务器响应
     * @throws InterruptedException
     */
    public FullHttpResponse getResponse() throws InterruptedException {
        latch.await();
        return response;
    }
}
