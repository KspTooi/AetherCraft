package com.ksptool.ql.commons.enums;

/**
 * 系统权限节点枚举
 */
public enum PermissionEnum {

    /**
     * 管理台权限(Panel)
     */
    PANEL_ACCESS("panel:access", "允许访问管理台"),

    // 用户管理权限
    PANEL_USER_VIEW("panel:user:view", "管理台:查看用户列表"),
    PANEL_USER_ADD("panel:user:add", "管理台:添加用户"),
    PANEL_USER_EDIT("panel:user:edit", "管理台:编辑用户"),
    PANEL_USER_DELETE("panel:user:delete", "管理台:删除用户"),

    // 用户组管理权限
    PANEL_GROUP_VIEW("panel:group:view", "管理台:查看用户组"),
    PANEL_GROUP_ADD("panel:group:add", "管理台:添加用户组"),
    PANEL_GROUP_EDIT("panel:group:edit", "管理台:编辑用户组"),
    PANEL_GROUP_DELETE("panel:group:delete", "管理台:删除用户组"),
    PANEL_GROUP_ASSIGN("panel:group:assign", "管理台:为用户组分配权限"),

    // 权限节点管理
    PANEL_PERMISSION_VIEW("panel:permission:view", "管理台:查看权限节点"),
    PANEL_PERMISSION_ADD("panel:permission:add", "管理台:添加权限节点"),
    PANEL_PERMISSION_EDIT("panel:permission:edit", "管理台:编辑权限节点"),
    PANEL_PERMISSION_REMOVE("panel:permission:remove", "管理台:移除权限"),

    // 面板应用管理权限
    PANEL_APP_VIEW("panel:app:view", "管理台:查看应用"),
    PANEL_APP_ADD("panel:app:add", "管理台:添加应用"),
    PANEL_APP_EDIT("panel:app:edit", "管理台:编辑应用"),
    PANEL_APP_DELETE("panel:app:delete", "管理台:删除应用"),

    //配置项管理
    PANEL_CONFIG_VIEW("panel:config:view", "管理台:查看自己的配置项"),
    PANEL_CONFIG_VIEW_GLOBAL("panel:config:view:global", "管理台:查看全部用户的配置项"),
    PANEL_CONFIG_ADD("panel:config:add", "管理台:创建配置项"),
    PANEL_CONFIG_EDIT("panel:config:edit", "管理台:编辑配置项"),
    PANEL_CONFIG_REMOVE("panel:config:remove", "管理台:移除配置项"),

    // AI模型配置权限
    PANEL_MODEL_VIEW("panel:model:view", "管理台:查看AI配置"),
    PANEL_MODEL_EDIT("panel:model:edit", "管理台:编辑AI配置"),
    PANEL_MODEL_EDIT_GLOBAL_PROXY("panel:model:edit:global:proxy", "管理台:编辑AI全局代理配置"),
    PANEL_MODEL_EDIT_USER_PROXY("panel:model:edit:user:proxy", "管理台:编辑AI用户代理配置"),

    // 维护工具权限
    PANEL_MAINTAIN_VIEW("panel:maintain:view", "管理台:访问维护工具"),
    PANEL_MAINTAIN_PERMISSION("panel:maintain:permission", "管理台:校验系统权限"),



    /**
     * 管理台权限(Admin)
     */
    ADMIN_ACCESS("admin:access", "允许访问管理台(admin)"),

    //权限管理
    ADMIN_PERMISSION_VIEW("admin:permission:view", "查看权限列表"),
    ADMIN_PERMISSION_SAVE("admin:permission:save", "新增/编辑权限"),
    ADMIN_PERMISSION_REMOVE("admin:permission:remove", "移除权限"),

    //用户管理
    ADMIN_USER_VIEW("admin:user:view", "查看用户列表"),
    ADMIN_USER_SAVE("admin:user:save", "新增/编辑用户"), 
    ADMIN_USER_DELETE("admin:user:delete", "删除用户"),

