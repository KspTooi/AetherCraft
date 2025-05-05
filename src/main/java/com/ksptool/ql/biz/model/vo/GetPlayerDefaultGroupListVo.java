package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class GetPlayerDefaultGroupListVo {

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


    public GetPlayerDefaultGroupListVo(Long id, String code, String name, Integer memberCount, Integer permissionCount,
                                       Boolean isSystem, Integer status, Date createTime) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.memberCount = memberCount;
        this.permissionCount = permissionCount;
        this.isSystem = isSystem;
        this.status = status;
        this.createTime = createTime;
    }
}
