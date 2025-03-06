package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.Date;

/**
 * API密钥列表响应数据
 */
@Data
public class ListApiKeyVo {
    
    // 密钥ID
    private Long id;
    
    // 密钥名称
    private String keyName;
    
    // 密钥类型
    private String keyType;
    
    // 是否共享：0-不共享，1-共享
    private Integer isShared;
    
    // 使用次数
    private Long usageCount;
    
    // 最后使用时间
    private Date lastUsedTime;
    
    // 状态：0-禁用，1-启用
    private Integer status;
} 