package com.ksptool.ql.commons.enums;

/**
 * 用户全局配置枚举
 */
public enum UserConfigEnum {


    AI_MODEL_TEMPERATURE("ai.model.cfg.#{modelCode}.temperature", "0.7", "模型温度参数，控制输出的随机性，范围0-1"),
    AI_MODEL_TOP_P("ai.model.cfg.#{modelCode}.topP", "1.0", "模型topP参数，控制输出的多样性，范围0-1"),
    AI_MODEL_TOP_K("ai.model.cfg.#{modelCode}.topK", "40", "模型topK参数，控制输出的词汇范围"),
    AI_MODEL_MAX_OUTPUT_TOKENS("ai.model.cfg.#{modelCode}.maxOutputTokens", "800", "模型最大输出token数"),
    
    MODEL_CHAT_CURRENT_THREAD("model.chat.current.thread", null, "用户最后一次选中的CHAT_THREAD"),
    MODEL_RP_CURRENT_THREAD("model.rp.current.thread", null, "用户最后一次选中的RP_THREAD"),
    MODEL_RP_CURRENT_ROLE("model.rp.current.role", null, "用户最后一次选中的RP_ROLE"),


    CUSTOM_MAIN_COLOR("theme.custom.main.color", null, "主要区域颜色"),
    CUSTOM_MAIN_FILTER("theme.custom.main.filter", null, "主要区域模糊"),
    CUSTOM_TOP_NAV_COLOR("theme.custom.nav.color", null, "导航栏颜色"),
    CUSTOM_TOP_NAV_FILTER("theme.custom.nav.filter", null, "导航栏模糊"),
    CUSTOM_ACTIVE_STYLE("theme.custom.active.style", null, "激活主题配色"),
    CUSTOM_BUTTON_STYLE("theme.custom.button.style", null, "按钮主题配色"),
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