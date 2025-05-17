package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EditThreadTitleDto {

    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long threadId;

    /**
     * 新标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;
} 