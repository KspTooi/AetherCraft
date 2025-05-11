package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;

@Data
public class GetSessionListDto extends PageQuery {

    //用户名
    private String userName;

    //玩家名
    private String playerName;

}
