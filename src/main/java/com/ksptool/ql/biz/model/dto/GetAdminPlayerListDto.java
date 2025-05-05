package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetAdminPlayerListDto extends PageQuery {

    //人物名
    private String playerName;

    //用户名
    private String username;

    //人物状态 0:正在使用 1:不活跃 2:等待删除 3:已删除
    private Integer status;

}
