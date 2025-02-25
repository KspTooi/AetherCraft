package com.ksptool.ql.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 用于标记需要进行权限校验的方法
 * 权限不足时会重定向到noPermission页面
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * 需要的权限标识
     */
    String value();
} 