package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 保存用户组的数据传输对象
 */
@Data
public class SavePanelGroupDto {
    
    /**
     * ID，创建时为空，更新时必填
     */
    private Long id;

    /**
     * 用户组标识
     */
    @NotBlank(message = "用户组标识不能为空")
    private String code;

    /**
     * 用户组名称
     */
    @NotBlank(message = "用户组名称不能为空")
    private String name;

    /**
     * 用户组描述
     */
    private String description;

    /**
     * 排序号
     */
    private Integer sortOrder = 0;

    /**
     * 用户组状态：0-禁用，1-启用
     */
    @NotNull(message = "用户组状态不能为空")
    private Integer status = 1;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;
} 