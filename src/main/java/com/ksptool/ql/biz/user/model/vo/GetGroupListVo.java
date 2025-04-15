package com.ksptool.ql.biz.user.model.vo;

import java.util.Date;
import lombok.Data;

@Data
public class GetGroupListVo {

    // 组ID
    private Long id;

    // 组标识
    private String code;

    // 组名称
    private String name;

    // 成员数量
    private Integer memberCount;

    // 权限节点数量
    private Integer permissionCount;

    // 系统内置组
    private Boolean isSystem;

    // 组状态：0-禁用，1-启用
    private Integer status;

    // 创建时间
    private Date createTime;
}
