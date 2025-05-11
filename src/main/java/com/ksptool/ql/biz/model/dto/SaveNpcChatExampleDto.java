package com.ksptool.ql.biz.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SaveNpcChatExampleDto {

    //NPC ID
    @NotNull
    private Long npcId;

    @Valid
    @NotNull
    private List<SaveNpcChatExampleItemDto> examples;

}
