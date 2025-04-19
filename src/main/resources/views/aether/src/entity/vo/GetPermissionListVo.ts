/**
 * 权限列表视图对象
 */
export default interface GetPermissionListVo {
    /**
     * 权限ID
     */
    id: string;
    
    /**
     * 权限代码
     */
    code: string;
    
    /**
     * 权限名称
     */
    name: string;
    
    /**
     * 权限描述
     */
    description: string;
    
    /**
     * 排序顺序
     */
    sortOrder: number;
    
    /**
     * 是否为系统权限（1-是，0-否）
     */
    isSystem: number;
    
    /**
     * 创建时间
     */
    createTime: string;
    
    /**
     * 更新时间
     */
    updateTime: string;
} 