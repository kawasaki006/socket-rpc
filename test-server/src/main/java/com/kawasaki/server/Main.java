package com.kawasaki.server;

import com.kawasaki.config.RpcServiceConfig;
import com.kawasaki.server.service.UserServiceImpl;
import com.kawasaki.transmission.RpcServer;
import com.kawasaki.transmission.netty.server.NettyRpcServer;

public class Main {
    public static void main(String[] args) {
        RpcServiceConfig testConfig = new RpcServiceConfig(new UserServiceImpl());

        RpcServer rpcServer = new NettyRpcServer();
        rpcServer.publishService(testConfig);

        rpcServer.start();
    }
}
