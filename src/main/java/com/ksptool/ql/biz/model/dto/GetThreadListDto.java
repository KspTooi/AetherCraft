package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetThreadListDto extends PageQuery {

    //Thread类型 0:标准会话 1:RP会话 2:标准增强会话
    @NotNull
    @Range(min = 0, max = 2)
    private Integer type;

    //NpcID 当type为1时必填
    private Long npcId;

}
