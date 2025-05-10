package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class GetApiKeyAuthorizationDetailsVo {

    // 授权ID
    private Long id;

    // API密钥ID
    private Long apiKeyId;

    // 被授权者人物名
    private String authorizedPlayerName;

    // 使用次数限制
    private Long usageLimit;

    // 已使用次数
    private Long usageCount;

    // 过期时间
    private Date expireTime;

    // 状态：0-禁用，1-启用
    private Integer status;

    // 创建时间
    private Date createTime;

}
