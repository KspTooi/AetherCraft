import type RestPageableView from '@/entity/RestPageableView';
import type PageQuery from '@/entity/PageQuery';
import Http from '@/commons/Http';

// --- VO Definitions ---

export interface GetPlayerDefaultGroupListVo {
    id: string;             // 记录ID
    code: string;           // 组标识
    name: string;           // 组名称
    memberCount: number;    // 成员数量 (从 SIZE(g.users) 计算)
    permissionCount: number;// 权限节点数量 (从 SIZE(g.permissions) 计算)
    isSystem: boolean;      // 是否系统内置组
    status: number;         // 组状态：0-禁用，1-启用
    createTime: string;     // 记录创建时间
}

// --- DTO Definitions ---

// getPlayerDefaultGroupList 使用通用的 PageQuery DTO，此处无需重复定义

export interface RemovePlayerDefaultGroupDto {
    ids: string[];          // 要移除的 PlayerDefaultGroup 记录 ID 列表
}

export interface AddPlayerDefaultGroupDto {
    ids: string[];          // 要添加的 Group ID 列表
}


// --- API Functions ---

export default {
    /**
     * 获取玩家默认分组列表（分页）
     */
    getPlayerDefaultGroupList: async (dto: PageQuery): Promise<RestPageableView<GetPlayerDefaultGroupListVo>> => {
        return await Http.postEntity<RestPageableView<GetPlayerDefaultGroupListVo>>('/admin/player/default/group/getPlayerDefaultGroupList', dto);
    },

    /**
     * 移除玩家默认分组
     */
    removePlayerDefaultGroup: async (dto: RemovePlayerDefaultGroupDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/player/default/group/removePlayerDefaultGroup', dto);
    },

    /**
     * 添加玩家默认分组
     */
    addPlayerDefaultGroup: async (dto: AddPlayerDefaultGroupDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/player/default/group/addPlayerDefaultGroup', dto);
    }
}
