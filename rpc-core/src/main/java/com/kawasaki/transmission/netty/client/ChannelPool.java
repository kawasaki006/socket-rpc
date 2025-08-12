package com.kawasaki.transmission.netty.client;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ChannelPool {
    private final Map<String, Channel> pool = new ConcurrentHashMap<>();

    public Channel get(InetSocketAddress address, Supplier<Channel> supplier) {
        String addressStr = address.toString();

        Channel channel = pool.get(addressStr);
        if (channel != null && channel.isActive()) {
            return channel;
        }

        Channel newChannel = supplier.get();
        pool.put(addressStr, newChannel);
        return newChannel;
    }
}
