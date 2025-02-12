package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class SavePermissionVo {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
} 