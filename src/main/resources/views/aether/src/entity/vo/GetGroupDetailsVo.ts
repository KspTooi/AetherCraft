import type GroupPermissionDefinitionVo from '@/entity/vo/GroupPermissionDefinitionVo';

// 组详情视图对象
export default interface GetGroupDetailsVo {
    // 组ID
    id: string;

    // 组标识
    code: string;

    // 组名称
    name: string;

    // 组描述
    description: string;

    // 是否系统内置组
    isSystem: boolean;

    // 组状态：0-禁用，1-启用
    status: number;

    // 排序号
    sortOrder: number;

    // 权限节点列表
    permissions: GroupPermissionDefinitionVo[];
} 