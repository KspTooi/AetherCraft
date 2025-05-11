package com.ksptool.ql.biz.model.dto;

import lombok.Data;

@Data
public class SaveNpcChatExampleItemDto {


    //NPC对话示例ID
    private Long id;

    //(加密)对话内容
    private String content;

    //排序号
    private Integer sortOrder;

}
