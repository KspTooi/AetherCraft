package com.ksptool.ql.biz.model.vo;

import lombok.Data;

// 聊天消息片段VO
@Data
public class RpSegmentVo {
    
    // 会话ID
    private Long threadId;
    
    // 历史记录ID
    private Long historyId;
    
    // 角色 0-用户 1-AI助手
    private Integer role;
    
    // 片段序号
    private Integer sequence;
    
    // 片段内容
    private String content;

    // 片段类型 0:开始 1:数据 2:结束 10:错误 null:无数据
    private Integer type;
    
    // 角色ID
    private Long roleId;
    
    // 角色名称
    private String roleName;
    
    // 角色头像路径
    private String roleAvatarPath;
} 