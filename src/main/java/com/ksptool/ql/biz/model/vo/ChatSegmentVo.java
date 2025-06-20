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
     * 历史记录ID
     */
    private Long historyId;
    
    /**
     * 角色 0:用户 1:模型
     */
    private Integer role;
    
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

    private String name;

    private String AvatarPath;
} 