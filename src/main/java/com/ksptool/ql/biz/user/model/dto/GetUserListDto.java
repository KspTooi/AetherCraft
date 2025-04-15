package com.ksptool.ql.biz.user.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetUserListDto extends PageQuery {
    /**
     * 用户名查询
     */
    private String username;
}
