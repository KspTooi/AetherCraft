package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetAppDetailsVo {
    private Long id;
    private String name;
    private String kind;
    private String execPath;
    private String iconPath;
    private String command;
    private String description;
    private Integer launchCount;
} 