package com.serve.aop;

import com.serve.anno.RepeatSubmit;
import com.serve.context.BaseContext;
import com.serve.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
@Order(1)
public class RepeatSubmitAspect {

    private final RedisTemplate<String,Object> redisTemplate;

    @Around("@annotation(repeatSubmit)")
    public Object repeatSubmit(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        Long userId = BaseContext.getThreadLocal();
        String uri = request.getRequestURI();
        String key = "repeatSubmit:" + userId + ":" + uri;

        //防重复提交
        Boolean lock = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", repeatSubmit.expire(), TimeUnit.SECONDS);
        if (lock == null || !lock) {
            return Result.error("操作过于频繁，请稍后再试!");
        }
        return joinPoint.proceed();
    }
}
