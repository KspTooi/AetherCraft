export default interface SaveGroupDto {
    // 组ID
    id?: string;

    // 组标识
    code: string;

    // 组名称
    name: string;

    // 组描述
    description?: string;

    // 组状态：0-禁用，1-启用
    status: number;

    // 排序号
    sortOrder: number;

    // 权限ID列表
    permissionIds?: number[];
} 