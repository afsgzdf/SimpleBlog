package com.serve.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncList {
    String prefix();
    //缓存时间
    long expire() default 3600;
    //空值缓存时间,防穿透
    long nullExpire() default 60;
}
