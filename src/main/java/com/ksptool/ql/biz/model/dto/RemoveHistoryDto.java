package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除历史消息DTO
 */
@Data
public class RemoveHistoryDto {
    
    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long threadId;
    
    /**
     * 历史消息ID
     */
    @NotNull(message = "历史消息ID不能为空")
    private Long historyId;
} 