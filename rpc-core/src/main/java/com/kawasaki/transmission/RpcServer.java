package com.kawasaki.transmission;

import com.kawasaki.config.RpcServiceConfig;

public interface RpcServer {
    void start();

    void publishService(RpcServiceConfig config);
}
