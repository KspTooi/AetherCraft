package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class SavePanelGroupVo {
    // 用户组ID，创建时为null
    private Long id;
    // 用户组标识
    private String code;
    // 用户组名称
    private String name;
    // 用户组描述
    private String description;
    // 用户组状态：0-禁用，1-启用
    private Integer status = 1;
    // 是否系统内置组
    private Boolean isSystem = false;
    // 排序号
    private Integer sortOrder;
    // 下一个可用的排序号（仅在创建时使用）
    private Integer nextOrder;
    // 权限列表
    private List<SavePanelGroupPermissionVo> permissions;
} 