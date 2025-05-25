package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateThreadDto {

    //模型代码
    @NotBlank
    private String modelCode;

    //Thread类型 0:标准会话 1:RP会话 2:标准增强会话
    @NotBlank
    private Integer type;

    //NpcId
    @NotBlank
    private Long npcId;

}
