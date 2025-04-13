/**
 * 保存模型角色的数据传输对象
 */
export default interface SaveModelRoleDto {
    /**
     * 角色ID，新建时为null
     */
    id?: string;
    
    /**
     * 角色名称
     */
    name: string;
    
    /**
     * 角色描述
     */
    description?: string;
    
    /**
     * 头像路径
     */
    avatarPath?: string;
    
    /**
     * 角色设定摘要
     */
    roleSummary?: string;
    
    /**
     * 情景
     */
    scenario?: string;
    
    /**
     * 首次对话内容
     */
    firstMessage?: string;
    
    /**
     * 角色标签，多个标签用逗号分隔
     */
    tags?: string;
    
    /**
     * 排序号
     */
    sortOrder?: number;
    
    /**
     * 状态：0-禁用，1-启用
     */
    status: number;
} 