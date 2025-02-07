package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetAppDetailsVo {
    private Long id;
    private String name;
    private Integer kind;
    private String execPath;
    private Integer launchCount;
} 