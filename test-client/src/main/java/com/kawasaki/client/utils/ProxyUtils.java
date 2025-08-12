package com.kawasaki.client.utils;

import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.proxy.RpcClientProxy;
import com.kawasaki.transmission.RpcClient;
import com.kawasaki.transmission.netty.client.NettyRpcClient;
import com.kawasaki.transmission.socket.client.SocketRpcClient;

public class ProxyUtils {
    private static final RpcClient rpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    private static final RpcClientProxy proxy = new RpcClientProxy(rpcClient);

    public static <T> T getProxy(Class<T> clazz) {
        return proxy.getProxy(clazz);
    }
}
