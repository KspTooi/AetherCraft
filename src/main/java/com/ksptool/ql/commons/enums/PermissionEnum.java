package com.ksptool.ql.commons.enums;

/**
 * 系统权限节点枚举
 */
public enum PermissionEnum {

    /**
     * 管理台权限(Panel)
     */
    // 用户管理权限
    PANEL_USER_VIEW("panel:user:view", "查看用户列表"),
    PANEL_USER_ADD("panel:user:add", "添加用户"),
    PANEL_USER_EDIT("panel:user:edit", "编辑用户"),
    PANEL_USER_DELETE("panel:user:delete", "删除用户"),
    
    // 用户组管理权限
    PANEL_GROUP_VIEW("panel:group:view", "查看用户组"),
    PANEL_GROUP_ADD("panel:group:add", "添加用户组"),
    PANEL_GROUP_EDIT("panel:group:edit", "编辑用户组"),
    PANEL_GROUP_DELETE("panel:group:delete", "删除用户组"),
    PANEL_GROUP_ASSIGN("panel:group:assign", "为用户组分配权限"),

    // 权限节点管理
    PANEL_PERMISSION_VIEW("panel:permission:view", "查看权限节点"),
    PANEL_PERMISSION_ADD("panel:permission:add", "添加权限节点"),
    PANEL_PERMISSION_REMOVE("panel:permission:remove", "移除权限"),

    // 面板应用管理权限
    PANEL_APP_VIEW("panel:app:view", "查看面板应用"),
    PANEL_APP_ADD("panel:app:add", "添加面板应用"),
    PANEL_APP_EDIT("panel:app:edit", "编辑面板应用"),
    PANEL_APP_DELETE("panel:app:delete", "删除面板应用"),

    //配置项管理
    PANEL_CONFIG_VIEW("panel:config:view", "查看自己的配置项"),
    PANEL_CONFIG_VIEW_GLOBAL("panel:config:view:global", "查看全部用户的配置项"),
    PANEL_CONFIG_EDIT("panel:config:edit", "编辑配置项"),
    PANEL_CONFIG_REMOVE("panel:config:remove", "移除配置项"),

    // AI模型配置权限
    PANEL_MODEL_VIEW("panel:model:view", "查看AI配置"),
    PANEL_MODEL_EDIT("panel:model:edit", "编辑AI配置"),

    /**
     * 客户端权限
     */
    //应用中心
    APP_VIEW("app:view", "查看应用"),
    APP_ADD("app:add", "添加应用"),
    APP_LAUNCH("app:launch", "启动应用"),
    APP_KILL("app:kill", "终止应用"),
    APP_EDIT("app:edit", "配置应用"),
    APP_REMOVE("app:remove", "移除应用"),




    // 文件浏览器权限
    EXPLORER_VIEW("explorer:view", "查看文件"),
    EXPLORER_UPLOAD("explorer:upload", "上传文件"),
    EXPLORER_DOWNLOAD("explorer:download", "下载文件"),
    EXPLORER_DELETE("explorer:delete", "删除文件"),
    EXPLORER_RENAME("explorer:rename", "重命名文件");




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