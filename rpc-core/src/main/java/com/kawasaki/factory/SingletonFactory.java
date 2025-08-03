package com.kawasaki.factory;

import lombok.SneakyThrows;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class SingletonFactory {
    private static final Map<Class<?>, Object> INSTANCE_CACHE = new ConcurrentHashMap<Class<?>, Object>();

    private SingletonFactory() {}

    @SneakyThrows
    public static <T> T getInstance(Class<T> clazz) {
        if (Objects.isNull(clazz)) {
            throw new IllegalArgumentException("clazz cannot be null!");
        }

        if (INSTANCE_CACHE.containsKey(clazz)) {
            return clazz.cast(INSTANCE_CACHE.get(clazz));
        }

        synchronized (SingletonFactory.class) {
            if (INSTANCE_CACHE.containsKey(clazz)) {
                return clazz.cast(INSTANCE_CACHE.get(clazz));
            }

            T t = clazz.getConstructor().newInstance();
            INSTANCE_CACHE.put(clazz, t);

            return t;
        }
    }
}
