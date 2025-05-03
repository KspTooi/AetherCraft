package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;

@Data
public class GetConfigListDto extends PageQuery {

    // 配置键或值
    private String keyOrValue;
    
    // 描述
    private String description;

} 