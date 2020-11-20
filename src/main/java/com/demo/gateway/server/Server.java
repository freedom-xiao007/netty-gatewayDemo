package com.demo.gateway.server;

import com.demo.gateway.client.CustomClientAsync;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;


/**
 * 服务端配置启动
 * @author lw
 */
@Component
@Order(2)
public class Server implements ApplicationRunner, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final
    Environment environment;

    @Autowired
    private CustomClientAsync clientAsync;

    private EventLoopGroup bossGroup;
    private EventLoopGroup serverGroup;

    @Value("${server.SO_REUSEADDR}")
    private boolean SO_REUSEADDR;

    @Value("${server.AUTO_CLOSE}")
    private boolean AUTO_CLOSE;

    public Server(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ThreadFactory serverBoos = new ThreadFactoryBuilder().setNameFormat("server boos-%d").build();
        ThreadFactory serverWork = new ThreadFactoryBuilder().setNameFormat("server work-%d").build();

        int bossNumber = Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.boos.threadNumber")));
        int workNumber = Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.work.threadNumber")));

        bossGroup = new NioEventLoopGroup(bossNumber, serverBoos);
        serverGroup = new NioEventLoopGroup(workNumber, serverWork);

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, serverGroup)
                .option(ChannelOption.SO_REUSEADDR, SO_REUSEADDR)
                .option(ChannelOption.AUTO_CLOSE, AUTO_CLOSE)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ServerInitializer(clientAsync));

        int port = Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.port")));
        Channel channel = serverBootstrap.bind(port).sync().channel();
        logger.info("SO_REUSEADDR::" + SO_REUSEADDR + "  AUTO_CLOSE::" + AUTO_CLOSE);
        logger.info("Gateway lister on port: " + port);
        channel.closeFuture().sync();
    }

    @Override
    public void destroy() {
        bossGroup.shutdownGracefully();
        serverGroup.shutdownGracefully();
    }
}
