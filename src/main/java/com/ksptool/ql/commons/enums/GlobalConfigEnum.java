package com.ksptool.ql.commons.enums;

/**
 * 系统全局配置枚举
 */
public enum GlobalConfigEnum {
    

    USER_FILE_STORAGE_PATH("user.file.storage.path", "/userdata/res", "用户文件的存储位置(相对路径)"),


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

    GlobalConfigEnum(String key, String defaultValue, String description) {
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