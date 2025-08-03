package com.kawasaki.registry;

import com.kawasaki.dto.RpcReq;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {
    InetSocketAddress findService(RpcReq rpcReq);
}
