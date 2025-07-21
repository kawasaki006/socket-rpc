package com.kawasaki.transmission.socket.client;

import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.transmission.RpcClient;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
public class SocketRpcClient implements RpcClient {
    private final String host;
    private final int port;

    public SocketRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcResp<?> sendReq(RpcReq rpcReq) {
        try (Socket socket = new Socket(this.host, this.port)) {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(rpcReq);
            outputStream.flush();

            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            Object o = inputStream.readObject();
            return (RpcResp<?>) o;
        } catch (Exception e) {
            log.error("Fail to send rpc request", e);
            throw new RuntimeException(e);
        }
    }
}
