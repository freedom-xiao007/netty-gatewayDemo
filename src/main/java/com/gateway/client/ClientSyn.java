package com.gateway.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 自己实现的同步阻塞客户端，性能勉强达到要求
 * @author lw
 */
public class ClientSyn implements Client{

    private EventLoopGroup clientGroup;
    /**
     * 使用Map来保存用过的Channel，看下次相同的后台服务是否能够重用，起一个类似缓存的作用
     */
    private ConcurrentHashMap<String, Channel> channelPool = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ClientSynHandler> handlerPool = new ConcurrentHashMap<>();

    ClientSyn(EventLoopGroup clientGroup) {
        this.clientGroup = clientGroup;
    }

    @Override
    public FullHttpResponse sendAndGet(String address, String port, HttpRequest request) {
        try {
            return getResponse(address, port, request);
        } catch (Exception e) {
            System.out.println("Error, get response");
        }
        return null;
    }

    private FullHttpResponse getResponse(String address, String port, HttpRequest request) throws InterruptedException {
        String server = address + "::" + port;

        // 查看缓存池中是否有可重用的channel
        if (channelPool.containsKey(server)) {
            Channel channel = channelPool.get(server);
            if (!channel.isActive() || !channel.isWritable() || !channel.isOpen()) {
                System.out.println("Channel can't reuse");
            } else {
                try {
                    channel.writeAndFlush(request).sync();
                    return handlerPool.get(server).getResponse();
                } catch (Exception e) {
                    System.out.println("channel reuse send msg failed!");
                    channel.close();
                    channelPool.remove(server);
                    handlerPool.remove(server);
                }
            }
        }

        // 没有或者不可用则新建
        // 并将最终的handler添加到pipeline中，拿到结果后返回
        ClientSynHandler handler = new ClientSynHandler();
        handlerPool.put(server, handler);
        Channel channel = getNewChannel(address, port, server);
        channel.pipeline().addLast("clientHandler", handler);

//        System.out.println("client send request");
        channel.writeAndFlush(request).sync();
        return handler.getResponse();
    }

    private Channel getNewChannel(String address, String port, String server) throws InterruptedException {
        Channel channel = createChannel(address, port);
        channelPool.put(server, channel);
        return channel;
    }

    /**
     * 返回新的Channel
     * @param address ip地址
     * @param port 端口
     * @return channel
     * @throws InterruptedException exception
     */
    private Channel createChannel(String address, String port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
//                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ClientSynInitializer());
        return bootstrap.connect(address, Integer.parseInt(port)).sync().channel();
    }
}