    //玩家管理
    ADMIN_PLAYER_VIEW("admin:player:view", "查看玩家列表"),
    ADMIN_PLAYER_SAVE("admin:player:save", "新增/编辑玩家"),
    ADMIN_PLAYER_DELETE("admin:player:delete", "删除玩家"),
    
    //新玩家默认访问组管理
    ADMIN_PLAYER_DEFAULT_GROUP_VIEW("admin:player:default:group:view", "查看新玩家默认访问组"),
    ADMIN_PLAYER_DEFAULT_GROUP_ADD("admin:player:default:group:add", "添加新玩家默认访问组"),
    ADMIN_PLAYER_DEFAULT_GROUP_REMOVE("admin:player:default:group:remove", "移除新玩家默认访问组"),

    //用户组管理  
    ADMIN_GROUP_VIEW("admin:group:view", "查看用户组"),
    ADMIN_GROUP_SAVE("admin:group:save", "新增/编辑用户组"), 
    ADMIN_GROUP_DELETE("admin:group:delete", "删除用户组"),
    ADMIN_GROUP_ASSIGN("admin:group:assign", "为用户组分配权限"),

    //配置管理
    ADMIN_CONFIG_VIEW("admin:config:view", "查看配置项"),
    ADMIN_CONFIG_SAVE("admin:config:save", "新增/编辑配置项"), 
    ADMIN_CONFIG_REMOVE("admin:config:remove", "移除配置项"),

    //模型配置管理
    ADMIN_MODEL_VIEW("admin:model:view", "查看模型配置"),
    ADMIN_MODEL_EDIT("admin:model:edit", "编辑模型配置"),
    ADMIN_MODEL_TEST("admin:model:test", "测试模型连接"),

    //模型变体管理
    ADMIN_MODEL_VARIANT_VIEW("admin:model:variant:view", "查看模型变体"),
    ADMIN_MODEL_VARIANT_SAVE("admin:model:variant:save", "新增/编辑模型变体"),
    ADMIN_MODEL_VARIANT_REMOVE("admin:model:variant:remove", "删除模型变体"),

    //模型变体参数管理
    ADMIN_MODEL_VARIANT_PARAM_VIEW("admin:model:variant:param:view", "查看模型变体参数"),
    ADMIN_MODEL_VARIANT_PARAM_SAVE_GLOBAL("admin:model:variant:param:save:global", "新增/编辑全局模型变体参数"),
    ADMIN_MODEL_VARIANT_PARAM_SAVE_SELF("admin:model:variant:param:save:player", "新增/编辑个人模型变体参数"),
    ADMIN_MODEL_VARIANT_PARAM_REMOVE_GLOBAL("admin:model:variant:param:remove:global", "删除全局模型变体参数"),
    ADMIN_MODEL_VARIANT_PARAM_REMOVE_SELF("admin:model:variant:param:remove:player", "删除个人模型变体参数"),

    //模型变体参数模板管理
    ADMIN_MODEL_VARIANT_PARAM_TEMPLATE_VIEW("admin:model:variant:param:template:view", "查看模型变体参数模板"),
    ADMIN_MODEL_VARIANT_PARAM_TEMPLATE_SAVE("admin:model:variant:param:template:save", "新增/编辑模型变体参数模板"),
    ADMIN_MODEL_VARIANT_PARAM_TEMPLATE_DELETE("admin:model:variant:param:template:delete", "删除模型变体参数模板"),
    ADMIN_MODEL_VARIANT_PARAM_TEMPLATE_APPLY_GLOBAL("admin:model:variant:param:template:apply:global", "应用参数模板到全局"),
    ADMIN_MODEL_VARIANT_PARAM_TEMPLATE_APPLY_PLAYER("admin:model:variant:param:template:apply:player", "应用参数模板到个人"),

