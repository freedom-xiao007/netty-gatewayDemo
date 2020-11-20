package com.demo.gateway.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 自己实现的同步阻塞客户端，性能勉强达到要求
 * @author lw
 */
public class CustomClientAsync implements Client {

    private static final Logger logger = LoggerFactory.getLogger(CustomClientAsync.class);

    /**
     * 使用Map来保存用过的Channel，看下次相同的后台服务是否能够重用，起一个类似缓存的作用
     */
    private ConcurrentHashMap<Channel, Channel> channelPool = new ConcurrentHashMap<>();
    private EventLoopGroup clientGroup;

    CustomClientAsync(EventLoopGroup clientGroup) {
        this.clientGroup = clientGroup;
    }


    /**
     * 调用channel发送请求，从handler中获取响应结果
     * @param address 服务器ip
     * @param port 服务器端口
     * @param request 请求
     * @param serverChannel server outbound
     * @return 响应
     * @throws InterruptedException exception
     */
    private FullHttpResponse getResponse(String address, int port, FullHttpRequest request,
                                         Channel serverChannel) throws InterruptedException {
        // 查看缓存池中是否有可重用的channel
        if (channelPool.containsKey(serverChannel)) {
            Channel channel = channelPool.get(serverChannel);
            if (!channel.isActive() || !channel.isWritable() || !channel.isOpen()) {
                logger.debug("Channel can't reuse");
            } else {
                try {
                    channel.pipeline().removeLast();
                    CustomClientAsyncHandler handler = new CustomClientAsyncHandler();
                    handler.setLatch(new CountDownLatch(1));
                    channel.pipeline().addLast("clientHandler", handler);
                    channel.writeAndFlush(request.retain()).sync();
                    return handler.getResponse();
                } catch (Exception e) {
                    logger.debug("channel reuse send msg failed!");
                    channel.close();
                    channelPool.remove(serverChannel);
                }
                logger.debug("Handler is busy, please user new channel");
            }
        }

        // 没有或者不可用则新建
        // 并将最终的handler添加到pipeline中，拿到结果后返回
        CustomClientAsyncHandler handler = new CustomClientAsyncHandler();
        handler.setLatch(new CountDownLatch(1));
        Channel channel = createChannel(address, port);
        channel.pipeline().addLast("clientHandler", handler);
        channelPool.put(serverChannel, channel);

        channel.writeAndFlush(request).sync();
        return handler.getResponse();
    }

    /**
     * 返回新的Channel
     * @param address ip地址
     * @param port 端口
     * @return channel
     * @throws InterruptedException exception
     */
    private Channel createChannel(String address, int port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .handler(new CustomClientAsyncInitializer());
        return bootstrap.connect(address, port).sync().channel();
    }

    @Override
    public FullHttpResponse execute(FullHttpRequest request, String address, int port, Channel serverOutbound) {
        try {
            return getResponse(address, port, request, serverOutbound);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
