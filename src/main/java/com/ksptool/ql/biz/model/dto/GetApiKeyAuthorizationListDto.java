package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetApiKeyAuthorizationListDto extends PageQuery {

    // API密钥ID
    @NotNull
    private Long apiKeyId;

    // 被授权人用户名
    private String authorizedUserName;

}
