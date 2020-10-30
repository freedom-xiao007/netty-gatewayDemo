package com.gateway.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequest;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lw
 */
public class Client {

    static private EventLoopGroup clientGroup;

    static public void init(EventLoopGroup group) {
        clientGroup = group;
    }

    /**
     * 构建启动客户端，传入请求和server的channel（用户返回结果给用户）
     * @param request 用户请求
     * @param serverChannel
     * @throws InterruptedException
     */
    static public void send(HttpRequest request, Channel serverChannel, String address, int port) throws InterruptedException {
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(clientGroup)
                .channel(NioSocketChannel.class)
                .handler(new ClientInitializer(serverChannel));
        Channel channel = clientBootstrap.connect(address, port).sync().channel();
        channel.writeAndFlush(request);
        channel.closeFuture().sync();
    }

}
