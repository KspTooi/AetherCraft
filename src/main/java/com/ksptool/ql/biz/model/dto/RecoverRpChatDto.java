package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecoverRpChatDto {

    //模型角色ID
    @NotNull(message = "模型角色ID不能为空")
    private Long modelRoleId;

    //AI模型代码
    @NotBlank(message = "AI模型代码不能为空")
    private String modelCode;
    
    //是否创建新会话 0:创建新会话Thread 1:总是尝试获取旧Thread
    private Integer newThread = 1;
    
    //指定要加载的会话ID，如果提供则直接加载该会话，无视newThread参数
    private Long threadId;
}
