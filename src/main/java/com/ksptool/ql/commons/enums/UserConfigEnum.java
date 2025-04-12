package com.ksptool.ql.commons.enums;

/**
 * 用户全局配置枚举
 */
public enum UserConfigEnum {


    AI_MODEL_TEMPERATURE("ai.model.cfg.#{modelCode}.temperature", "0.7", "模型温度参数，控制输出的随机性，范围0-1"),
    AI_MODEL_TOP_P("ai.model.cfg.#{modelCode}.topP", "1.0", "模型topP参数，控制输出的多样性，范围0-1"),
    AI_MODEL_TOP_K("ai.model.cfg.#{modelCode}.topK", "40", "模型topK参数，控制输出的词汇范围"),
    AI_MODEL_MAX_OUTPUT_TOKENS("ai.model.cfg.#{modelCode}.maxOutputTokens", "4096", "模型最大输出token数"),
    
    MODEL_CHAT_CURRENT_THREAD("model.chat.current.thread", null, "用户最后一次选中的CHAT_THREAD"),
    MODEL_RP_CURRENT_THREAD("model.rp.current.thread", null, "用户最后一次选中的RP_THREAD"),
    MODEL_RP_CURRENT_ROLE("model.rp.current.role", null, "用户最后一次选中的RP_ROLE"),

    WALLPAPER_PATH("wallpaper.path", null, "用户的壁纸路径(相对路径)"),

    USER_PREF_CLIENT_PATH("user.pref.client.path", null, "偏好:用户前端当前访问的路径"),
    USER_PREF_CUSTOMIZE_PATH_SIDE("user.pref.customize.path.side", null, "偏好:个性化-边栏-路径"),
    USER_PREF_CUSTOMIZE_PATH_TAB_WALLPAPER("user.pref.customize.path.tab.wallpaper", null, "偏好:个性化-背景-TAB"),
    USER_PREF_CUSTOMIZE_PATH_TAB_THEME("user.pref.customize.path.tab.theme", null, "偏好:个性化-主题-TAB"),
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