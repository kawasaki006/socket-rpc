package com.kawasaki.handler;

import com.kawasaki.dto.RpcReq;
import com.kawasaki.provider.ServiceProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class RpcReqHandler {
    private final ServiceProvider serviceProvider;

    public RpcReqHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @SneakyThrows
    public Object invoke(RpcReq rpcReq) {
        // get service from provider
        String serviceName = rpcReq.rpcReqName();
        Object service = serviceProvider.retrieveService(serviceName);

        log.debug("Retrieved service: {}", service.getClass().getCanonicalName());

        // retrieve and invoke method
        Method method = service.getClass().getMethod(rpcReq.getMethodName(), rpcReq.getParamTypes());
        return method.invoke(service, rpcReq.getParams());
    }
}
