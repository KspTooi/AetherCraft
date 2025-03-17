package com.ksptool.ql.commons.enums;

/**
 * 系统全局配置枚举
 */
public enum GlobalConfigEnum {


    ALLOW_USER_REGISTER("allow.user.register","false","允许用户注册账号 true-允许 -false禁止"),

    PAGE_LOGIN_BRAND("page.login.brand","Aether Craft","登录页展示的品牌名"),
    PAGE_TOP_BAR_BRAND("page.top.bar.brand","Aether Craft","导航栏展示的品牌名"),


    USER_FILE_STORAGE_PATH("user.file.storage.path", "/userdata/res", "用户文件的存储位置(相对路径)"),
    MODEL_CHAT_GEN_THREAD_TITLE("model.chat.gen.thread.title", "true", "是否自动生成会话标题，true-生成，false-不生成"),
    MODEL_CHAT_GEN_THREAD_PROMPT("model.chat.gen.thread.prompt", "总结内容并生成一个简短的标题(不超过10个字符),请直接回复标题,不要回复其他任何多余的话! 需总结的内容:#{content}", "生成会话标题的提示语模板，#{content}将被替换为用户的第一条消息"),

    // 壁纸缓存相关配置（全局配置）
    CUSTOMIZE_WALLPAPER_CACHE_SECONDS("customize.wallpaper.cache.seconds", "60", "壁纸缓存时间(秒)"),
    CUSTOMIZE_WALLPAPER_DEFAULT_PATH("customize.wallpaper.default.path", "/img/bg1.jpg", "默认壁纸路径"),

    // 角色扮演系统相关配置
    MODEL_RP_PROMPT_MAIN("model.rp.prompt.main", "扮演 #{model}。在与 #{user} 的虚构对话中，生成 #{model} 的下一条回复。", "RP-主提示词"),
    MODEL_RP_PROMPT_ROLE("model.rp.prompt.role.model", """
            #{userDesc}
            #{modelDescription}
            #{modelRoleSummary}
            #{modelScenario}
            """, "RP-角色设定提示词"),

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