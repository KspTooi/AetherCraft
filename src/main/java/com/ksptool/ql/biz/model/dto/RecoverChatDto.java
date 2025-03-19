package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecoverChatDto {

    /**
     * 指定要加载的会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long threadId;

}
