package com.ksptool.ql.biz.model.vo;

import java.util.List;

/**
 * 校验系统权限结果VO
 */
public class ValidateSystemPermissionsVo {
    
    /**
     * 已存在的权限数量
     */
    private int existCount;
    
    /**
     * 新增的权限数量
     */
    private int addedCount;
    
    /**
     * 新增的权限列表
     */
    private List<String> addedPermissions;
    
    public int getExistCount() {
        return existCount;
    }
    
    public void setExistCount(int existCount) {
        this.existCount = existCount;
    }
    
    public int getAddedCount() {
        return addedCount;
    }
    
    public void setAddedCount(int addedCount) {
        this.addedCount = addedCount;
    }
    
    public List<String> getAddedPermissions() {
        return addedPermissions;
    }
    
    public void setAddedPermissions(List<String> addedPermissions) {
        this.addedPermissions = addedPermissions;
    }
} 