package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeActiveThreadDto {
    
    // 角色对话ID
    @NotNull(message = "角色对话ID不能为空")
    private Long threadId;
} 