package com.serve.aop;

import com.google.common.util.concurrent.RateLimiter;
import com.serve.anno.RateLimitPerMinute;
import com.serve.anno.RateLimitPerSecond;
import com.serve.enums.ResultCode;
import com.serve.result.Result;
import com.serve.util.RateLimiterUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class RateLimitAspect {

    @Around("@annotation(rateLimitPerSecond)")
    public Object rateLimitPerSecond(ProceedingJoinPoint joinPoint,
                                     RateLimitPerSecond rateLimitPerSecond) throws Throwable {
        //构建限流器唯一标识key
        String key = rateLimitPerSecond.key();
        if (key.isEmpty()) {
            key = joinPoint.getSignature().getClass().getName() + ":"
                    + joinPoint.getSignature().getName();
        }

        //获取限流器
        RateLimiter rateLimiter = RateLimiterUtil.
                getRateLimiterPerSecond(key, rateLimitPerSecond.permitsPerSecond());
        //尝试获取令牌
        if (!rateLimiter.tryAcquire()) {
            log.warn("接口:{} 请求过于频繁!", key);
            return Result.error(ResultCode.TOO_MANY_REQUESTS.toString());
        }
        //执行业务
        return joinPoint.proceed();
    }

    @Around("@annotation(rateLimitPerMinute)")
    public Object rateLimitPerMinute(
            ProceedingJoinPoint joinPoint, RateLimitPerMinute rateLimitPerMinute) throws Throwable {
        String key = rateLimitPerMinute.key();
        if (key.isEmpty()) {
            key = joinPoint.getSignature().getClass().getName() + ":"
                    + joinPoint.getSignature().getName();
        }

        RateLimiter rateLimiter = RateLimiterUtil.
                getRateLimiterPerMinute(key, rateLimitPerMinute.permitsPerMinute());
        if (!rateLimiter.tryAcquire()) {
            log.warn("接口:{} 请求过于频繁!", key);
            return Result.error(ResultCode.TOO_MANY_REQUESTS.toString());
        }

        return joinPoint.proceed();
    }
}
