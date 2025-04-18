package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class RecoverChatVo {

    //会话ID
    private Long threadId;

    //AI模型代码
    private String modelCode;

    //对话串
    private List<RecoverChatHistoryVo> messages;

}
