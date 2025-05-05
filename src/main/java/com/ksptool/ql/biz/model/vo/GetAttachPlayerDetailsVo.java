package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class GetAttachPlayerDetailsVo {

    //人物ID
    private Long id;

    //头像路径
    private String avatarUrl;

    //人物名称
    private String name;

    //(明文)个人信息
    private String publicInfo;

    //(密文)人物角色描述
    private String description;

    //钱包余额(CU)
    private BigDecimal balance;

    //语言
    private String language;

    //年代
    private String era;

    //内容过滤等级
    private Integer contentFilterLevel;

    //状态: 0:正在使用 1:不活跃 2:等待删除 3:已删除
    private Integer status;

    //诞生日期
    private Date createTime;

}
