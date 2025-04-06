package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetModelRoleListVo {
    // 角色ID
    private Long id;
    
    // 角色名称
    private String name;
    
    // 头像路径
    private String avatarPath;
}