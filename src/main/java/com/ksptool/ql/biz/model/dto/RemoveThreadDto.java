package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除会话DTO
 */
@Data
public class RemoveThreadDto {
    
    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long threadId;
} 