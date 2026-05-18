package com.serve.aop;

import com.github.benmanes.caffeine.cache.Cache;
import com.serve.anno.AsyncLocalCache;
import com.serve.anno.AsyncLocalEvict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
@Order(10)
public class AsyncCaffeineAspect {

    private final Cache<String, Object> localCache;

    private final Executor cacheThreadPool;

    @Around("@annotation(asyncLocalCache)")
    public Object localCache(ProceedingJoinPoint joinPoint, AsyncLocalCache asyncLocalCache) throws Throwable {
        String prefix = asyncLocalCache.prefix();
        String key = asyncLocalCache.key();
        String keyVar = prefix + ":" + key;

        long start = System.nanoTime();
        //先查本地缓存
        Object data = localCache.getIfPresent(keyVar);
        if (data != null) {
            log.info("缓存命中: {}", keyVar);
            log.info("查询耗时: {} ms", (System.nanoTime() - start) / 1_000_000.0);
            return data;
        }

        Object result = joinPoint.proceed();

        cacheThreadPool.execute(() -> {
            try {
                log.info("当前异步缓存线程: {}", Thread.currentThread().getName());
                if (result == null) {
                    localCache.put(keyVar, new Object());
                    return;
                }
                localCache.put(keyVar, result);
            } catch (Exception e) {
                log.error("异步缓存写入失败: {}", keyVar, e);
            }
        });

        log.info("执行耗时: {} ms", (System.nanoTime() - start) / 1_000_000.0);
        return result;
    }

    @After("@annotation(asyncLocalEvict)")
    public void localCacheEvict(AsyncLocalEvict asyncLocalEvict) {
        String prefix = asyncLocalEvict.prefix();
        String key = asyncLocalEvict.key();
        String keyVar = prefix + ":" + key;

        cacheThreadPool.execute(() -> {
            try {
                localCache.invalidate(keyVar);
            } catch (Exception e) {
                log.error("异步缓存删除失败: {}", keyVar, e);
            }
        });
    }
}
