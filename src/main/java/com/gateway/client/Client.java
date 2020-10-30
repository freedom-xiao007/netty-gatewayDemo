package com.gateway.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequest;

import java.util.concurrent.ConcurrentHashMap;

public class Client {

    static private EventLoopGroup clientGroup;

    static public void init(EventLoopGroup group) {
        clientGroup = group;
    }

    static public void send(HttpRequest request, Channel serverChannel) throws InterruptedException {
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(clientGroup)
                .channel(NioSocketChannel.class)
                .handler(new ClientInitializer(serverChannel));
        Channel channel = clientBootstrap.connect("localhost", 8080).sync().channel();
        channel.writeAndFlush(request);
        channel.closeFuture().sync();
    }

}
