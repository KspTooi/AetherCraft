package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class GetModelVariantParamTemplateListVo {

    public GetModelVariantParamTemplateListVo(Long id, String name, Long valueCount, Date createTime, Date updateTime) {
        this.id = id;
        this.name = name;
        this.valueCount = valueCount;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // 模板ID
    private Long id;

    // 模板名称
    private String name;
    
    // 参数值数量
    private Long valueCount;
    
    // 创建时间
    private Date createTime;
    
    // 更新时间
    private Date updateTime;
    
} 