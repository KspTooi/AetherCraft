package com.ksptool.ql.commons.enums;

/**
 * 系统全局配置枚举
 */
public enum GlobalConfigEnum {

    APPLICATION_VERSION("application.version","1.0A","应用程序版本"),

    ALLOW_USER_REGISTER("allow.user.register","false","允许用户注册账号 true-允许 -false禁止"),

    ALLOW_INSTALL_WIZARD("allow.install.wizard","false","第一次进入时显示安装向导 true-允许执行安装向导 false-不执行安装向导"),

    ALLOW_INSTALL_WIZARD_UPGRADED("allow.install.wizard.upgraded","true","应用程序版本落后时是否允许执行升级向导 true-允许 false-拒绝"),

    PAGE_LOGIN_BRAND("page.login.brand","Aether Craft","登录页展示的品牌名"),
    PAGE_TOP_BAR_BRAND("page.top.bar.brand","Aether Craft","导航栏展示的品牌名"),
    PAGE_PANEL_BRAND("page.panel.brand","Aether Craft Admin","管理台展示的品牌名"),


    USER_FILE_STORAGE_PATH("user.file.storage.path", "/userdata/res", "用户文件的存储位置(相对路径)"),
    MODEL_CHAT_GEN_THREAD_TITLE("model.chat.gen.thread.title", "true", "是否自动生成会话标题，true-生成，false-不生成"),
    MODEL_CHAT_GEN_THREAD_PROMPT("model.chat.gen.thread.prompt.v1", "总结聊天对话内容,根据总结生成一个不超过10个字的简短标题,只回复标题内容,不要有多余的话.#内容 发送:#{userContent} 回复:#{modelContent}", "生成会话标题的提示语模板，#{userContent}将被替换为用户的第一条消息 #{modelContent}将会被替换为模型的回复"),

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

    MODEL_RP_PROMPT_EXAMPLE_CHAT_START("model.rp.prompt.example.chat.start","#示例对话#\n\n","RP-角色示例对话片段-开始 提示词"),
    MODEL_RP_PROMPT_EXAMPLE_CHAT_END("model.rp.prompt.example.chat.end","\n\n","RP-角色示例对话片段-结束 提示词"),

    MODEL_RP_PROMPT_EXAMPLE_CHAT_FINISH("model.rp.prompt.example.chat.finish","#示例对话结束#","RP-角色示例对话-结束 提示词"),

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