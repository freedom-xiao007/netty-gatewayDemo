package com.gateway.server;

import com.gateway.client.Client;
import com.gateway.util.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.IOException;


/**
 * 程序入口，默认监听在80端口
 */
public class Server {


    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Init property file");
        Config.init();

        int port = 80;
        if (Config.getProperty("port") != null) {
            port = Integer.parseInt(Config.getProperty("port"));
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup serverGroup = new NioEventLoopGroup();
        EventLoopGroup clientGroup = new NioEventLoopGroup();

        try {
            Client.init(clientGroup);

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, serverGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerInitializer());


            Channel channel = serverBootstrap.bind(port).sync().channel();
            System.out.println("Gateway lister on port: " + port);
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            serverGroup.shutdownGracefully();
            clientGroup.shutdownGracefully();
        }
    }
}
