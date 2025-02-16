package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.Date;

@Data
public class ListPanelAppVo {
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

    // 运行次数
    private Integer launchCount;

    // 最后运行时间
    private Date lastLaunchTime;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

    public ListPanelAppVo() {
    }

    public ListPanelAppVo(Long id, String name, String kind, String execPath, String iconPath, 
                         String command, String description, Integer launchCount, Date lastLaunchTime, 
                         Date createTime, Date updateTime) {
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.execPath = execPath;
        this.iconPath = iconPath;
        this.command = command;
        this.description = description;
        this.launchCount = launchCount;
        this.lastLaunchTime = lastLaunchTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
} 