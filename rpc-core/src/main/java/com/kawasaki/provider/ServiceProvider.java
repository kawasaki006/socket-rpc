package com.kawasaki.provider;

import com.kawasaki.config.RpcServiceConfig;

public interface ServiceProvider {
    // register services by passing service config
    void publishServices(RpcServiceConfig config);

    // retrieve a service by name
    Object retrieveService(String serviceName);
}
