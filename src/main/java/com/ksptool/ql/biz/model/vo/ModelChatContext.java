package com.ksptool.ql.biz.model.vo;

import lombok.Data;

/**
 * 聊天上下文VO
 * 用于存储聊天的上下文信息
 */
@Data
public class ModelChatContext {

    //上下文ID，每一次聊天唯一
    private String contextId;

    //模型代码
    private String modelCode;

    //上下文类型 0:数据 1:结束 2:错误
    private Integer type;

    //上下文内容(模型回复内容) 如果type=1时返回模型的完整回复内容 如果type=0时返回模型当前回复的片段
    private String content;
    
    //顺序，用于排序
    private Integer sequence;

    //异常信息，当type=2时不为null
    private Exception exception;

    //当type=1时产生TOKEN计数

    //Token计数 - 用户输入
    private Integer tokenInput;

    //Token计数 - 输出
    private Integer tokenOutput;

    //Token计数 - 输出(思考)
    private Integer tokenThoughtsOutput;
    
} 