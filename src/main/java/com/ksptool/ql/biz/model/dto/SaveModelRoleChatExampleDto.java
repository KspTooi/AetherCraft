package com.ksptool.ql.biz.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SaveModelRoleChatExampleDto {

    public Long modelRoleId;

    private List<SaveModelRoleChatExampleChatDto> chatList;

}
