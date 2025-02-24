package com.ksptool.ql.commons.enums;

/**
 * 系统权限节点枚举
 */
public enum PermissionEnum {
    // 系统级权限
    SYSTEM_ACCESS("system:access", "访问系统"),
    SYSTEM_ADMIN("system:admin", "系统管理员"),
    
    // 用户管理权限
    USER_VIEW("user:view", "查看用户"),
    USER_ADD("user:add", "添加用户"),
    USER_EDIT("user:edit", "编辑用户"),
    USER_DELETE("user:delete", "删除用户"),
    
    // 用户组管理权限
    GROUP_VIEW("group:view", "查看用户组"),
    GROUP_ADD("group:add", "添加用户组"),
    GROUP_EDIT("group:edit", "编辑用户组"),
    GROUP_DELETE("group:delete", "删除用户组"),
    
    // 应用管理权限
    APP_VIEW("app:view", "查看应用"),
    APP_ADD("app:add", "添加应用"),
    APP_EDIT("app:edit", "编辑应用"),
    APP_DELETE("app:delete", "删除应用"),
    APP_LAUNCH("app:launch", "启动应用"),
    
    // 面板应用管理权限
    PANEL_APP_VIEW("panel:app:view", "查看面板应用"),
    PANEL_APP_ADD("panel:app:add", "添加面板应用"),
    PANEL_APP_EDIT("panel:app:edit", "编辑面板应用"),
    PANEL_APP_DELETE("panel:app:delete", "删除面板应用"),
    
    // 文件浏览器权限
    FILE_VIEW("file:view", "查看文件"),
    FILE_UPLOAD("file:upload", "上传文件"),
    FILE_DOWNLOAD("file:download", "下载文件"),
    FILE_DELETE("file:delete", "删除文件"),
    FILE_RENAME("file:rename", "重命名文件"),
    
    // 配置管理权限
    CONFIG_VIEW("config:view", "查看配置"),
    CONFIG_EDIT("config:edit", "编辑配置"),
    
    // AI模型配置权限
    AI_CONFIG_VIEW("ai:config:view", "查看AI配置"),
    AI_CONFIG_EDIT("ai:config:edit", "编辑AI配置"),
    
    // 权限节点管理
    PERMISSION_VIEW("permission:view", "查看权限节点"),
    PERMISSION_ASSIGN("permission:assign", "分配权限");

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