package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.Date;

@Data
public class RecoverRpChatHistoryVo {

    //对话历史ID
    private Long id;

    // 角色名称
    private String name;

    // 头像路径
    private String avatarPath;

    //消息类型：0-用户消息，1-AI消息
    private Integer type;

    //消息内容-原始消息(展示给用户看的消息,不含系统Prompt等)
    private String rawContent;
    
    //消息创建时间
    private Date createTime;
}
