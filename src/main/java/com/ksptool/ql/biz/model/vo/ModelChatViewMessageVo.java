package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.Date;

/**
 * 聊天消息数据
 */
@Data
public class ModelChatViewMessageVo {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 角色 0-用户 1-AI助手
     */
    private Integer role;
    
    /**
     * 消息序号
     */
    private Integer sequence;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 