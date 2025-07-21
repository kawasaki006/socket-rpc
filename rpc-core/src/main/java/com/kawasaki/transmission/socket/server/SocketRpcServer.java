package com.kawasaki.transmission.socket.server;

import com.kawasaki.config.RpcServiceConfig;
import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.handler.RpcReqHandler;
import com.kawasaki.provider.ServiceProvider;
import com.kawasaki.provider.impl.SimpleServiceProvider;
import com.kawasaki.transmission.RpcServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

// only handles request and response
@Slf4j
public class SocketRpcServer implements RpcServer {
    private final int port;
    private final RpcReqHandler rpcReqHandler;
    private final ServiceProvider serviceProvider;

    public SocketRpcServer(int port) {
        this(port, new SimpleServiceProvider());
    }

    public SocketRpcServer(int port, ServiceProvider serviceProvider) {
        this.port = port;
        this.serviceProvider = serviceProvider;
        this.rpcReqHandler = new RpcReqHandler(serviceProvider);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            log.info("Server started on port {}", this.port);

            Socket socket;
            while((socket = serverSocket.accept()) != null) {
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                Object o = objectInputStream.readObject();
                RpcReq rpcReq = (RpcReq) o;

                System.out.println(rpcReq);

                // Call method specified by the rpc request
                Object data = rpcReqHandler.invoke(rpcReq);

                RpcResp<?> rpcResp = RpcResp.success(rpcReq.getReqId(), data);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(rpcResp);
                objectOutputStream.flush();
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
