package com.kawasaki.transmission.socket.server;

import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.handler.RpcReqHandler;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
@AllArgsConstructor
public class SocketReqHandler implements Runnable{
    private final Socket socket;
    private final RpcReqHandler rpcReqHandler;

    @SneakyThrows
    @Override
    public void run() {
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        RpcReq rpcReq = (RpcReq) objectInputStream.readObject();

        // Call method specified by the rpc request
        Object data = rpcReqHandler.invoke(rpcReq);

        RpcResp<?> rpcResp = RpcResp.success(rpcReq.getReqId(), data); // create resp object
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(rpcResp);
        objectOutputStream.flush();
    }
}
