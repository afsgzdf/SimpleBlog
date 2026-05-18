package com.serve.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimitPerMinute {

    //默认每分钟接收5次请求
    double permitsPerMinute() default 5;
    //接口标识
    String key() default "";
}
