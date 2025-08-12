package com.kawasaki.transmission.socket.client;

import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.registry.ServiceDiscovery;
import com.kawasaki.registry.impl.ZkServiceDiscovery;
import com.kawasaki.transmission.RpcClient;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
public class SocketRpcClient implements RpcClient {
    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient() {
        this(SingletonFactory.getInstance(ZkServiceDiscovery.class));
    }

    public SocketRpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public Future<RpcResp<?>> sendReq(RpcReq rpcReq) {
        InetSocketAddress address = serviceDiscovery.findService(rpcReq);

        try (Socket socket = new Socket(address.getAddress(), address.getPort())) {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(rpcReq);
            outputStream.flush();

            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            Object o = inputStream.readObject();
            return CompletableFuture.completedFuture((RpcResp<?>) o);
        } catch (Exception e) {
            log.error("Fail to send rpc request", e);
            throw new RuntimeException(e);
        }
    }
}
