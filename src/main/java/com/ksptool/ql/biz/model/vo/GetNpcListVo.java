package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetNpcListVo {

    public GetNpcListVo(Long id, String name, String avatarUrl, Integer threadCount, Integer active) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.threadCount = threadCount;
        this.active = active;
    }

    // NPC ID
    private Long id;
    
    // 角色名称
    private String name;
    
    // 头像路径
    private String avatarUrl;

    // 对话数量
    private Integer threadCount;

    // 是否当前活动 0:否 1:是
    private Integer active;
}