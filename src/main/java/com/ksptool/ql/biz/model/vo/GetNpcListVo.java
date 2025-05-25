package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetNpcListVo {

    public GetNpcListVo(Long id, String name, String avatarPath, Integer threadCount) {
        this.id = id;
        this.name = name;
        this.avatarPath = avatarPath;
        this.threadCount = threadCount;
    }

    // NPC ID
    private Long id;
    
    // 角色名称
    private String name;
    
    // 头像路径
    private String avatarPath;

    // 对话数量
    private Integer threadCount;
}