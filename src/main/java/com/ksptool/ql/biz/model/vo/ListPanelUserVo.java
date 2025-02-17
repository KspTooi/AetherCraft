package com.ksptool.ql.biz.model.vo;

import lombok.Data;

/**
 * 用户列表项
 */
@Data
public class ListPanelUserVo {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 创建时间
     */
    private String createTime;
} 