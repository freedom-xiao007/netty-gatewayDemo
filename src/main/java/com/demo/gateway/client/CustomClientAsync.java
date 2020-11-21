package com.demo.gateway.client;

import com.demo.gateway.annotation.RequestFilterAnnotation;
import com.demo.gateway.annotation.ResponseFilterAnnotation;
import com.demo.gateway.annotation.RouteAnnotation;
import com.demo.gateway.common.CreatResponse;
import com.demo.gateway.jms.MessageCenter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自己实现的异步非阻塞阻塞客户端
 * @author lw
 */
@Component
@EnableAspectJAutoProxy(exposeProxy = true)
public class CustomClientAsync extends Client implements DisposableBean, ClientAsync {

    private static final Logger logger = LoggerFactory.getLogger(CustomClientAsync.class);

    private EventLoopGroup clientGroup = new NioEventLoopGroup(new ThreadFactoryBuilder().setNameFormat("client work-%d").build());
    private final int cpuProcessors = Runtime.getRuntime().availableProcessors() * 2;
    /**
     * 空闲Channel缓存池
     */
    private ConcurrentHashMap<String, ArrayBlockingQueue<Channel>> freeChannels = new ConcurrentHashMap<>();
    /**
     * 繁忙Channel持有（繁忙池），起一个
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Channel>> busyChannels = new ConcurrentHashMap<>();

    @Value("${client.SO_REUSEADDR}")
    private boolean soReuseaddr;
    @Value("${client.TCP_NODELAY}")
    private boolean tcpNodelay;
    @Value("${client.AUTO_CLOSE}")
    private boolean autoClose;
    @Value("${client.SO_KEEPALIVE}")
    private boolean soKeepalive;

    CustomClientAsync() {}

    /**
     * 获取 client channel 发送请求
     * 发生错误时需要将 channel 放回缓存池
     * @param request 请求
     */
    @Override
    @RouteAnnotation
    @RequestFilterAnnotation
    public void sendRequest(FullHttpRequest request, int channelHashCode) {
        Channel channel;
        try {
            channel = getChannel(request, channelHashCode);
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            returnResponse(CreatResponse.creat404(request), channelHashCode, request);
            return;
        }

        CustomClientAsyncHandler handler = new CustomClientAsyncHandler(this, channelHashCode, request);
        channel.pipeline().replace("clientHandler", "clientHandler", handler);
        try {
            channel.writeAndFlush(request).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("client channel send failed");
            repayChannel(request, channelHashCode);
        }
    }

    /**
     * 获取Client Channel
     *
     * 一、如果不存在此ip地址和端口的Channel缓存池，直接新建，加入繁忙池（下面将ip地址和端口称为key）
     *
     * 二、存在key的Channel缓存池
     *      1.如果有空闲的Channel，从空闲池中取出，放入繁忙池
     *
     *      2.如果此key的繁忙池中的channel数量小于 CPU核心*2，则新建一个channel，放入繁忙池
     *
     *      3.上面两种情况都不成立，则阻塞一直等待缓冲池中有channel可用时返回，从空闲池中取出，放入繁忙池
     *
     * @param request request
     * @param channelHashCode server outbound hashcode
     * @return channel
     * @throws URISyntaxException exception
     * @throws InterruptedException exception
     */
    private Channel getChannel(FullHttpRequest request, int channelHashCode) throws URISyntaxException, InterruptedException {
        URI uri = new URI(request.uri());
        String key = uri.getHost() + "::" + uri.getPort();

        Channel channel;

        if (!freeChannels.containsKey(key)) {
            channel = createChannel(uri.getHost(), uri.getPort());
        } else {
            if (!freeChannels.get(key).isEmpty()) {
                channel = freeChannels.get(key).take();
            } else if (busyChannels.get(key).size() < cpuProcessors) {
                channel = createChannel(uri.getHost(), uri.getPort());
            } else {
                channel = freeChannels.get(key).take();
            }
        }

        ConcurrentHashMap<Integer, Channel> requestMapChannel = busyChannels.getOrDefault(key,
                new ConcurrentHashMap<>(cpuProcessors));
        requestMapChannel.put(channelHashCode, channel);
        busyChannels.put(key, requestMapChannel);

        return channel;
    }

    /**
     * 归还channel到缓冲池
     * 缓存池添加，繁忙池删除
     * @param request request
     * @param channelHashCode server outbound hashcode
     */
    private void repayChannel(FullHttpRequest request, int channelHashCode) {
        URI uri;
        try {
            uri = new URI(request.uri());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            logger.error("uri parse error:" + request.uri());
            return;
        }

        String key = uri.getHost() + "::" + uri.getPort();
        Channel channel = busyChannels.get(key).get(channelHashCode);

        if (!freeChannels.containsKey(key)) {
            freeChannels.put(key, new ArrayBlockingQueue<>(cpuProcessors));
        }
        freeChannels.get(key).add(channel);

        busyChannels.get(key).remove(channelHashCode);
    }

    /**
     * 返回新的Channel
     * @param address ip地址
     * @param port 端口
     * @return channel
     * @throws InterruptedException exception
     */
    private Channel createChannel(String address, int port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup)
                .option(ChannelOption.SO_REUSEADDR, soReuseaddr)
                .option(ChannelOption.TCP_NODELAY, tcpNodelay)
                .option(ChannelOption.AUTO_CLOSE, autoClose)
                .option(ChannelOption.SO_KEEPALIVE, soKeepalive)
                .channel(NioSocketChannel.class)
                .handler(new CustomClientInitializer());
        return bootstrap.connect(address, port).sync().channel();
    }

    /**
     * 关闭线程池
     */
    @Override
    public void destroy() {
        clientGroup.shutdownGracefully();
    }

    /**
     * 从MessageCenter 中获取对应的 server outbound,返回响应,归还 client channel
     * 接收并返回响应后才将channel放回缓冲池是为了让 client channel一个时间点处理特定的请求即可，避免冲突
     * @param response response
     * @param channelHashCode server outbound hash code
     * @param request request
     */
    @ResponseFilterAnnotation
    void returnResponse(FullHttpResponse response, int channelHashCode, FullHttpRequest request) {
        try {
            MessageCenter.getChannel(channelHashCode).writeAndFlush(response).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("Server inbound can't use");
        }

        repayChannel(request, channelHashCode);
    }
}