    //模型变体参数模板值管理
    ADMIN_MODEL_VARIANT_PARAM_TEMPLATE_VALUE_VIEW("admin:model:variant:param:template:value:view", "查看模型变体参数模板值"),
    ADMIN_MODEL_VARIANT_PARAM_TEMPLATE_VALUE_SAVE("admin:model:variant:param:template:value:save", "新增/编辑模型变体参数模板值"),
    ADMIN_MODEL_VARIANT_PARAM_TEMPLATE_VALUE_DELETE("admin:model:variant:param:template:value:delete", "删除模型变体参数模板值"),
    
    //系统维护  
    ADMIN_MAINTAIN_PERMISSION("admin:maintain:permission", "校验系统权限"),
    ADMIN_MAINTAIN_GROUP("admin:maintain:group", "校验系统用户组"),
    ADMIN_MAINTAIN_USER("admin:maintain:user", "校验系统用户"),
    ADMIN_MAINTAIN_CONFIG("admin:maintain:config", "校验系统配置"),
    ADMIN_MAINTAIN_FORCE_CREATE_PLAYER("admin:maintain:force:create:player", "为没有Player的用户强制创建Player"),
    ADMIN_MAINTAIN_MODEL_VARIANT("admin:maintain:model:variant", "校验模型变体配置"),

    ADMIN_MAINTAIN_ACTUATOR("admin:maintain:actuator", "访问actuator端点"),

    //API密钥管理
    ADMIN_APIKEY_VIEW("admin:apikey:view", "查看API密钥"),
    ADMIN_APIKEY_SAVE("admin:apikey:save", "新增/编辑API密钥"),
    ADMIN_APIKEY_DELETE("admin:apikey:delete", "删除API密钥"),
    ADMIN_APIKEY_AUTH_VIEW("admin:apikey:auth:view", "查看API密钥授权"),
    ADMIN_APIKEY_AUTH_SAVE("admin:apikey:auth:save", "新增/编辑API密钥授权"),
    ADMIN_APIKEY_AUTH_REMOVE("admin:apikey:auth:remove", "移除API密钥授权"),

    //会话管理
    ADMIN_SESSION_VIEW("admin:session:view", "查看会话列表"),
    ADMIN_SESSION_CLOSE("admin:session:close", "关闭会话"),

    /**
     * 客户端权限
     */
    //AI聊天AGENT功能
    MODEL_CHAT_VIEW("model:chat:view", "访问AI助手"),
    MODEL_CHAT_EDIT_THREAD("model:chat:edit:thread", "编辑对话标题"),
    MODEL_CHAT_CREATE_THREAD("model:chat:create:thread", "创建新对话"),
    MODEL_CHAT_REMOVE_THREAD("model:chat:remove:thread", "移除对话"),
    MODEL_CHAT_REMOVE_HISTORY("model:chat:remove:history", "移除消息历史记录"),
    MODEL_CHAT_MESSAGE("model:chat:message", "发送消息"),
    MODEL_CHAT_TOGGLE_MODEL("model:chat:toggle_model", "切换模型"),
    MODEL_CHAT_FUNC("model:chat:func", "AI执行函数"),
    MODEL_CHAT_TOOLS("model:chat:tools", "AI调用工具"),
    MODEL_CHAT_CREATE("model:chat:create", "AI创建文件"),
    MODEL_CHAT_EDIT("model:chat:edit", "AI编辑文件"),
    MODEL_CHAT_REMOVE("model:chat:remove", "AI移除文件"),
    MODEL_CONTROL_MOUSE("model:control:mouse", "AI接管鼠标"),
    MODEL_CONTROL_KEYBOARD("model:control:keyboard", "AI接管键盘"),


    //其他
    CUSTOMIZE("customize:view", "允许访问个性化"),
    DESKTOP("desktop:view", "允许访问桌面"),
    ;



    private final String code;
    private final String description;

    PermissionEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据权限代码获取枚举值
     * @param code 权限代码
     * @return 对应的枚举值，如果不存在返回null
     */
    public static PermissionEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (PermissionEnum permission : values()) {
            if (permission.getCode().equals(code)) {
                return permission;
            }
        }
        return null;
    }
} 