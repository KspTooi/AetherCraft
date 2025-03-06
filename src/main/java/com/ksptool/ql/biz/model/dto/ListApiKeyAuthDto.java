package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * API密钥授权列表查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ListApiKeyAuthDto extends PageQuery {
    
    // API密钥ID
    private Long apiKeyId;
    
    // 被授权人用户名
    private String authorizedUserName;
} 