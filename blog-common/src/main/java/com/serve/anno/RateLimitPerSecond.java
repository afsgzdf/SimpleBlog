package com.serve.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimitPerSecond {

    //默认每秒接收10次请求
    double permitsPerSecond() default 10;
    //接口标识
    String key() default "";
}
