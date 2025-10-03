package com.kawasaki.proxy;

import cn.hutool.core.util.IdUtil;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.kawasaki.annotation.Breaker;
import com.kawasaki.annotation.Retry;
import com.kawasaki.breaker.CircuitBreaker;
import com.kawasaki.breaker.CircuitBreakerManager;
import com.kawasaki.config.RpcServiceConfig;
import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.enums.RpcRespStatusEnum;
import com.kawasaki.exception.RpcException;
import com.kawasaki.transmission.RpcClient;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
        // only handles one method: send rpc request!!!
        RpcReq rpcReq = RpcReq.builder()
                .reqId(IdUtil.fastSimpleUUID())
                .interfaceName(method.getDeclaringClass().getCanonicalName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .version(config.getVersion())
                .group(config.getGroup())
                .build();

        Breaker breaker = method.getAnnotation(Breaker.class);
        if (Objects.isNull(breaker)) {
            return sendReqWithRetry(rpcReq, method);
        }

        CircuitBreaker circuitBreaker = CircuitBreakerManager.get(rpcReq.rpcServiceName(), breaker);
        if (!circuitBreaker.canReq()) {
            throw new RpcException("Proxy is circuit broken");
        }

        try {
            Object o = sendReqWithRetry(rpcReq, method);
            circuitBreaker.success();
            return o;
        } catch (Exception e) {
            circuitBreaker.fail();
            throw e;
        }
    }

    @SneakyThrows
    private Object sendReqWithRetry(RpcReq rpcReq, Method method) {
        Retry retry = method.getAnnotation(Retry.class);
        if (Objects.isNull(retry)) {
            return sendReq(rpcReq);
        }

        // retry
        Retryer<Object> retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(retry.value())
                .withStopStrategy(StopStrategies.stopAfterAttempt(retry.maxAttempts()))
                .withWaitStrategy(WaitStrategies.fixedWait(retry.delay(), TimeUnit.MILLISECONDS))
                .build();

        return retryer.call(() -> sendReq(rpcReq));
    }

    @SneakyThrows
    private Object sendReq(RpcReq rpcReq) {
        Future<RpcResp<?>> future = rpcClient.sendReq(rpcReq);
        RpcResp<?> rpcResp = (RpcResp<?>) future.get(); // blocked until future is completed
        check(rpcReq, rpcResp);

        return rpcResp.getData();
    }

    private void check(RpcReq rpcReq, RpcResp<?> rpcResp) {
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
