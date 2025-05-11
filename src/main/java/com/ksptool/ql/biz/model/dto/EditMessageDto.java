package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EditMessageDto {

    //消息ID
    @NotNull
    private Long messageId;

    //消息内容
    @NotBlank
    private String content;

}
