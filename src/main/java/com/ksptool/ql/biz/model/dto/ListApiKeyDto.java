package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * API密钥列表查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ListApiKeyDto extends PageQuery {
    
    // 密钥名称
    private String keyName;
    
    // 密钥类型
    private String keyType;
    
    // 状态：0-禁用，1-启用
    private Integer status;
} 