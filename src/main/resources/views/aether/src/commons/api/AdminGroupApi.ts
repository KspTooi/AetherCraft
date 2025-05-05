import type RestPageableView from "@/entity/RestPageableView.ts";
import Http from "@/commons/Http.ts";
import type PageQuery from "@/entity/PageQuery.ts";
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts";

export interface GroupPermissionDefinitionVo {
    id: string;           // 权限节点ID
    code: string;         // 权限节点标识
    name: string;         // 权限节点名称
    has: number;          // 当前组是否拥有 0:拥有 1:不拥有
}

export interface GetGroupDefinitionsVo {
    id: string;           // 组ID
    name: string;         // 组名称
}

export interface GetGroupListDto extends PageQuery {
    keyword?: string;     // 模糊匹配 组标识、组名称、组描述
    status?: number;      // 组状态：0:禁用 1:启用
}

export interface GetGroupListVo {
    id: string;           // 组ID
    code: string;         // 组标识
    name: string;         // 组名称
    memberCount: number;  // 成员数量
    permissionCount: number; // 权限节点数量
    isSystem: boolean;    // 系统内置组
    status: number;       // 组状态：0-禁用，1-启用
    createTime: string;   // 创建时间
}

export interface GetGroupDetailsVo {
    id: string;           // 组ID
    code: string;         // 组标识
    name: string;         // 组名称
    description: string;  // 组描述
    isSystem: boolean;    // 是否系统内置组
    status: number;       // 组状态：0-禁用，1-启用
    sortOrder: number;    // 排序号
    permissions: GroupPermissionDefinitionVo[]; // 权限节点列表
}

export interface SaveGroupDto {
    id?: string;          // 组ID
    code: string;         // 组标识
    name: string;         // 组名称
    description?: string; // 组描述
    status: number;       // 组状态：0-禁用，1-启用
    sortOrder: number;    // 排序号
    permissionIds?: number[]; // 权限ID列表
}

export default {
    /**
     * 获取组定义列表
     */
    getGroupDefinitions: async (): Promise<GetGroupDefinitionsVo[]> => {
        return await Http.postEntity<GetGroupDefinitionsVo[]>('/admin/group/getGroupDefinitions', {});
    },

    /**
     * 获取组列表
     */
    getGroupList: async (dto: GetGroupListDto): Promise<RestPageableView<GetGroupListVo>> => {
        return await Http.postEntity<RestPageableView<GetGroupListVo>>('/admin/group/getGroupList', dto);
    },

    /**
     * 获取组详情
     */
    getGroupDetails: async (dto: CommonIdDto): Promise<GetGroupDetailsVo> => {
        return await Http.postEntity<GetGroupDetailsVo>('/admin/group/getGroupDetails', dto);
    },

    /**
     * 保存组
     */
    saveGroup: async (dto: SaveGroupDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/group/saveGroup', dto);
    },

    /**
     * 删除组
     */
    removeGroup: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/group/removeGroup', dto);
    }
}