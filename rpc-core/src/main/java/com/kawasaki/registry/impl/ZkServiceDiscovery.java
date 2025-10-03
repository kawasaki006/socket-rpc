package com.kawasaki.registry.impl;

import cn.hutool.core.util.StrUtil;
import com.kawasaki.constant.RpcConstant;
import com.kawasaki.dto.RpcReq;
import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.lb.LoadBalance;
import com.kawasaki.lb.impl.RandomLoadBalance;
import com.kawasaki.registry.ServiceDiscovery;
import com.kawasaki.registry.ServiceRegistry;
import com.kawasaki.registry.zk.ZkClient;
import com.kawasaki.util.IpUtils;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscovery implements ServiceDiscovery {
    private final ZkClient zkClient;
    private final LoadBalance lb;

    public ZkServiceDiscovery() {
        this(
            SingletonFactory.getInstance(ZkClient.class),
            SingletonFactory.getInstance(RandomLoadBalance.class)
        );
    }

    public ZkServiceDiscovery(ZkClient zkClient, LoadBalance lb) {
        this.zkClient = zkClient;
        this.lb = lb;
    }

    @Override
    public InetSocketAddress findService(RpcReq rpcReq) {
        String path = RpcConstant.ZK_RPC_ROOT_PATH
                + StrUtil.SLASH
                +rpcReq.rpcServiceName();

        List<String> children = zkClient.getChildrenNodes(path);
        String address = lb.select(children, rpcReq);

        return IpUtils.ToInetSocketAddress(address);
    }
}
