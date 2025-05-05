package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class EditAttachPlayerDetailsDto {

    //人物ID
    @NotNull
    private Long id;

    //头像路径
    private String avatarUrl;

    //(明文)个人信息
    private String publicInfo;

    //(密文)人物角色描述
    private String description;

    //语言
    private String language;

    //年代
    private String era;

    //内容过滤等级
    private Integer contentFilterLevel;

}
