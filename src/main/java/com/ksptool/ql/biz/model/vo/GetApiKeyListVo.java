package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * API密钥列表响应数据
 */
@Data
public class GetApiKeyListVo {

    public GetApiKeyListVo(Long id, String keyName, String keySeries, Integer isShared, Long authorizedCount, Long usageCount, Date lastUsedTime, Integer status) {
        this.id = id;
        this.keyName = keyName;
        this.keySeries = keySeries;
        this.isShared = isShared;
        this.authorizedCount = authorizedCount;
        this.usageCount = usageCount;
        this.lastUsedTime = lastUsedTime;
        this.status = status;
    }

    // 密钥ID
    private Long id;
    
    // 密钥名称
    private String keyName;
    
    // 密钥系列
    private String keySeries;
    
    // 是否共享：0-不共享，1-共享
    private Integer isShared;

    // 授权人数
    private Long authorizedCount;

    // 使用次数
    private Long usageCount;
    
    // 最后使用时间
    private Date lastUsedTime;
    
    // 状态：0-禁用，1-启用
    private Integer status;
} 