package com.serve.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RecordTimeAspect {

    @Around("execution(* com.serve.service.impl.*.*(..))")         //环绕类型方法(切面)
    public Object recordTime(ProceedingJoinPoint joinPoint) throws Throwable {
        //记录方法执行前的时间
        long startTime = System.currentTimeMillis();
        //调用原始方法
        Object proceed = joinPoint.proceed();
        //记录方法执行后的时间
        long endTime = System.currentTimeMillis();
        //构造日志实体
        log.info("类名:{}", joinPoint.getSignature().getClass().getName());
        log.info("方法 {} 耗时:{} ms", joinPoint.getSignature().getName(), endTime - startTime);

        return proceed;
    }
}
