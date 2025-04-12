package com.ksptool.ql.biz.model.dto;

import lombok.Data;

@Data
public class SavePreferencesDto {

    //用户前端当前访问的路径
    private String clientPath;

    //个性化-边栏-路径
    private String customizePathSide;

    //个性化-背景-TAB
    private String customizePathTabWallpaper;

    //个性化-主题-TAB
    private String customizePathTabTheme;

}
