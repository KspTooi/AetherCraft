package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SelectThreadDto extends PageQuery {

    //NPC_ID 用于获取该NPC下最近的一次会话
    private Long npcId;

    //ThreadId 直接获取该Thread下的所有会话
    private Long threadId;

}
