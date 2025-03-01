package com.ksptool.ql.commons.enums;

/**
 * 系统全局配置枚举
 */
public enum GlobalConfigEnum {
    

    USER_FILE_STORAGE_PATH("user.file.storage.path", "/userdata/res", "用户文件的存储位置(相对路径)"),
    MODEL_CHAT_GEN_THREAD_TITLE("model.chat.gen.thread.title", "true", "是否自动生成会话标题，true-生成，false-不生成"),
    MODEL_CHAT_GEN_THREAD_PROMPT("model.chat.gen.thread.prompt", "总结内容并生成一个简短的标题(不超过10个字符),请直接回复标题,不要回复其他任何多余的话! 需总结的内容:#{content}", "生成会话标题的提示语模板，#{content}将被替换为用户的第一条消息"),


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