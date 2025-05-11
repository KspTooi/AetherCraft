package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetNpcChatExampleListVo {

    //NPC对话示例ID
    private Long id;

    //(加密)对话内容
    private String content;

    //排序号
    private String sortOrder;

}
