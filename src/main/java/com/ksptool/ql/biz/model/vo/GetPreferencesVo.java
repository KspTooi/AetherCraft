package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetPreferencesVo {

    //用户前端当前访问的路径
    private String clientPath;

    //个性化-边栏-路径
    private String customizePathSide;

    //个性化-背景-TAB
    private String customizePathTabWallpaper;

    //个性化-主题-TAB
    private String customizePathTabTheme;
    
    //客户端角色设计器-当前正在设计的角色ID
    private String modelRoleEditCurrentId;

    //角色设计器-TAB
    private String modelRoleEditPathTab;
}
