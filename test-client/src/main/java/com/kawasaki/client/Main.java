package com.kawasaki.client;

import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.transmission.RpcClient;
import com.kawasaki.transmission.socket.client.SocketRpcClient;
import com.kawasaki.util.ThreadPoolUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class Main {
    public static void main(String[] args) {
        RpcClient rpcClient = new SocketRpcClient("127.0.0.1", 8888);

        RpcReq testReq = RpcReq.builder()
                .reqId("1213")
                .interfaceName("com.kawasaki.api.UserService")
                .methodName("getUser")
                .params(new Object[]{1L})
                .paramTypes(new Class[]{Long.class})
                .build();

        ExecutorService executor = ThreadPoolUtils.createIOIntensiveThreadPool("test");
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                RpcResp<?> testResp = rpcClient.sendReq(testReq);
                System.out.println(testResp.getData());
            });
        }
    }
}
