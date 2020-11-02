package com.gateway;

import com.gateway.client.Client;
import com.gateway.filter.Filter;
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

        // 初始化监听端口
        int port = 80;
        if (Config.getProperty("port") != null) {
            port = Integer.parseInt(Config.getProperty("port"));
        }

        // 初始化请求和返回的过滤器
        Filter.initRequestFilter();
        Filter.initResponseFilter();

        // 初始化Server
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup serverGroup = new NioEventLoopGroup();

        try {
            Server.run(bossGroup, serverGroup, port);
        } finally {
            bossGroup.shutdownGracefully();
            serverGroup.shutdownGracefully();
        }
    }
}
