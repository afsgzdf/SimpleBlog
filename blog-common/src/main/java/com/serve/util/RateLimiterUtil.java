package com.serve.util;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RateLimiterUtil {

    private static final ConcurrentMap<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    /**
     * 获取限流器（不存在则创建）
     * @param key
     * @param ratePerSecond
     * @return RateLimiter
     */
    public static RateLimiter getRateLimiterPerSecond(String key, double ratePerSecond) {
        return rateLimiterMap.computeIfAbsent(key, k -> RateLimiter.create(ratePerSecond));
    }

    public static RateLimiter getRateLimiterPerMinute(String key, double ratePerMinute) {
        return rateLimiterMap.computeIfAbsent(key, k -> RateLimiter.create(ratePerMinute / 60.0));
    }
}
