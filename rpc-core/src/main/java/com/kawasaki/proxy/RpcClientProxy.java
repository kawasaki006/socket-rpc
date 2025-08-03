package com.kawasaki.proxy;

import cn.hutool.core.util.IdUtil;
import com.kawasaki.config.RpcServiceConfig;
import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.enums.RpcRespStatusEnum;
import com.kawasaki.exception.RpcException;
import com.kawasaki.transmission.RpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

public class RpcClientProxy implements InvocationHandler {
    private final RpcClient rpcClient;
    private final RpcServiceConfig config;

    public RpcClientProxy(RpcClient rpcClient) {
        this(rpcClient, new RpcServiceConfig());
    }

    public RpcClientProxy(RpcClient rpcClient, RpcServiceConfig config) {
        this.rpcClient = rpcClient;
        this.config = config;
    }

    // pass in interface
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcReq rpcReq = RpcReq.builder()
                .reqId(IdUtil.fastSimpleUUID())
                .interfaceName(method.getDeclaringClass().getCanonicalName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .version(config.getVersion())
                .group(config.getGroup())
                .build();

        RpcResp<?> rpcResp = rpcClient.sendReq(rpcReq);

        check(rpcReq, rpcResp);

        return rpcResp.getData();
    }

    private void check(RpcReq rpcReq, RpcResp rpcResp) {
        if (Objects.isNull(rpcResp)) {
            throw new RpcException("rpc response is empty!");
        }

        if (!Objects.equals(rpcReq.getReqId(), rpcResp.getReqId())) {
            throw new RpcException("rpc request id is not the same as rpc response id");
        }

        if (RpcRespStatusEnum.isFailed(rpcResp.getCode())) {
            throw new RpcException("rpc request failed: " + rpcResp.getMsg());
        }
    }
}
