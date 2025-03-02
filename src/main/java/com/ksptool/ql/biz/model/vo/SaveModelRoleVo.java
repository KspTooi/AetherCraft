package com.ksptool.ql.biz.model.vo;

import lombok.Data;

/**
 * 保存模型角色的视图对象
 */
@Data
public class SaveModelRoleVo {
    
    /**
     * 是否保存成功
     */
    private Boolean success;
    
    /**
     * 保存后的角色ID
     */
    private Long id;
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 提示消息
     */
    private String message;
} 