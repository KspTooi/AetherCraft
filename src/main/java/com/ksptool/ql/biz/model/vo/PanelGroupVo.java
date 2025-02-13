package com.ksptool.ql.biz.model.vo;

import com.ksptool.ql.biz.model.po.PermissionPo;
import lombok.Data;

import java.util.Set;

@Data
public class PanelGroupVo {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private Boolean isSystem;
    private Integer sortOrder;
    private Long memberCount;
    private Set<PermissionPo> permissions;
} 