package com.ksptool.ql.commons.enums;

/**
 * 用户全局配置枚举
 */
public enum WallpaperEnum {


    W1("wallpaper1.jpeg", "预设的壁纸"),
    W2("wallpaper2.jpeg", "预设的壁纸"),
    W3("wallpaper3.jpeg", "预设的壁纸"),
    W4("wallpaper4.jpeg", "预设的壁纸"),
    W5("wallpaper5.jpeg", "预设的壁纸"),
    W6("wallpaper6.jpeg", "预设的壁纸"),
    W7("wallpaper7.jpeg", "预设的壁纸"),
    W8("wallpaper8.jpeg", "预设的壁纸"),
    W9("wallpaper9.jpeg", "预设的壁纸"),

    ;

    /**
     * 配置键
     */
    private final String path;

    /**
     * 默认值
     */
    private final String description;


    WallpaperEnum(String key, String defaultValue) {
        this.path = key;
        this.description = defaultValue;
    }

    public String key() {
        return path;
    }

    public String defaultValue() {
        return description;
    }

} 