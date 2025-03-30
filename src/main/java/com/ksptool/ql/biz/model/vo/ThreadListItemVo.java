package com.ksptool.ql.biz.model.vo;

import lombok.Data;

/**
 * 会话列表项VO
 */
@Data
public class ThreadListItemVo {
    
    /**
     * 会话ID
     */
    private Long id;
    
    /**
     * 会话标题
     */
    private String title;
    
    /**
     * 模型代码
     */
    private String modelCode;

    /**
     * 是否默认选中 0:否 1:是
     */
    private Integer checked = 0;
} 