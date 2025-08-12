package com.kawasaki.transmission;

import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;

import java.util.concurrent.Future;

public interface RpcClient {
    Future<RpcResp<?>> sendReq(RpcReq rpcReq);
}
