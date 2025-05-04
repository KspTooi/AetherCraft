package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * API密钥授权列表视图对象
 */
@Data
public class GetApiKeyAuthorizationListVo {

    public GetApiKeyAuthorizationListVo(Long id, String authorizedUserName, Long usageLimit, Long usageCount, Date expireTime, Integer status, Date createTime) {
        this.id = id;
        this.authorizedUserName = authorizedUserName;
        this.usageLimit = usageLimit;
        this.usageCount = usageCount;
        this.expireTime = expireTime;
        this.status = status;
        this.createTime = createTime;
    }

    public GetApiKeyAuthorizationListVo() {

    }

    // 授权ID
    private Long id;
    
    // 被授权人用户名
    private String authorizedUserName;
    
    // 使用次数限制
    private Long usageLimit;
    
    // 已使用次数
    private Long usageCount;
    
    // 过期时间
    private Date expireTime;
    
    // 状态：0-禁用，1-启用
    private Integer status;

    //创建时间
    private Date createTime;
}