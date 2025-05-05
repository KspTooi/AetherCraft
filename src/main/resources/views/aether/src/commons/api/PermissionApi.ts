import type RestPageableView from "@/entity/RestPageableView";
import Http from "@/commons/Http";
import type CommonIdDto from "@/entity/dto/CommonIdDto";
import type PageQuery from "@/entity/PageQuery.ts";


export interface GetPermissionDefinitionVo {
    id: string;    // 权限节点ID
    code: string;  // 权限节点标识
    name: string;  // 权限节点名称
}

export interface GetPermissionListDto extends PageQuery {
    code?: string | null;        // 权限代码
    name?: string | null;        // 权限名称
}

export interface GetPermissionListVo {
    id: string;           // 权限ID
    code: string;         // 权限代码
    name: string;         // 权限名称
    description: string;  // 权限描述
    isSystem: number;     // 是否为系统权限（1-是，0-否）
}

export interface GetPermissionDetailsVo {
    id: string;           // 权限ID
    code: string;         // 权限代码
    name: string;         // 权限名称
    description: string;  // 权限描述
    sortOrder: number;    // 排序顺序
    isSystem: number;     // 是否为系统权限（1-是，0-否）
    createTime: string;   // 创建时间
    updateTime: string;   // 修改时间
}

export interface SavePermissionDto {
    id?: string | null;           // 权限ID，创建时为空
    code: string;                 // 权限代码
    name: string;                 // 权限名称
    description?: string | null;  // 权限描述
    sortOrder?: number | null;    // 排序顺序
}

export default {
    /**
     * 获取所有权限定义
     */
    getPermissionDefinition: async (): Promise<GetPermissionDefinitionVo[]> => {
        return await Http.postEntity<GetPermissionDefinitionVo[]>('/admin/permission/getPermissionDefinition', {});
    },

    /**
     * 获取权限列表（分页）
     */
    getPermissionList: async (dto: GetPermissionListDto): Promise<RestPageableView<GetPermissionListVo>> => {
        return await Http.postEntity<RestPageableView<GetPermissionListVo>>('/admin/permission/getPermissionList', dto);
    },

    /**
     * 获取权限详情
     */
    getPermissionDetails: async (dto: CommonIdDto): Promise<GetPermissionDetailsVo> => {
        return await Http.postEntity<GetPermissionDetailsVo>('/admin/permission/getPermissionDetails', dto);
    },

    /**
     * 保存权限（新增或更新）
     */
    savePermission: async (dto: SavePermissionDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/permission/savePermission', dto);
    },

    /**
     * 删除权限
     */
    removePermission: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/permission/removePermission', dto);
    }
} 