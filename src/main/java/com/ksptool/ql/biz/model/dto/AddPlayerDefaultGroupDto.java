package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AddPlayerDefaultGroupDto {

    @NotNull
    private List<Long> ids;
    
}
