package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class AppItemVo {

    private Long id; //应用ID

    private String name; //应用名称

    private String kind; //应用类型 0:EXE 1:BAT

    private String execPath;

    private String iconPath;

    private Integer launchCount;

    private Date lastLaunchTime;

}
