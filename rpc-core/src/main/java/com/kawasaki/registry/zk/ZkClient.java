package com.kawasaki.registry.zk;

import cn.hutool.core.util.StrUtil;
import com.kawasaki.constant.RpcConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

@Slf4j
public class ZkClient {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RECIPES = 3;
    private CuratorFramework client;

    public ZkClient() {
        this(RpcConstant.ZK_IP, RpcConstant.ZK_PORT);
    }

    public ZkClient(String hostname, int port) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RECIPES);

        client = CuratorFrameworkFactory.builder()
                .connectString(hostname + StrUtil.COLON + port)
                .retryPolicy(retryPolicy)
                .build();

        log.info("start connecting to zk server...");

        client.start();
        log.info("zk server connected!");
    }

    @SneakyThrows
    public void createPersistentNode(String path) {
        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("path is null!");
        }

        if (client.checkExists().forPath(path) != null) {
            log.info("path exists!");
            return;
        }

        log.info("Creating node...");
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path);
    }

    @SneakyThrows
    public List<String> getChildrenNodes(String path) {
        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("path is null!");
        }

        return client.getChildren().forPath(path);
    }
}
