package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;

@Data
public class GetPermissionListDto extends PageQuery {
    /**
     * 权限代码
     */
    private String code;
    
    /**
     * 权限名称
     */
    private String name;

}
