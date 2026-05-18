package com.serve.aop;

import com.serve.anno.AsyncCache;
import com.serve.anno.AsyncCacheEvict;
import com.serve.result.Result;
import com.serve.util.ObjectGetIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class AsyncCacheAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    //注入通用线程池
    private final Executor cacheThreadPool;

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer
            = new DefaultParameterNameDiscoverer();

    //查询：异步缓存
    @Around("@annotation(asyncCache)")
    public Object asyncCache(ProceedingJoinPoint joinPoint, AsyncCache asyncCache) throws Throwable {
        String keyVar = parseKey(asyncCache.key(), joinPoint);
        String key = asyncCache.prefix() + ":" + keyVar;

        //1. 先读缓存
        long start = System.nanoTime();
        Object cache = redisTemplate.opsForValue().get(key);
        if (cache != null) {
            log.info("缓存命中: {}", key);
            log.info("查询耗时: {} ms", (System.nanoTime() - start) / 1_000_000.0);
            return Result.success(cache);
        }
        log.info("缓存未命中: {}", key);

        //2. 执行业务逻辑（查库）
        Object result = joinPoint.proceed();

        //3. 异步回写Redis（不阻塞业务）
        if (result instanceof Result<?>) {
            Object data = ((Result<?>) result).getData();
            cacheThreadPool.execute(() -> {
                try {
                    log.info("当前异步缓存线程: {}", Thread.currentThread().getName());
                    if (data == null) {
                        //空值缓存,防止穿透
                        redisTemplate.opsForValue().set(key, "", asyncCache.nullExpire(), TimeUnit.SECONDS);
                    }else {
                        redisTemplate.opsForValue().set(key, data, asyncCache.expire(), TimeUnit.SECONDS);
                    }
                }catch (Exception e){
                    log.error("异步缓存写入失败: {}", key, e);
                }
            });
        }
        log.info("查询耗时: {} ms", (System.nanoTime() - start) / 1_000_000.0);
        return result;
    }

    @After("@annotation(asyncCacheEvict)")
    public void asyncEvictCache(JoinPoint joinPoint, AsyncCacheEvict asyncCacheEvict) throws Throwable {
        String prefix = asyncCacheEvict.prefix();
        Object[] args = joinPoint.getArgs();

        long start = System.nanoTime();

        try {
            if (asyncCacheEvict.allEntries()) {
                //全部删除缓存
                String pattern = prefix + ":*";
                Set<String> keys = scanKeyBySPEL(pattern);

                if (!keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } else {
                if (args == null || args.length == 0) {
                    log.error("删除缓存失败,方法无参数!");
                    return;
                }
                //获取要删除的id缓存集合
                Object arg = args[0];
                Set<Long> keyIds = extractIds(arg, asyncCacheEvict.variable());

                if (keyIds.isEmpty()) {
                    log.error("未解析需要缓存的id,请稍后再试!");
                }

                cacheThreadPool.execute(() -> {
                    try {
                        //拼接keys删除缓存
                        List<String> deleteKeys = keyIds.stream()
                                .map(id -> prefix + ":" + id)
                                .collect(Collectors.toList());
                        log.info("执行批量删除缓存: {}", deleteKeys);
                        redisTemplate.delete(deleteKeys);

                        log.info("缓存切面执行耗时: {} ms", (System.nanoTime() - start) / 1_000_000.0);
                    }catch (Exception e){
                        log.error("批量删除缓存失败!: {}", prefix, e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("缓存切面执行异常!: {}", prefix, e);
        }
    }

    private String parseKey(String key, JoinPoint joinPoint) {
        //1.获取方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        //2.获取方法参数值
        Object[] args = joinPoint.getArgs();

        //3.获取参数名[productId]
        String[] parameterNames = discoverer.getParameterNames(method);

        //4.创建一个参数字典 productId -> 1001
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        //5.翻译productId -> 1001
        return expressionParser.parseExpression(key).getValue(context, String.class);
    }

    //获取要删除的id集合
    private Set<Long> extractIds(Object param, String variable) {
        Set<Long> ids = new HashSet<>();
        if (param == null) {
            return ids;
        }

        if (param instanceof Collection<?>) {
            ((Collection<?>) param).forEach(key -> {
                if (key instanceof Long) {
                    ids.add((Long) key);
                } else if (key instanceof String) {
                    ids.add(Long.valueOf((String) key));
                }
            });
        }
        else if (param instanceof Map<?,?>) {
            // 对应 deduceStock 方法，参数是 Map<String, Integer>
            ((Map<?,?>) param).keySet().forEach(key -> {
                if (key instanceof String) {
                    ids.add(Long.valueOf((String) key));
                }
            });
        } else if (param instanceof Long) {
            ids.add((Long) param);

        }else {
            //最后再通过反射获取id
            Long idFromObject = ObjectGetIdUtil.getIdFromObject(param, variable);
            if (idFromObject != null) { ids.add(idFromObject); }
        }
        return ids;
    }

    //SCAN 迭代获取匹配key（无阻塞）
    private Set<String> scanKeyBySPEL(String pattern) {
        Set<String> keys = new HashSet<>();

        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions()
                            .match(pattern)
                            .count(1000)
                            .build()
            )) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            }catch (Exception e){
                log.error("获取key出错!: {}", pattern, e);
            }
            return null;
        });
        return keys;
    }
}
