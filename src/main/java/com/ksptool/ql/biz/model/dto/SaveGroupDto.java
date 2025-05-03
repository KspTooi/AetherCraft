package com.ksptool.ql.biz.model.dto;
import lombok.Data;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
public class SaveGroupDto {

    // 组ID
    private Long id;

    // 组标识
    @NotBlank(message = "组标识不能为空")
    @Length(min = 2, max = 50, message = "组标识长度必须在2-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z_]*$", message = "组标识只能包含英文字符和下划线，且必须以字母开头")
    private String code;

    // 组名称
    @NotBlank(message = "组名称不能为空")
    @Length(min = 2, max = 50, message = "组名称长度必须在2-50个字符之间")
    private String name;

    // 组描述
    @Length(max = 200, message = "组描述长度不能超过200个字符")
    private String description;

    // 组状态：0-禁用，1-启用
    @NotNull(message = "组状态不能为空")
    @Min(value = 0, message = "状态值不正确")
    @Max(value = 1, message = "状态值不正确")
    private Integer status;

    // 排序号
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号必须大于等于0")
    private Integer sortOrder;

    //权限ID列表
    private List<Long> permissionIds;
}
