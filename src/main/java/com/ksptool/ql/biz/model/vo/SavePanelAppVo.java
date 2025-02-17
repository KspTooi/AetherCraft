package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class SavePanelAppVo {
    // 应用ID
    private Long id;

    // 应用名称
    private String name;

    // 应用类型
    private String kind;

    // 程序路径
    private String execPath;

    // 图标路径
    private String iconPath;

    // 快捷命令
    private String command;

    // 描述
    private String description;
} 