package com.demo.gateway.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author lw
 */
@Deprecated
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    private Channel serverChannel;
    private HttpRequest request;

    public ClientInitializer(Channel serverChannel, HttpRequest request) {
        this.serverChannel = serverChannel;
        this.request = request;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpContentDecompressor());
//        pipeline.addLast(new LoggingHandler(LogLevel.ERROR));
        pipeline.addLast(new ClientHandler(serverChannel, request));
    }
}
