package com.ksptool.ql.biz.model.vo;

import lombok.Data;

/**
 * 聊天消息片段VO
 */
@Data
public class ChatSegmentVo {
    
    /**
     * 会话ID
     */
    private Long threadId;
    
    /**
     * 片段序号
     */
    private Integer sequence;
    
    /**
     * 片段内容
     */
    private String content;
    
    /**
     * 片段类型
     * 0 - 开始
     * 1 - 数据
     * 2 - 结束
     * 10 - 错误
     * null - 无数据
     */
    private Integer type;
} 