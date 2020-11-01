package com.gateway.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.asynchttpclient.netty.NettyResponse;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.asynchttpclient.Dsl.asyncHttpClient;


/**
 * @author lw
 */
public class Client {

    static private EventLoopGroup clientGroup;
    static private Bootstrap client;
    static private AsyncHttpClient asyncHttpClient = asyncHttpClient();


    static public void init(EventLoopGroup group) throws InterruptedException {
        clientGroup = group;
        client = new Bootstrap();
        client.group(clientGroup)
                .option(ChannelOption.SO_REUSEADDR, true)
                .channel(NioSocketChannel.class);
    }


    static public void send(HttpRequest request, Channel serverChannel, String address, int port) {
        client.remoteAddress(address, port);
        client.handler(new ClientInitializer(serverChannel, request));
        client.connect();
    }

    static public void getResponse(HttpRequest request, String address, int port) throws InterruptedException {
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup)
                .channel(NioSocketChannel.class)
                .handler(new ClientInitializer(null, null));
        ChannelFuture future = bootstrap.connect(address, port).sync().channel().writeAndFlush(request);
        future.addListeners(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("successful");
                    System.out.println(future.get().toString());
                } else {
                    System.out.println("failed");
                }
            }
        });
    }

    static public byte[] getResponse(String url) throws ExecutionException, InterruptedException {
        ListenableFuture<Response> responseFuture = asyncHttpClient.prepareGet("http://192.168.101.105:8080/").execute();
        Response response = responseFuture.get();

        Charset charset = Charset.forName("ASCII");
        return response.getResponseBody().toString().getBytes(charset);
    }

    static private HttpResponse build() {
        return new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER);
    }
}
