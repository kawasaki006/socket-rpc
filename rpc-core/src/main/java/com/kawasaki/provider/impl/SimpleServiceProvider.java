package com.kawasaki.provider.impl;

import cn.hutool.core.collection.CollUtil;
import com.kawasaki.config.RpcServiceConfig;
import com.kawasaki.provider.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SimpleServiceProvider implements ServiceProvider {
    private final Map<String, Object> SERVICE_CACHE = new HashMap<>();

    @Override
    public void publishServices(RpcServiceConfig config) {
        List<String> serviceNames = config.serviceNames();

        if (CollUtil.isEmpty(serviceNames)) {
            throw new RuntimeException("This service does not have any implementations!");
        }

        log.debug("Published services: {}", serviceNames);

        serviceNames.forEach(serviceName -> SERVICE_CACHE.put
                (serviceName, config.getService()));
    }

    @Override
    public Object retrieveService(String serviceName) {
        if (!SERVICE_CACHE.containsKey(serviceName)) {
            throw new IllegalArgumentException("Cannot find the service specified by the service name: " + serviceName);
        }

        return SERVICE_CACHE.get(serviceName);
    }
}
