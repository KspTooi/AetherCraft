package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class EditAdminPlayerDto {

    //人物ID
    @NotNull
    private Long id;

    //头像路径
    private String avatarUrl;

    //个人信息
    private String publicInfo;

    //语言
    private String language;

    //年代
    private String era;

    //内容过滤等级
    private Integer contentFilterLevel;

    //状态: 1:不活跃 3:已删除 (后台仅允许设置这两个状态)
    @Min(value = 1, message = "状态值无效")
    @Max(value = 3, message = "状态值无效")
    private Integer status;

}
