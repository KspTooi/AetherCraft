package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoveRpHistoryDto {

    // 消息历史ID
    @NotNull(message = "消息ID不能为空")
    private Long historyId;
} 