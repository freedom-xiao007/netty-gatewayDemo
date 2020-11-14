package com.gateway;

import com.gateway.Util.ThreadInfo;
import com.gateway.client.ClientCenter;
import com.gateway.client.ClientSyn;
import com.gateway.filter.Filter;
import com.gateway.route.RouteTable;
import com.gateway.server.Server;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 *
 * 程序入口，默认监听在80端口
 * @author lw
 */
public class GateWayApplication {

    public static void main(String[] args) throws InterruptedException {
        ThreadInfo threadInfo = new ThreadInfo();
        threadInfo.start();

        // 初始化监听端口
        int port = 81;

        // 初始化路由配置
        RouteTable.initTable();

        // 初始化请求和返回的过滤器
        Filter.initRequestFilter();
        Filter.initResponseFilter();

        // 初始化Server,Client 这里对线程池进行统一关闭
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup serverGroup = new NioEventLoopGroup();
        EventLoopGroup clientGroup = new NioEventLoopGroup();

        try {
            // 使用第三方客户端
//            ClientCenter.getInstance().init();

            // 使用自写同步非阻塞客户端
            ClientCenter.getInstance().init(clientGroup);

            Server.run(bossGroup, serverGroup, port);
        } finally {
            bossGroup.shutdownGracefully();
            serverGroup.shutdownGracefully();
        }
    }
}
