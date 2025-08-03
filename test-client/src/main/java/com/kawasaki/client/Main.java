package com.kawasaki.client;

import com.kawasaki.api.User;
import com.kawasaki.api.UserService;
import com.kawasaki.client.utils.ProxyUtils;
import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.proxy.RpcClientProxy;
import com.kawasaki.transmission.RpcClient;
import com.kawasaki.transmission.socket.client.SocketRpcClient;
import com.kawasaki.util.ThreadPoolUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class Main {
    public static void main(String[] args) {
        UserService userService = ProxyUtils.getProxy(UserService.class);
        User user = userService.getUser(1L);
        System.out.println(user);
    }
}
