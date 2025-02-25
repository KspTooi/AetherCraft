package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建/更新权限的数据传输对象
 */
@Data
public class CreatePermissionDto {
    
    /**
     * ID，创建时为空，更新时必填
     */
    private Long id;

    /**
     * 权限标识
     */
    @NotBlank(message = "权限标识不能为空")
    private String code;

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    private String name;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 排序号
     */
    private Integer sortOrder = 0;
    
    /**
     * 是否系统权限 0:非系统权限 1:系统权限
     */
    private Integer isSystem = 0;
} 