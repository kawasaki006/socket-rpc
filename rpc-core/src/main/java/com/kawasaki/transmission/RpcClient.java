package com.kawasaki.transmission;

import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;

public interface RpcClient {
    RpcResp<?> sendReq(RpcReq rpcReq);
}
