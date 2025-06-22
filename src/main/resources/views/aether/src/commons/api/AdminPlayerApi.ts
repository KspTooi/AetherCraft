import type RestPageableView from '@/entity/RestPageableView';
import Http from '@/commons/Http';
import type PageQuery from '@/entity/PageQuery';
import type CommonIdDto from '@/entity/dto/CommonIdDto';

export interface GetAdminPlayerListDto extends PageQuery {
    playerName: string | null; // 人物名
    username: string | null;   // 用户名
    status: number | null | undefined; // 人物状态
}

export interface EditAdminPlayerDto {
    id: string;                 // 人物ID
    avatarUrl?: string;        // 头像路径
    gender?: number;           // 性别 0:男 1:女 2:不愿透露 (管理台只能修改为0 1 2)
    publicInfo?: string;       // 个人信息
    language?: string;          // 语言
    era?: string;              // 年代
    contentFilterLevel?: number; // 内容过滤等级
    status?: number;           // 状态: 1:不活跃 3:已删除 (后台仅允许设置这两个状态)
    groupIds?: string[];       // 访问组IDS
}


export interface GetAdminPlayerListVo {
    id: string;         // 主键ID
    name: string;       // 人物名称
    username: string;   // 所有者
    balance: string;    // 余额
    status: number;    // 状态: 0:正在使用 1:不活跃 2:等待删除 3:已删除
    createTime: string; // 诞生日期
    groupCount: number; // 访问组数量
}

export interface GetAdminPlayerDetailsVo {
    id: string;                 // 人物ID
    avatarUrl?: string;        // 头像路径
    name: string;               // 人物名称
    gender?: number;           // 性别 0:男 1:女 2:不愿透露 4:自定义(男性) 5:自定义(女性) 6:自定义(其他)
    username: string;           // 所有者
    publicInfo?: string;       // 个人信息
    balance: string;            // 余额
    language?: string;          // 语言
    era?: string;              // 年代
    contentFilterLevel?: number; // 内容过滤等级
    status: number;            // 状态: 0:正在使用 1:不活跃 2:等待删除 3:已删除
    removalRequestTime?: string; // 移除申请提交时间
    removedTime?: string;      // 角色移除时间
    lastActiveTime?: string;   // 最后激活时间
    createTime: string;         // 诞生日期
    groupIds: string[];       // 拥有的访问组ID
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
        return await Http.postEntity<GetAdminPlayerDetailsVo>('/admin/player/getPlayerDetails', dto);
    },

    /**
     * 编辑后台玩家信息
     */
    editPlayer: async (dto: EditAdminPlayerDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/player/editPlayer', dto);
    },

}
