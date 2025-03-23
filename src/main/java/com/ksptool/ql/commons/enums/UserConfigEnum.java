package com.ksptool.ql.commons.enums;

/**
 * 用户全局配置枚举
 */
public enum UserConfigEnum {

    APPLICATION_VERSION("application.version","1.0A","应用程序版本"),


    ;

    /**
     * 配置键
     */
    private final String key;

    /**
     * 默认值
     */
    private final String defaultValue;

    /**
     * 配置描述
     */
    private final String description;

    UserConfigEnum(String key, String defaultValue, String description) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }
} 