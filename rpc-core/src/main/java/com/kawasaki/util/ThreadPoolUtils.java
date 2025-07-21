package com.kawasaki.util;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class ThreadPoolUtils {
    private static final Map<String, ExecutorService> THREAD_POOL_CACHE = new ConcurrentHashMap<>();
    private static final int CPU_NUM = Runtime.getRuntime().availableProcessors();
    private static final int CPU_INTENSIVE_CORE_POOL_SIZE = CPU_NUM + 1;
    private static final int IO_INTENSIVE_CORE_POOL_SIZE = CPU_NUM * 2;
    private static final int DEFAULT_KEEP_ALIVE_TIME = 60;
    private static final int DEFAULT_QUEUE_SIZE = 128;

    public static ExecutorService createCPUIntensiveThreadPool(String poolName) {
        return createThreadPool(CPU_INTENSIVE_CORE_POOL_SIZE, poolName);
    }

    public static ExecutorService createIOIntensiveThreadPool(String poolName) {
        return createThreadPool(IO_INTENSIVE_CORE_POOL_SIZE, poolName);
    }

    public static ExecutorService createThreadPool(
            int corePoolSize, String poolName) {
        return createThreadPool(corePoolSize, corePoolSize, poolName);
    }

    public static ExecutorService createThreadPool(
            int corePoolSize, int maxPoolSize,
            String poolName) {
        return createThreadPool(corePoolSize, maxPoolSize,
                DEFAULT_KEEP_ALIVE_TIME, DEFAULT_QUEUE_SIZE,
                poolName);
    }

    public static ExecutorService createThreadPool(
            int corePoolSize, int maxPoolSize,
            long keepAliveTime,int queueSize,
            String poolName) {
        return createThreadPool(corePoolSize, maxPoolSize,
                keepAliveTime, queueSize,
                poolName, false);
    }

    public static ExecutorService createThreadPool(
            int corePoolSize, int maxPoolSize,
            long keepAliveTime,int queueSize,
            String poolName, boolean isDaemon) {
        if (THREAD_POOL_CACHE.containsKey(poolName)) {
            return THREAD_POOL_CACHE.get(poolName);
        }

        ExecutorService executorService = new ThreadPoolExecutor(
                corePoolSize, maxPoolSize,
                keepAliveTime, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                createThreadFactory(poolName, isDaemon));

        log.info("Creating thread pool {}", poolName);

        // cache new thread pool
        THREAD_POOL_CACHE.put(poolName, executorService);

        return executorService;
    }

    // helper method to create a thread factory with thread pool name and is daemon flag
    public static ThreadFactory createThreadFactory(String poolName, boolean isDaemon) {
        ThreadFactoryBuilder builder = ThreadFactoryBuilder
                .create()
                .setDaemon(isDaemon);

        // check if poolName is empty
        if (StrUtil.isBlank(poolName)) {
            return builder.build();
        }

        return builder.setNamePrefix(poolName).build();
    }

    // shutdown pools
    public static void shutdownAll() {
        THREAD_POOL_CACHE.entrySet().parallelStream()
                .forEach( entry -> {
                    String poolName = entry.getKey();
                    ExecutorService executorService = entry.getValue();

                    executorService.shutdown();
                    log.info("{}, thread pool begins shutting down...", poolName);

                    try {
                        if (executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                            log.info("{}, thread pool is down", poolName);
                        } else {
                            log.info("{}, thread pool failed to shut down in 10s, start force quit", poolName);
                            executorService.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        log.error("{}, error trying to shut down thread pool", poolName);
                        executorService.shutdownNow();
                    }
                });
    }
}
