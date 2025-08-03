package com.kawasaki.server;

import com.kawasaki.api.User;
import com.kawasaki.api.UserService;
import com.kawasaki.config.RpcServiceConfig;
import com.kawasaki.proxy.RpcClientProxy;
import com.kawasaki.server.service.UserServiceImpl;
import com.kawasaki.transmission.RpcServer;
import com.kawasaki.transmission.socket.server.SocketRpcServer;

public class Main {
    public static void main(String[] args) {
        RpcServiceConfig testConfig = new RpcServiceConfig(new UserServiceImpl());

        RpcServer rpcServer = new SocketRpcServer();
        rpcServer.publishService(testConfig);

        rpcServer.start();
    }
}
