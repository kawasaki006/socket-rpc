package com.kawasaki.transmission.socket.server;

import com.kawasaki.config.RpcServiceConfig;
import com.kawasaki.constant.RpcConstant;
import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.handler.RpcReqHandler;
import com.kawasaki.provider.ServiceProvider;
import com.kawasaki.provider.impl.SimpleServiceProvider;
import com.kawasaki.provider.impl.ZkServiceProvider;
import com.kawasaki.transmission.RpcServer;
import com.kawasaki.util.ThreadPoolUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

// only handles request and response
@Slf4j
public class SocketRpcServer implements RpcServer {
    private final int port;
    private final RpcReqHandler rpcReqHandler;
    private final ServiceProvider serviceProvider;
    private final ExecutorService executor;

    public SocketRpcServer() {this(RpcConstant.SERVER_PORT);}

    public SocketRpcServer(int port) {
        this(port, SingletonFactory.getInstance(ZkServiceProvider.class));
    }

    public SocketRpcServer(int port, ServiceProvider serviceProvider) {
        this.port = port;
        this.serviceProvider = serviceProvider;
        this.rpcReqHandler = new RpcReqHandler(serviceProvider);
        this.executor = ThreadPoolUtils.createIOIntensiveThreadPool("socket-rpc-server-");
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            log.info("Server started on port {}", this.port);

            Socket socket;
            while((socket = serverSocket.accept()) != null) {
                // everytime server receives a req from socket, start executing with thread pool
                executor.submit(new SocketReqHandler(socket, rpcReqHandler));
            }
        } catch (Exception e) {
            log.error("Server exception", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void publishService(RpcServiceConfig config) {
        serviceProvider.publishServices(config);
    }
}
