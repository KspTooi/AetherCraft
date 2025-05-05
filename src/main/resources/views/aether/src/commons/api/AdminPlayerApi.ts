import type RestPageableView from '@/entity/RestPageableView';
import Http from '@/commons/Http';
import type PageQuery from '@/entity/PageQuery';
import type CommonIdDto from '@/entity/dto/CommonIdDto'; // 假设 CommonIdDto 在此路径

// --- DTOs ---

export interface GetAdminPlayerListDto extends PageQuery {
    playerName: string | null; // 人物名
    username: string | null;   // 用户名
    status: number | null | undefined; // 人物状态 (允许 undefined 以适配 el-select clearable)
}

export interface EditAdminPlayerDto {
    id: string;                 // 人物ID (NotNull)
    avatarUrl?: string;        // 头像路径
    publicInfo?: string;       // 个人信息
    language?: string;          // 语言
    era?: string;              // 年代
    contentFilterLevel?: number; // 内容过滤等级
    status?: number;           // 状态: 1:不活跃 3:已删除 (后台仅允许设置这两个状态)
    groupIds?: string[];       // 访问组IDS (Long[] -> string[])
}


// --- VOs ---

export interface GetAdminPlayerListVo {
    id: string;         // 主键ID
    name: string;       // 人物名称
    username: string;   // 所有者
    balance: string;    // 余额 (BigDecimal -> string)
    status: number;    // 状态: 0:正在使用 1:不活跃 2:等待删除 3:已删除
    createTime: string; // 诞生日期 (Date -> string)
    groupCount: number; // 访问组数量
}

export interface GetAdminPlayerDetailsVo {
    id: string;                 // 人物ID
    avatarUrl: string;        // 头像路径
    name: string;               // 人物名称
    username: string;           // 所有者
    publicInfo: string;       // 个人信息
    balance: string;            // 余额 (BigDecimal -> string)
    language: string;          // 语言
    era: string;              // 年代
    contentFilterLevel: number; // 内容过滤等级
    status: number;            // 状态: 0:正在使用 1:不活跃 2:等待删除 3:已删除
    removalRequestTime: string; // 移除申请提交时间 (Date -> string)
    removedTime: string;      // 角色移除时间 (Date -> string)
    lastActiveTime: string;   // 最后激活时间 (Date -> string)
    createTime: string;         // 诞生日期 (Date -> string)
    groupIds: string[];       // 拥有的访问组ID (Long[] -> string[])
}


export default {

    /**
     * 获取后台玩家列表
     */
    getPlayerList: async (dto: GetAdminPlayerListDto): Promise<RestPageableView<GetAdminPlayerListVo>> => {
        return await Http.postEntity<RestPageableView<GetAdminPlayerListVo>>('/admin/player/getPlayerList', dto);
    },

    /**
     * 获取后台玩家详情
     */
    getPlayerDetails: async (dto: CommonIdDto): Promise<GetAdminPlayerDetailsVo> => {
        // 后端返回 Result<GetAdminPlayerDetailsVo>, Http.postEntity 处理解包
        return await Http.postEntity<GetAdminPlayerDetailsVo>('/admin/player/getPlayerDetails', dto);
    },

    /**
     * 编辑后台玩家信息
     */
    editPlayer: async (dto: EditAdminPlayerDto): Promise<string> => {
        // 后端返回 Result<String>, Http.postEntity 处理解包
        return await Http.postEntity<string>('/admin/player/editPlayer', dto);
    },

}
