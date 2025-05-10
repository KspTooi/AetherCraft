package com.ksptool.ql.biz.model.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.List;

@Data
public class EditAdminPlayerDto {

    //人物ID
    @NotNull
    private Long id;

    //头像路径
    private String avatarUrl;

    //性别 0:男 1:女 2:不愿透露 4:自定义(男性) 5:自定义(女性) 6:自定义(其他) 管理台只能修改为0 1 2
    private Integer gender;

    //个人信息
    private String publicInfo;

    //语言
    private String language;

    //年代
    private String era;

    //内容过滤等级
    private Integer contentFilterLevel;

    //状态: 1:不活跃 3:已删除 (后台仅允许设置这两个状态)
    private Integer status;

    //访问组IDS
    private List<Long> groupIds;

}
