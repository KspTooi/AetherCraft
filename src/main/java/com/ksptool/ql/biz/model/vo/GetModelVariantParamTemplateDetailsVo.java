package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class GetModelVariantParamTemplateDetailsVo {
    
    // 模板ID
    private Long id;
    
    // 模板名称
    private String name;
    
    // 创建时间
    private Date createTime;
    
    // 更新时间
    private Date updateTime;
    
} 