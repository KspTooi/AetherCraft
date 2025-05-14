package com.ksptool.ql.biz.model.dto;

import lombok.Data;

/**
 * 模型聊天参数 - 历史记录
 */
@Data
public class ModelChatParamHistory {

    //角色 0:用户 1:模型
    private Integer role;

    //消息内容
    private String content;

    //消息序号
    private Integer sequence;
} 