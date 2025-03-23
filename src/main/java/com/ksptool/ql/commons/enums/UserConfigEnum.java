package com.ksptool.ql.commons.enums;

/**
 * 用户全局配置枚举
 */
public enum UserConfigEnum {


    MODEL_TEMPERATURE("ai.model.cfg.#{modelCode}.temperature", "0.7", "模型温度参数，控制输出的随机性，范围0-1"),
    MODEL_TOP_P("ai.model.cfg.#{modelCode}.topP", "1.0", "模型topP参数，控制输出的多样性，范围0-1"),
    MODEL_TOP_K("ai.model.cfg.#{modelCode}.topK", "40", "模型topK参数，控制输出的词汇范围"),
    MODEL_MAX_OUTPUT_TOKENS("ai.model.cfg.#{modelCode}.maxOutputTokens", "800", "模型最大输出token数")

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

    public String key() {
        return key;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public String description() {
        return description;
    }

} 