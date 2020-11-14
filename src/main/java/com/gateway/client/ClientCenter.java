package com.gateway.client;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

/**
 * 使用时请进行初始化操作
 * 客户端中心
 * 持有三种客户端：第三方异步非阻塞客户端、自写异步非阻塞客户端、自写同步非阻塞客户端
 * 暂时不支持自写异步非阻塞客户端
 * @author lw
 */
public class ClientCenter {

    private enum EnumSingleton {
        /**
         * 懒汉枚举单例
         */
        INSTANCE;
        private ClientCenter instance;

        EnumSingleton(){
            instance = new ClientCenter();
        }
        public ClientCenter getSingleton(){
            return instance;
        }
    }

    public static ClientCenter getInstance(){
        return EnumSingleton.INSTANCE.getSingleton();
    }

    private Client client;

    public void init() {
        client = new ClientAsync();
    }

    public void init(EventLoopGroup clientGroup) {
        client = new ClientSyn(clientGroup);
    }

    public FullHttpResponse sendAndGet(String address, String port, HttpRequest request) {
        return client.sendAndGet(address, port, request);
    }
}
