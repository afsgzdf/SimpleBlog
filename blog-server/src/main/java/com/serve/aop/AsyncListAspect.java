package com.serve.aop;

import com.serve.anno.AsyncList;
import com.serve.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executor;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class AsyncListAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    //注入通用线程池
    private final Executor cacheThreadPool;

    @Around("@annotation(asyncList)")
    public Object asyncListCache(ProceedingJoinPoint joinPoint, AsyncList asyncList) throws Throwable {
        List<String> keys = parseKeyList(asyncList.prefix(), joinPoint);

        long start = System.nanoTime();
        List<Object> objects = redisTemplate.opsForValue().multiGet(keys);
        List<Integer> signCount = new ArrayList<>();
        Map<String, Object> signObject = new HashMap<>();

        //缓存结果部分为空
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i) == null) {
                signCount.add(i);
            }
        }

        if (signCount.isEmpty()) {
            log.info("缓存命中: {}", keys);
            log.info("查询耗时: {} ms", (System.nanoTime() - start) / 1_000_000.0);
            return Result.success(objects);
        }

        Object result = joinPoint.proceed();

        if (result instanceof Result<?>) {
            log.info("缓存未命中: {}", keys);
            Object data = ((Result<?>) result).getData();
            if (data instanceof List<?> && !((List<?>) data).isEmpty()) {
                signCount.forEach(i -> {
                    signObject.put(keys.get(i), ((List<?>) data).get(i));
                });
            }
            try {
                cacheThreadPool.execute(() -> {
                    log.info("当前异步缓存线程: {}", Thread.currentThread().getName());
                    if (signObject.isEmpty()) {
                        //空值缓存,防止穿透
                        keys.forEach(key -> {
                            signObject.put(key, "");
                        });
                        listPipelined(signObject, asyncList.nullExpire());
                    }else {
                        listPipelined(signObject, asyncList.expire());
                    }
                });
            } catch (Exception e) {
                log.error("异步缓存写入失败: {}", keys, e);
            }
        }
        log.info("切面执行耗时: {} ms", (System.nanoTime() - start) / 1_000_000.0);

        return result;
    }

    private List<String> parseKeyList(String prefix, JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Object arg = args[0];

        List<String> keyListSet = new ArrayList<>();

        if (arg instanceof List<?>) {
            ((List<?>) arg).forEach(key -> {
                keyListSet.add(prefix + ":" + key.toString());
            });
        }

        return keyListSet;
    }

    //pipelined方法插入缓存
    private void listPipelined(Map<String, Object> map, long expireSeconds) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            RedisStringCommands stringCommands = connection.stringCommands();
            RedisSerializer<String> keySerializer = redisTemplate.getStringSerializer();
            RedisSerializer<Object> valueSerializer =
                    (RedisSerializer<Object>) redisTemplate.getValueSerializer();

            map.forEach((mapKey, mapValue) -> {
                byte[] key = keySerializer.serialize(mapKey);
                byte[] value = valueSerializer.serialize(mapValue);
                stringCommands.setEx(key, expireSeconds, value);
            });
            return null;
        });
    }
}
