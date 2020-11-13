package com.gateway.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import static org.asynchttpclient.Dsl.asyncHttpClient;


/**
 * @author lw
 */
public class ClientSyn {

    private enum EnumSingleton {
        /**
         * 懒汉枚举单例
         */
        INSTANCE;
        private ClientSyn instance = null;

        private EnumSingleton(){
            instance = new ClientSyn();
        }
        public ClientSyn getSingleton(){
            return instance;
        }
    }

    public static ClientSyn getInstance(){
        return EnumSingleton.INSTANCE.getSingleton();
    }

    private Bootstrap client = new Bootstrap();
//    private ClientSynHandler handler = new ClientSynHandler();
    private Map<String, Channel> channelPool = new HashMap<>();
    private Map<String, ClientSynHandler> handlerPool = new HashMap<>();

    public void init(EventLoopGroup clientGroup) {
        client.group(clientGroup)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
//                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ClientSynInitializer());
    }

//    public FullHttpResponse sendAndGet(String address, String port, HttpRequest request) throws InterruptedException {
////        Channel channel = client.connect("localhost", 8081).sync().channel();
//        ClientSynHandler handler = null;
//        String server = address + "::" + port;
//        Channel channel = getExistChannel(address, port, server);
//        if (channel != null) {
////            channel.pipeline().removeLast();
//            handler = (ClientSynHandler) channel.pipeline().get("clientHandler");
//        }
//
//        if (channel == null) {
//            channel = getNewChannel(address, port, server);
//            handler = new ClientSynHandler();
//            channel.pipeline().addLast("clientHandler", handler);
//        }
//
//        channel.writeAndFlush(request).sync();
//        FullHttpResponse response = handler.getResponse();
//        System.out.println("Response connection status: " + response.headers().get("connection"));
//        if ("close".equals(response.headers().get("connection"))) {
//            System.out.println("close channel");
//            channelPool.remove(address + "::" + port);
//        }
//        return response;
//    }

    public FullHttpResponse sendAndGet(String address, String port, HttpRequest request) throws InterruptedException {
        String server = address + "::" + port;
        if (channelPool.containsKey(server)) {
            Channel channel = channelPool.get(server);
            if (!channel.isActive() || !channel.isWritable() || !channel.isOpen()) {
                System.out.println("Channel can't reuse");
            } else {
                try {
                    channel.writeAndFlush(request).sync();
                    return handlerPool.get(server).getResponse();
                } catch (Exception e) {
                    System.out.println("channel reuse send msg failed!");
                    channel.close();
                    channelPool.remove(server);
                }
            }
        }
        ClientSynHandler handler = new ClientSynHandler();
        handlerPool.put(server, handler);
        Channel channel = getNewChannel(address, port, server);
        channel.pipeline().addLast("clientHandler", handler);
        channel.writeAndFlush(request).sync();
        return handler.getResponse();
    }

    private Channel getExistChannel(String address, String port, String server) {
        if (channelPool.containsKey(server)) {
//            System.out.println("return exist channel");
            return channelPool.get(server);
        }
        return null;
    }

    private Channel getNewChannel(String address, String port, String server) throws InterruptedException {
        Channel channel = createChannel(address, port);
        channelPool.put(server, channel);
        return channel;
    }

    private Channel createChannel(String address, String port) throws InterruptedException {
//        System.out.println("create new channel");
        return client.connect(address, Integer.parseInt(port)).sync().channel();
    }
}
