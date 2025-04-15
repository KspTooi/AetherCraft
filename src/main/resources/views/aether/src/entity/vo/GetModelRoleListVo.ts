/**
 * 获取模型角色列表项VO
 */
export default interface GetModelRoleListVo {
    /**
     * 角色ID
     */
    id: string;
    
    /**
     * 角色名称
     */
    name: string;
    
    /**
     * 头像路径
     */
    avatarPath?: string;
} 