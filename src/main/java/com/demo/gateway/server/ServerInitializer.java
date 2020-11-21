package com.demo.gateway.server;

import com.demo.gateway.client.CustomClientAsync;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

/**
 * @author lw
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final CustomClientAsync client;

    ServerInitializer(CustomClientAsync clientAsync) {
        this.client = clientAsync;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
        pipeline.addLast("my handler", new ServerHandler(client));
    }
}
