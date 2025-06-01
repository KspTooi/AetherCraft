package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetModelVariantParamListVo {
    
    // 参数键
    private String paramKey;
    
    // 全局值
    private String globalVal;
    
    // 用户值
    private String userVal;
    
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