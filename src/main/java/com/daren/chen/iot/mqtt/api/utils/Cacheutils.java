package com.daren.chen.iot.mqtt.api.utils;

import java.util.concurrent.TimeUnit;

import org.springframework.util.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/6/29 16:14
 */
public class Cacheutils {
    /**
     *
     */
    // 设置并发级别为8，并发级别是指可以同时写缓存的线程数
    // 设置缓存容器的初始容量为10
    // 设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
    // 是否需要统计缓存情况,该操作消耗一定的性能,生产环境应该去除
    // 设置写缓存后n秒钟过期
    // 设置读写缓存后n秒钟过期,实际很少用到,类似于expireAfterWrite
    private static final Cache<String, String> CACHE = CacheBuilder.newBuilder().concurrencyLevel(8).initialCapacity(10)
        .maximumSize(100).expireAfterWrite(5, TimeUnit.MINUTES).build();

    /**
     *
     * @param key
     * @param value
     */
    public static void put(String key, String value) {
        if (StringUtils.isEmpty(key)) {
            throw new RuntimeException("key不能为空!");
        }
        CACHE.put(key, value);
    }

    /**
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        if (StringUtils.isEmpty(key)) {
            throw new RuntimeException("key不能为空!");
        }
        return CACHE.getIfPresent(key);

    }
}
