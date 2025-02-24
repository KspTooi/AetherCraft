package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.List;

/**
 * 聊天视图数据
 */
@Data
public class ModelChatViewVo {
    
    /**
     * 会话列表
     */
    private List<ModelChatViewThreadVo> threads;
    
    /**
     * 会话总数
     */
    private long threadCount;
    
    /**
     * 当前会话的消息列表
     */
    private List<ModelChatViewMessageVo> messages;
    
    /**
     * 当前会话ID
     */
    private Long currentThreadId;
    
    /**
     * 当前会话信息
     */
    private ModelChatViewThreadVo currentThread;
    
    /**
     * 当前选择的模型代码
     */
    private String selectedModel;
    
    /**
     * 可用的模型列表
     */
    private List<String> models;
} 