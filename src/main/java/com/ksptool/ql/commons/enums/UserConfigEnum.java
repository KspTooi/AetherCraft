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

    CUSTOM_THEME_BRAND_COLOR("custom.theme.brand.color", "rgba(75, 227, 238, 0.62)", "品牌颜色"),
    CUSTOM_THEME_PRIMARY_COLOR("custom.theme.primary.color", "rgba(135, 206, 250, 0.7)", "主要主题颜色"),
    CUSTOM_THEME_MESSAGE_HOVER_USER("custom.theme.message.hover.user", "rgba(61, 138, 168, 0.12)", "消息悬浮背景色 - 用户"),
    CUSTOM_THEME_MESSAGE_HOVER_MODEL("custom.theme.message.hover.model", "rgba(61, 138, 168, 0.12)", "消息悬浮背景色 - 模型"),
    CUSTOM_THEME_ACTIVE_COLOR("custom.theme.active.color", "rgba(94, 203, 245, 0.85)", "激活状态的颜色"),
    CUSTOM_THEME_PRIMARY_HOVER("custom.theme.primary.hover", "rgba(135, 206, 250, 0.2)", "hover状态的颜色"),
    CUSTOM_THEME_PRIMARY_BUTTON("custom.theme.primary.button", "rgba(61, 138, 168, 0.24)", "按钮背景颜色"),
    CUSTOM_THEME_PRIMARY_BUTTON_BORDER("custom.theme.primary.button.border", "rgba(135, 206, 250, 0.7)", "按钮边框颜色"),
    CUSTOM_THEME_TEXTAREA_COLOR("custom.theme.textarea.color", "rgba(2, 98, 136, 0.1)", "textarea背景颜色"),
    CUSTOM_THEME_TEXTAREA_ACTIVE("custom.theme.textarea.active", "rgba(61, 138, 168, 0.18)", "textarea激活颜色"),
    CUSTOM_THEME_TEXTAREA_BORDER("custom.theme.textarea.border", "rgba(135, 206, 250, 0.22)", "textarea边框颜色"),
    CUSTOM_THEME_NAV_BLUR("custom.theme.nav.blur", "15px", "导航栏模糊程度"),
    CUSTOM_THEME_MAIN_BLUR("custom.theme.main.blur", "15px", "主要内容模糊程度"),
    CUSTOM_THEME_SIDE_BLUR("custom.theme.side.blur", "15px", "次要内容模糊程度"),
    CUSTOM_THEME_MODAL_COLOR("custom.theme.modal.color", "rgba(2, 98, 136, 0.1)", "模态窗口背景颜色"),
    CUSTOM_THEME_MODAL_BLUR("custom.theme.modal.blur", "10px", "模态窗口模糊程度"),
    CUSTOM_THEME_MODAL_ACTIVE("custom.theme.modal.active", "rgba(0, 212, 240, 0.6)", "模态窗口激活颜色"),
    CUSTOM_THEME_MENU_COLOR("custom.theme.menu.color", "rgba(22, 56, 66, 0.51)", "三点菜单背景颜色"),
    CUSTOM_THEME_MENU_ACTIVE_COLOR("custom.theme.menu.active.color", "rgb(51, 168, 184)", "三点菜单激活颜色"),
    CUSTOM_THEME_MENU_BLUR("custom.theme.menu.blur", "6px", "三点菜单模糊程度"),
    CUSTOM_THEME_SELECTOR_COLOR("custom.theme.selector.color", "rgba(25, 35, 60, 0.5)", "选择器背景颜色"),
    CUSTOM_THEME_SELECTOR_ACTIVE_COLOR("custom.theme.selector.active.color", "rgba(23, 140, 194, 0.5)", "选择器激活颜色"),
    CUSTOM_THEME_SELECTOR_BORDER_COLOR("custom.theme.selector.border.color", "rgba(79, 172, 254, 0.15)", "选择器边框颜色");


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