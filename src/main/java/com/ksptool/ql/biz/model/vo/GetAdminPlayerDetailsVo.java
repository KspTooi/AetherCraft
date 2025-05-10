package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class GetAdminPlayerDetailsVo {

    //人物ID
    private Long id;

    //头像路径
    private String avatarUrl;

    //人物名称
    private String name;

    //人物性别 0:男 1:女 2:不愿透露 4:自定义(男性) 5:自定义(女性) 6:自定义(其他)
    private Integer gender;

    //所有者
    private String username;

    //个人信息
    private String publicInfo;

    //余额
    private BigDecimal balance;

    //语言
    private String language;

    //年代
    private String era;

    //内容过滤等级
    private Integer contentFilterLevel;

    //状态: 0:正在使用 1:不活跃 2:等待删除 3:已删除
    private Integer status;

    //移除申请提交时间
    private Date removalRequestTime;

    //角色移除时间
    private Date removedTime;

    //最后激活时间
    private Date lastActiveTime;

    //诞生日期
    private Date createTime;

    //拥有的访问组ID
    private List<Long> groupIds;

}
