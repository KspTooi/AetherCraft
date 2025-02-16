package com.ksptool.ql.biz.model.vo;

import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.EntityResult;
import jakarta.persistence.FieldResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.ColumnResult;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户组列表视图对象
 */
@Data
public class ListPanelGroupVo {

    public ListPanelGroupVo(Long id,
                            String name,
                            String code,
                            String description,
                            Integer status,
                            Boolean isSystem,
                            Integer sortOrder,
                            Integer memberCount,
                            Integer permissionCount) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.status = status;
        this.isSystem = isSystem;
        this.sortOrder = sortOrder;
        this.memberCount = memberCount;
        this.permissionCount = permissionCount;
    }

    // 用户组ID
    private Long id;
    
    // 用户组名称
    private String name;
    
    // 用户组标识
    private String code;
    
    // 用户组描述
    private String description;
    
    // 用户组状态：1-启用，0-禁用
    private Integer status;
    
    // 是否为系统用户组
    private Boolean isSystem;
    
    // 排序号
    private Integer sortOrder;
    
    // 成员数量
    private Integer memberCount;

    // 权限节点数量
    private Integer permissionCount;
} 