package com.gateway;

import com.gateway.filter.Filter;
import com.gateway.route.RouteTable;
import com.gateway.server.Server;
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
        // 初始化监听端口
        int port = 81;

        // 初始化路由配置
        RouteTable.initTable();

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
