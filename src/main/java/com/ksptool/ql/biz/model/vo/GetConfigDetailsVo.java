package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class GetConfigDetailsVo {

    // 主键ID
    private Long id;

    // 用户ID
    private Long userId;

    // 用户名
    private String username;

    // 配置键
    private String configKey;

    // 配置值
    private String configValue;

    // 配置描述
    private String description;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

}
