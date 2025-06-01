package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetUserListDto extends PageQuery {
    /**
     * 用户名查询
     */
    private String username;

    //0:正常 1:封禁
    private Integer status;
}
