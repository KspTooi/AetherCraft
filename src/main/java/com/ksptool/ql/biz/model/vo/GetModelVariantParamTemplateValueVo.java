package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class GetModelVariantParamTemplateValueVo {

    // 模板值ID
    private Long id;

    // 所属模板ID
    private Long templateId;

    // 参数键
    private String paramKey;

    // 参数值
    private String paramVal;

    // 参数类型 0:string 1:int 2:boolean
    private Integer type;

    // 参数描述
    private String description;

    // 排序号
    private Integer seq;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

} 