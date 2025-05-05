package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RemovePlayerDefaultGroupDto {

    @NotNull
    private List<Long> ids;

}
