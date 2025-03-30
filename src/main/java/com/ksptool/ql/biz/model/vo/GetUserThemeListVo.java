package com.ksptool.ql.biz.model.vo;

import lombok.Data;


@Data
public class GetUserThemeListVo {

    //主题ID
    private Long id;

    //主题名称
    private String themeName;

    //主题描述
    private String description;

    //是否为默认主题（0-否，1-是）
    private Integer isActive = 0;

    //是否为系统预设主题（0-否，1-是）
    private Integer isSystem = 0;

}
