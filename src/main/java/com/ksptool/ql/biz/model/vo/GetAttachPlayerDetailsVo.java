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

    //性别 0:男 1:女 2:不愿透露 4:自定义(男性) 5:自定义(女性) 6:自定义(其他)
    private Integer gender;

    //(密文)自定义性别种类 gender为4 5 6时必填
    private String genderData;

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
