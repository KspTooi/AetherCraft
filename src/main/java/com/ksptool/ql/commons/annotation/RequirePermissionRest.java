package com.ksptool.ql.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * REST接口权限校验注解
 * 用于标记需要进行权限校验的REST方法
 * 权限不足时会抛出SecurityException异常
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermissionRest {
    
    /**
     * 需要的权限标识
     */
    String value();
} 