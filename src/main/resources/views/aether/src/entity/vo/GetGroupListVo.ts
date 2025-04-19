// 组列表视图对象
export default interface GetGroupListVo {
    // 组ID
    id: string;

    // 组标识
    code: string;

    // 组名称
    name: string;

    // 成员数量
    memberCount: number;

    // 权限节点数量
    permissionCount: number;

    // 系统内置组
    isSystem: boolean;

    // 组状态：0-禁用，1-启用
    status: number;

    // 创建时间
    createTime: string;
} 