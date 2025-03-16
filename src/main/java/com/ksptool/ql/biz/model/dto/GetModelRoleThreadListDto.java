package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetModelRoleThreadListDto {

    // 模型角色ID
    @NotNull(message = "模型角色ID不能为空")
    private Long modelRoleId;
} 