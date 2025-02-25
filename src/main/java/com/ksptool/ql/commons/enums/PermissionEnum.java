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
    PANEL_PERMISSION_REMOVE("panel:permission:remove", "管理台:移除权限"),

    // 面板应用管理权限
    PANEL_APP_VIEW("panel:app:view", "管理台:查看应用"),
    PANEL_APP_ADD("panel:app:add", "管理台:添加应用"),
    PANEL_APP_EDIT("panel:app:edit", "管理台:编辑应用"),
    PANEL_APP_DELETE("panel:app:delete", "管理台:删除应用"),

    //配置项管理
    PANEL_CONFIG_VIEW("panel:config:view", "管理台:查看自己的配置项"),
    PANEL_CONFIG_VIEW_GLOBAL("panel:config:view:global", "管理台:查看全部用户的配置项"),
    PANEL_CONFIG_EDIT("panel:config:edit", "管理台:编辑配置项"),
    PANEL_CONFIG_REMOVE("panel:config:remove", "管理台:移除配置项"),

    // AI模型配置权限
    PANEL_MODEL_VIEW("panel:model:view", "管理台:查看AI配置"),
    PANEL_MODEL_EDIT("panel:model:edit", "管理台:编辑AI配置"),
    
    // 维护工具权限
    PANEL_MAINTAIN_VIEW("panel:maintain:view", "管理台:访问维护工具"),
    PANEL_MAINTAIN_PERMISSION("panel:maintain:permission", "管理台:校验系统权限"),

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

    //任务管理器
    TASK_MGR_VIEW("task:mgr:view", "访问任务管理器"),
    TASK_MGR_SEARCH("task:mgr:search", "任务管理器关键字搜索"),
    TASK_MGR_KILL("task:mgr:kill", "终止任务"),

    //文件资源管理器权限
    EXPLORER_VIEW("explorer:view", "访问文件资源管理器"),
    EXPLORER_UPLOAD("explorer:upload", "上传文件"),
    EXPLORER_DOWNLOAD("explorer:download", "下载文件"),
    EXPLORER_OPEN("explorer:open", "打开文件"),
    EXPLORER_RENAME("explorer:rename", "重命名文件"),
    EXPLORER_DELETE("explorer:delete", "删除文件"),

    //服务权限
    SERVICE_VIEW("service:view", "访问文件资源管理器"),
    SERVICE_START("service:start", "启动服务"),
    SERVICE_KILL("service:kill", "停止服务"),

    //AI聊天功能
    MODEL_CHAT_VIEW("model:chat:view", "访问AI助手"),
    MODEL_CHAT_CREATE_THREAD("model:chat:create:thread", "创建新对话"),
    MODEL_CHAT_REMOVE_THREAD("model:chat:remove:thread", "移除对话"),
    MODEL_CHAT_MESSAGE("model:chat:message", "发送消息"),
    MODEL_CHAT_TOGGLE_MODEL("model:chat:toggle_model", "切换模型"),
    MODEL_CHAT_FUNC("model:chat:func", "AI执行函数"),
    MODEL_CHAT_TOOLS("model:chat:tools", "AI调用工具"),
    MODEL_CHAT_CREATE("model:chat:create", "AI创建文件"),
    MODEL_CHAT_EDIT("model:chat:edit", "AI编辑文件"),
    MODEL_CHAT_REMOVE("model:chat:remove", "AI移除文件"),

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