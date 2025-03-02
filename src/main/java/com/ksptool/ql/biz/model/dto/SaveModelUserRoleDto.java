package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 保存用户角色的数据传输对象
 */
@Data
public class SaveModelUserRoleDto {
    
    // 角色ID，新增时为null
    private Long id;
    
    // 角色名称
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;
    
    // 角色描述
    @Size(max = 5000, message = "角色描述长度不能超过5000个字符")
    private String description;
    
    // 排序号
    @NotNull(message = "排序号不能为空")
    private Integer sortOrder;
    
    // 是否默认角色（1:是，0:否）
    @NotNull(message = "请选择是否为默认角色")
    private Integer isDefault;
    
    // 角色头像路径
    private String avatarPath;
} 