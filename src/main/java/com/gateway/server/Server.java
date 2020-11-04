package com.gateway.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * @author lw
 */
public class Server {

    static public void run(EventLoopGroup bossGroup, EventLoopGroup serverGroup, int port) throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, serverGroup)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .channel(NioServerSocketChannel.class)
//                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ServerInitializer());

        Channel channel = serverBootstrap.bind(port).sync().channel();
        System.out.println("Gateway lister on port: " + port);
        channel.closeFuture().sync();
    }
}
