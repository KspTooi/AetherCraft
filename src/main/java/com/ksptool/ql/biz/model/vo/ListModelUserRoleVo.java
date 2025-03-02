package com.ksptool.ql.biz.model.vo;

import com.ksptool.ql.biz.model.dto.ModelUserRoleQueryDto;
import lombok.Data;

import java.util.List;

/**
 * 用户角色列表视图对象
 * 用于响应角色列表页面数据
 */
@Data
public class ListModelUserRoleVo {
    
    // 角色列表
    private List<ModelUserRoleVo> roles;
    
    // 是否为新建角色模式
    private Boolean isNewRole = false;
    
    // 当前选中的角色
    private ModelUserRoleVo selectedRole;
    
    // 查询参数
    private ModelUserRoleQueryDto query;
} 