package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class EditPanelGroupVo {
    // 用户组基本信息
    private Long id;
    // 用户组标识
    private String code;
    // 用户组名称
    private String name;
    // 用户组描述
    private String description;
    // 用户组状态：0-禁用，1-启用
    private Integer status;
    // 是否系统内置组
    private Boolean isSystem;
    // 排序号
    private Integer sortOrder;
    // 权限列表
    private List<EditPanelGroupPermissionVo> permissions;
} 