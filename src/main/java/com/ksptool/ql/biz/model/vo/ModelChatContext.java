package com.ksptool.ql.biz.model.vo;

import lombok.Data;

/**
 * 聊天上下文VO
 * 用于存储聊天的上下文信息
 */
@Data
public class ModelChatContext {
    
    /**
     * 上下文ID，每一次聊天唯一
     */
    private String contextId;
    
    /**
     * 上下文类型
     * 0 - 数据
     * 1 - 结束
     * 2 - 错误
     */
    private Integer type;
    
    /**
     * 上下文内容
     */
    private String content;
    
    /**
     * 顺序，用于排序
     */
    private Integer sequence;
    
    /**
     * 异常信息，当type=2时不为null
     */
    private Exception exception;
    
} 