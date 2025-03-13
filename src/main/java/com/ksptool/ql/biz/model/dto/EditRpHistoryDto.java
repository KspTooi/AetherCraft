package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 编辑RP对话历史记录的请求参数
 */
@Data
public class EditRpHistoryDto {

    // 历史记录ID
    @NotNull(message = "历史记录ID不能为空")
    private Long historyId;

    // 新的消息内容
    @NotBlank(message = "消息内容不能为空")
    private String content;
} 