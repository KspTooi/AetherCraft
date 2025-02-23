package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.Date;

/**
 * 聊天会话数据
 */
@Data
public class ModelChatViewThreadVo {
    
    /**
     * 会话ID
     */
    private Long id;
    
    /**
     * 会话标题
     */
    private String title;
    
    /**
     * AI模型代码
     */
    private String modelCode;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 