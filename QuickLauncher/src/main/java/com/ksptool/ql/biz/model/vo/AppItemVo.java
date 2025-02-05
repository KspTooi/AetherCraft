package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class AppItemVo {

    private Long id; //应用ID

    private String name; //应用名称

    private Integer kind; //应用类型 0:EXE 1:BAT

}
