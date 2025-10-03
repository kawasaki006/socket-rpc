package com.kawasaki.lb;

import com.kawasaki.dto.RpcReq;

import java.util.List;

public interface LoadBalance {
    public String select(List<String> list, RpcReq rpcReq);
}
