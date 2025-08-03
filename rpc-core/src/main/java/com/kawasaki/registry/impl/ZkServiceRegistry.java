package com.kawasaki.registry.impl;

import cn.hutool.core.util.StrUtil;
import com.kawasaki.constant.RpcConstant;
import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.registry.ServiceRegistry;
import com.kawasaki.registry.zk.ZkClient;
import com.kawasaki.util.IpUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {
    private final ZkClient zkClient;

    public ZkServiceRegistry() {
        this(SingletonFactory.getInstance(ZkClient.class));
    }

    public ZkServiceRegistry(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public void registerService(String rpcServiceName, InetSocketAddress address) {
        log.info("Registering service, name: {}, address: {}", rpcServiceName, address);

        String path = RpcConstant.ZK_RPC_ROOT_PATH
                + StrUtil.SLASH
                + rpcServiceName
                + StrUtil.SLASH
                + IpUtils.ToIpPort(address);

        zkClient.createPersistentNode(path);

        log.info("Service register at {}!", path);
    }
}
