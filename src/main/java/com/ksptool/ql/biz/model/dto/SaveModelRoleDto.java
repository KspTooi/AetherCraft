package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 保存模型角色的数据传输对象
 */
@Data
public class SaveModelRoleDto {
    
    /**
     * 角色ID，新建时为null
     */
    private Long id;
    
    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称最多50个字符")
    private String name;
    
    /**
     * 角色描述
     */
    @Size(max = 50000, message = "角色描述最多50000个字符")
    private String description;
    
    /**
     * 头像路径
     */
    @Size(max = 255, message = "头像路径最多255个字符")
    private String avatarPath;
    
    /**
     * 角色设定摘要
     */
    @Size(max = 50000, message = "角色设定摘要最多50000个字符")
    private String roleSummary;
    
    /**
     * 情景
     */
    @Size(max = 50000, message = "情景最多50000个字符")
    private String scenario;
    
    /**
     * 首次对话内容
     */
    @Size(max = 50000, message = "首次对话内容最多50000个字符")
    private String firstMessage;
    
    /**
     * 角色标签，多个标签用逗号分隔
     */
    @Size(max = 50, message = "角色标签最多50个字符")
    private String tags;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
} 