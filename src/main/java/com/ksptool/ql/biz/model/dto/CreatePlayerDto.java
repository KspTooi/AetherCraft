package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class CreatePlayerDto {

    //(明文)人物角色名称
    @NotBlank
    @Size(max = 24)
    private String name;

    //性别 0:男 1:女 2:不愿透露 4:自定义(男性) 5:自定义(女性) 6:自定义(其他)
    @NotNull
    @Range(min = 0, max = 6)
    private Integer gender;

    //(密文)自定义性别种类 gender为4 5 6时必填
    private String genderData;

    //(明文)个人信息
    @Size(max = 40000)
    private String publicInfo;

    //(密文)人物角色描述
    @Size(max = 32000)
    private String description;

    //语言 如 中文,English,zh-CN,en-US
    @NotBlank
    @Size(max = 24)
    private String language;

    //年代 如 古代,中世纪,现代,未来,赛博朋克,80S,90S,2025-01-01
    @Size(max = 32)
    private String era;

    //内容过滤等级 0:不过滤 1:普通 2:严格
    @NotNull
    private Integer contentFilterLevel;

    //头像路径
    @Size(max = 320)
    private String avatarUrl;

}
