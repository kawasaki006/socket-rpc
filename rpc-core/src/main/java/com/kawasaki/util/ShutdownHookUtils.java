package com.kawasaki.util;

import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.registry.ServiceRegistry;
import com.kawasaki.registry.impl.ZkServiceRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShutdownHookUtils {
    public static void clearAll() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("System begins shutting down, clearing all resources...");
            ServiceRegistry serviceRegistry = SingletonFactory.getInstance(ZkServiceRegistry.class);
            serviceRegistry.clearAll();
            ThreadPoolUtils.shutdownAll();
        }));
    }
}
