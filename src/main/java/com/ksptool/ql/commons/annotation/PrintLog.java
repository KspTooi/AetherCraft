package com.ksptool.ql.commons.annotation;

import java.lang.annotation.*;

@Target({ ElementType.METHOD,ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PrintLog {

    //模块名称(如果不声明会采用控制器类名)
    String value() default "";

    // 需要脱敏的字段名称，支持多个
    String[] sensitiveFields() default {};

}