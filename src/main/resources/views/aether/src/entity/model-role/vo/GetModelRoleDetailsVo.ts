/**
 * 获取模型角色详情VO
 */
export default interface GetModelRoleDetailsVo {
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

    /**
     * 角色描述
     */
    description?: string;

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