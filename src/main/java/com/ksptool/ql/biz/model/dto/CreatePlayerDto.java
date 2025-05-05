package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePlayerDto {

    //(明文)人物角色名称
    @NotBlank
    @Size(max = 24)
    private String name;

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
