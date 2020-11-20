package com.demo.gateway;

import com.demo.gateway.client.ClientCenter;
import com.demo.gateway.common.ThreadInfo;
import com.demo.gateway.filter.Filter;
import com.demo.gateway.route.RouteTable;
import com.demo.gateway.server.Server;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ThreadFactory;

import static com.demo.gateway.common.Constant.CUSTOM_CLIENT_ASYNC;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(DemoApplication.class, args);

		ThreadInfo threadInfo = new ThreadInfo();
		threadInfo.start();

		// init listen port
		int port = 81;

		// init route table config
		RouteTable.initTable();

		// 初始化请求和返回的过滤器
		Filter.initRequestFilter();
		Filter.initResponseFilter();

		// 初始化Server,Client 这里对线程池进行统一关闭
		ThreadFactory serverBoos = new ThreadFactoryBuilder().setNameFormat("server boos-%d").build();
		ThreadFactory serverWork = new ThreadFactoryBuilder().setNameFormat("server work-%d").build();
		ThreadFactory clientWork = new ThreadFactoryBuilder().setNameFormat("client work-%d").build();

		EventLoopGroup bossGroup = new NioEventLoopGroup(1, serverBoos);
		EventLoopGroup serverGroup = new NioEventLoopGroup(serverWork);
		EventLoopGroup clientGroup = new NioEventLoopGroup(clientWork);

		try {
			// 使用自定义第三方客户端
			ClientCenter.getInstance().init(CUSTOM_CLIENT_ASYNC, clientGroup);

			// 使用第三方客户端
//            ClientCenter.getInstance().init(THIRD_CLIENT_ASYNC, clientGroup);

			Server.run(bossGroup, serverGroup, port);
		} finally {
			bossGroup.shutdownGracefully();
			serverGroup.shutdownGracefully();
			clientGroup.shutdownGracefully();
		}
	}

}
