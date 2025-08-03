package com.kawasaki.provider.impl;

import cn.hutool.core.util.StrUtil;
import com.kawasaki.config.RpcServiceConfig;
import com.kawasaki.constant.RpcConstant;
import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.provider.ServiceProvider;
import com.kawasaki.registry.ServiceRegistry;
import com.kawasaki.registry.impl.ZkServiceRegistry;
import lombok.SneakyThrows;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ZkServiceProvider implements ServiceProvider {
    private final Map<String, Object> SERVICE_CACHE = new HashMap<>();
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProvider() {
        this(SingletonFactory.getInstance(ZkServiceRegistry.class));
    }

    public ZkServiceProvider(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void publishServices(RpcServiceConfig config) {
        config.serviceNames()
                .forEach(serviceName -> publishService(serviceName, config.getService()));
    }

    @Override
    public Object retrieveService(String serviceName) {
        if (StrUtil.isBlank(serviceName)) {
            throw new IllegalArgumentException("service name cannot be null!");
        }

        if (!SERVICE_CACHE.containsKey(serviceName)) {
            throw new IllegalArgumentException("service not registered: " + serviceName);
        }

        return SERVICE_CACHE.get(serviceName);
    }

    @SneakyThrows
    private void publishService(String rpcServiceName, Object service) {
        String host = InetAddress.getLocalHost().getHostAddress();
        int port = RpcConstant.SERVER_PORT;

        InetSocketAddress address = new InetSocketAddress(host, port);
        serviceRegistry.registerService(rpcServiceName, address);

        SERVICE_CACHE.put(rpcServiceName, service);
    }
}
