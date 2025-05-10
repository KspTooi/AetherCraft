package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;

@Data
public class GetConfigListDto extends PageQuery {

    // 配置键/值/描述
    private String keyword;

    // 所有者
    private String playerName;

} 