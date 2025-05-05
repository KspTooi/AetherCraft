package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;

@Data
public class GetPlayerListDto extends PageQuery {

    //关键字查询
    private String keyword;

}
