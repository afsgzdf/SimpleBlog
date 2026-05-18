package com.serve.aop;

import com.serve.context.BaseContext;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Aspect
@RequiredArgsConstructor
public class SseLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("execution(* com.serve.controller.AIRemoteController.*(..)))")
    public Object AIRequestLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = BaseContext.getThreadLocal();
        String key = "ai:limit:" + userId;

        Long count = redisTemplate.opsForValue().increment(key, 1);
        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.DAYS);
        }
        if (count > 20) {
            throw new RuntimeException("每个用户一天最多只能提问20次!");
        }
        return joinPoint.proceed();
    }
}
