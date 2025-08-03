package com.kawasaki.registry.zk;

import cn.hutool.core.util.StrUtil;
import com.kawasaki.constant.RpcConstant;
import com.kawasaki.util.IpUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkClient {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RECIPES = 3;
    private final CuratorFramework client;

    // service address cache
    private static final Map<String, List<String>> SERVICE_ADDRESS_CACHE = new ConcurrentHashMap<>();
    // stores full path to address
    private static final Set<String> SERVICE_ADDRESS_SET = ConcurrentHashMap.newKeySet();

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

        if (SERVICE_ADDRESS_SET.contains(path)) {
            log.info("path node exists!");
            return;
        }

        if (client.checkExists().forPath(path) != null) {
            SERVICE_ADDRESS_SET.add(path);
            log.info("path exists!");
            return;
        }

        log.info("Creating node...");
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path);

        SERVICE_ADDRESS_SET.add(path);
    }

    @SneakyThrows
    public List<String> getChildrenNodes(String path) {
        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("path is null!");
        }

        if (SERVICE_ADDRESS_CACHE.containsKey(path)) {
            return SERVICE_ADDRESS_CACHE.get(path);
        }

        List<String> children = client.getChildren().forPath(path);
        SERVICE_ADDRESS_CACHE.put(path, children);

        watchNode(path);

        return children;
    }

    // clear nodes on zk server when a server is down (address: address of the closing server)
    public void clearAll(InetSocketAddress address) {
        if (Objects.isNull(address)) {
            throw new IllegalArgumentException("address cannot be null!");
        }

        SERVICE_ADDRESS_SET.forEach(path -> {
            if (path.endsWith(IpUtils.ToIpPort(address))) {
                log.debug("zk deleting node: {}", path);
                try {
                    client.delete().deletingChildrenIfNeeded().forPath(path);
                } catch (Exception e) {
                    log.error("failed to delete zk node!");
                }
            }
        });
    }

    // watch for any changes in a path
    @SneakyThrows
    private void watchNode(String path) {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);

        PathChildrenCacheListener listener = (curClient, event) -> {
            List<String> children = curClient.getChildren().forPath(path);
            SERVICE_ADDRESS_CACHE.put(path, children);
        };

        pathChildrenCache.getListenable().addListener(listener);
        pathChildrenCache.start();
    }
}
