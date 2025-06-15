package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegenerateDto {

    @NotNull
    private Long threadId;

    @NotNull
    // 模型变体ID
    private Long modelVariantId;

    //根消息(只能是用户消息) -1为最后一条用户消息
    @NotNull
    private Long rootMessageId;

}
