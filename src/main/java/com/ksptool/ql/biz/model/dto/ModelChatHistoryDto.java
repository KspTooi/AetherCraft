package com.ksptool.ql.biz.model.dto;

import lombok.Data;

/**
 * 模型聊天历史记录DTO
 */
@Data
public class ModelChatHistoryDto {
    
    /**
     * 角色 0-用户 1-AI助手
     */
    private Integer role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息序号
     */
    private Integer sequence;
} 