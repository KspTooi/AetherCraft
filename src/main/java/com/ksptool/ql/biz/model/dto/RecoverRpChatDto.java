package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecoverRpChatDto {

    //模型角色ID
    @NotNull(message = "模型角色ID不能为空")
    private Long modelRoleId;

    //AI模型代码
    @NotBlank(message = "AI模型代码不能为空")
    private String modelCode;

}
