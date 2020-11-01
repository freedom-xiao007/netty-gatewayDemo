package com.gateway;

import com.gateway.client.Client;
import com.gateway.server.Server;
import com.gateway.util.Config;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;

/**
 *
 * 程序入口，默认监听在80端口
 * @author lw
 */
public class GateWayApplication {

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Init property file");
        Config.init();

        int port = 90;
//        if (Config.getProperty("port") != null) {
//            port = Integer.parseInt(Config.getProperty("port"));
//        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup serverGroup = new NioEventLoopGroup();
        EventLoopGroup clientGroup = new NioEventLoopGroup();

        try {
//            Client.init(clientGroup);
            Server.run(bossGroup, serverGroup, port);
        } finally {
            bossGroup.shutdownGracefully();
            serverGroup.shutdownGracefully();
            clientGroup.shutdownGracefully();
        }
    }
}
