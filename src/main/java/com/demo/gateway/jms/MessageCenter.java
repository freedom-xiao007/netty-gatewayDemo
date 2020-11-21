package com.demo.gateway.jms;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * 持有 server outbound hash code 到 server outbound 的映射
 * @author lw
 */
public class MessageCenter {

    private static final Map<Integer, Channel> MESSAGE_REF_CHANNEL = new HashMap<>();

    public static void add(Integer messageHashCode, Channel channel) {
        MESSAGE_REF_CHANNEL.put(messageHashCode, channel);
    }

    public static Channel getChannel(Integer messageHashCode) {
        return MESSAGE_REF_CHANNEL.getOrDefault(messageHashCode, null);
    }

    public static void remove(Integer messageHashCode) {
        MESSAGE_REF_CHANNEL.remove(messageHashCode);
    }
}
