package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommonIdDto {
    @NotNull(message = "ID不可为空")
    private Long id;
}
