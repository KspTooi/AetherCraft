package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class GetModelVariantParamDetailsVo {
    
    // 参数ID
    private Long id;
    
    // 模型变体ID
    private Long modelVariantId;
    
    // 参数键
    private String paramKey;
    
    // 参数值
    private String paramVal;
    
    // 参数类型 0:string 1:int 2:boolean
    private Integer type;
    
    // 参数描述
    private String description;
    
    // 是否为全局默认参数 0:否 1:是
    private Integer global;
    
    // 用户ID，为空表示全局默认参数
    private Long userId;
    
    // 玩家ID，为空表示全局默认参数
    private Long playerId;
    
    // 排序号
    private Integer seq;
    
    // 创建时间
    private Date createTime;
    
    // 更新时间
    private Date updateTime;
    
} 