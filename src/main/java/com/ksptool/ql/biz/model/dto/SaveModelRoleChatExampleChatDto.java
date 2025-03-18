package com.ksptool.ql.biz.model.dto;

import lombok.Data;

@Data
public class SaveModelRoleChatExampleChatDto {

    //ID 新增有、编辑无
    private Long id;

    //对话内容
    private String content;

    //排序号
    private Integer sortOrder;

    //前端移除标志 0:移除(需传ID) 其他值:不移除
    private Integer remove;

}
