package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class RecoverChatHistoryVo {

    //对话历史ID
    private Long id;

    // 角色名称
    private String name;

    // 头像路径
    private String avatarPath;

    //消息类型：0-用户消息，1-AI消息
    private Integer role;

    //消息内容
    private String content;
    
    //消息创建时间
    private Date createTime;
}
