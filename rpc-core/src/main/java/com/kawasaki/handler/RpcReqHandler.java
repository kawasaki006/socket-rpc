package com.kawasaki.handler;

import com.kawasaki.annotation.Limit;
import com.kawasaki.dto.RpcReq;
import com.kawasaki.exception.RpcException;
import com.kawasaki.provider.ServiceProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.util.concurrent.RateLimiter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcReqHandler {
    private final ServiceProvider serviceProvider;
    private static final Map<String, RateLimiter> RATE_LIMITER_MAP = new ConcurrentHashMap<>();

    public RpcReqHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @SneakyThrows
    public Object invoke(RpcReq rpcReq) {
        // get service from provider
        String serviceName = rpcReq.rpcServiceName();
        Object service = serviceProvider.retrieveService(serviceName);

        log.debug("Retrieved service: {}", service.getClass().getCanonicalName());

        // retrieve and invoke method
        Method method = service.getClass().getMethod(rpcReq.getMethodName(), rpcReq.getParamTypes());

        // handle limit
        Limit limit = method.getAnnotation(Limit.class);
        if (Objects.isNull(limit)) {
            return method.invoke(service, rpcReq.getParams());
        }

        RateLimiter rateLimiter = RATE_LIMITER_MAP.computeIfAbsent(serviceName,
                __ -> RateLimiter.create(limit.permitsPerSecond()));
        if (!rateLimiter.tryAcquire(limit.timeout(), TimeUnit.MILLISECONDS)) {
            throw new RpcException("Server busy, please try again later");
        }

        return method.invoke(service, rpcReq.getParams());
    }
}
