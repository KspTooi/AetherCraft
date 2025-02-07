package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TerminateProcessDto {
    @NotNull(message = "进程ID不能为空")
    private Long pid;  // 要终止的进程ID
} 