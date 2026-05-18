package com.serve.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncCacheEvict {
    String prefix();
    String variable() default "id";

    /**
     * 是否删除该前缀下的所有缓存
     */
    boolean allEntries() default false;
}
