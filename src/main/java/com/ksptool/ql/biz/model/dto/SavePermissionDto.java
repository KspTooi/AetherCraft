package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SavePermissionDto {
    /**
     * 权限ID，创建时为空
     */
    private Long id;
    
    /**
     * 权限代码
     */
    @NotEmpty(message = "权限标识不能为空")
    @Pattern(regexp = "^[a-z]([a-z0-9\\-]*[a-z0-9])*(:[a-z]([a-z0-9\\-]*[a-z0-9])*)*$", 
            message = "权限标识格式错误，只允许小写字母、数字、连字符，以及冒号作为分隔符")
    private String code;
    
    /**
     * 权限名称
     */
    @NotEmpty(message = "权限名称不能为空")
    private String name;
    
    /**
     * 权限描述
     */
    private String description;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
