package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GroupPermissionDefinitionVo {

    //权限节点ID
    private Long id;

    //权限节点标识
    private String code;

    //权限节点名称
    private String name;

    //当前组是否拥有 0:拥有 1:不拥有
    private Integer has;
}
