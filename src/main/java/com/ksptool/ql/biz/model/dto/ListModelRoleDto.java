package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型角色列表查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ListModelRoleDto extends PageQuery {
    
    /**
     * 当前选中的角色ID
     */
    private Long id;

    /**
     * 是否为新建角色模式
     */
    private Boolean isNew = false;
    
    /**
     * 角色名称或描述（模糊查询）
     */
    private String keyword;
} 