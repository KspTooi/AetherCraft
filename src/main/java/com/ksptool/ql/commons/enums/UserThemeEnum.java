package com.ksptool.ql.commons.enums;

/**
 * 用户主题枚举
 */
public enum UserThemeEnum {

    BRAND_COLOR("brandColor", "rgba(75, 227, 238, 0.62)", "品牌颜色"),
    PRIMARY_COLOR("primaryColor", "rgba(135, 206, 250, 0.7)", "主要主题颜色"),
    MESSAGE_HOVER_USER("messageHoverUser", "rgba(61, 138, 168, 0.12)", "消息悬浮背景色 - 用户"),
    MESSAGE_HOVER_MODEL("messageHoverModel", "rgba(61, 138, 168, 0.12)", "消息悬浮背景色 - 模型"),
    ACTIVE_COLOR("activeColor", "rgba(94, 203, 245, 0.85)", "激活状态的颜色"),
    PRIMARY_HOVER("primaryHover", "rgba(135, 206, 250, 0.2)", "hover状态的颜色"),
    PRIMARY_BUTTON("primaryButton", "rgba(61, 138, 168, 0.24)", "按钮背景颜色"),
    PRIMARY_BUTTON_BORDER("primaryButtonBorder", "rgba(135, 206, 250, 0.7)", "按钮边框颜色"),
    TEXTAREA_COLOR("textareaColor", "rgba(2, 98, 136, 0.1)", "textarea背景颜色"),
    TEXTAREA_ACTIVE("textareaActive", "rgba(61, 138, 168, 0.18)", "textarea激活颜色"),
    TEXTAREA_BORDER("textareaBorder", "rgba(135, 206, 250, 0.22)", "textarea边框颜色"),
    NAV_BLUR("navBlur", "15px", "导航栏模糊程度"),
    MAIN_BLUR("mainBlur", "15px", "主要内容模糊程度"),
    SIDE_BLUR("sideBlur", "15px", "次要内容模糊程度"),
    MODAL_COLOR("modalColor", "rgba(2, 98, 136, 0.1)", "模态窗口背景颜色"),
    MODAL_BLUR("modalBlur", "10px", "模态窗口模糊程度"),
    MODAL_ACTIVE("modalActive", "rgba(0, 212, 240, 0.6)", "模态窗口激活颜色"),
    MENU_COLOR("menuColor", "rgba(22, 56, 66, 0.51)", "三点菜单背景颜色"),
    MENU_ACTIVE_COLOR("menuActiveColor", "rgb(51, 168, 184)", "三点菜单激活颜色"),
    MENU_BLUR("menuBlur", "6px", "三点菜单模糊程度"),
    SELECTOR_COLOR("selectorColor", "rgba(25, 35, 60, 0.5)", "选择器背景颜色"),
    SELECTOR_ACTIVE_COLOR("selectorActiveColor", "rgba(23, 140, 194, 0.5)", "选择器激活颜色"),
    SELECTOR_BORDER_COLOR("selectorBorderColor", "rgba(79, 172, 254, 0.15)", "选择器边框颜色");


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

    UserThemeEnum(String key, String defaultValue, String description) {
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