package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.util.List;

@Data
public class AdminToggleModelVariantDto {

    @NotEmpty(message = "模型变体ID列表不能为空")
    private List<Long> ids;

    //启用状态 0:禁用 1:启用
    @NotNull(message = "启用状态不能为空") 
    @Min(value = 0, message = "启用状态值无效")
    @Max(value = 1, message = "启用状态值无效")
    private Integer enabled;

}
