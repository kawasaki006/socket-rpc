package com.kawasaki.transmission.netty.client;

import com.kawasaki.dto.RpcResp;
import com.kawasaki.exception.RpcException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRpcReq {
    private static final Map<String, CompletableFuture<RpcResp<?>>> RESP_CF_MAP = new ConcurrentHashMap<>();

    public static void put(String reqId, CompletableFuture<RpcResp<?>> future) {
        RESP_CF_MAP.put(reqId, future);
    }

    public static void complete(RpcResp<?> resp) {
        CompletableFuture<RpcResp<?>> cf = RESP_CF_MAP.remove(resp.getReqId());

        if (Objects.isNull(cf)) {
            throw new RpcException("UnprocessedRpcReq error");
        }

        cf.complete(resp);
    }
}
