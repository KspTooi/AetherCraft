import type RestPageableView from "@/entity/RestPageableView.ts";
import Http from "@/commons/Http.ts";
import type PageQuery from "@/entity/PageQuery.ts";
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts";

export interface GetSessionListDto extends PageQuery {
    userName: string | null;    // 用户名
    playerName: string | null;  // 玩家名
}

export interface GetSessionListVo {
    id: string;             // 会话ID
    username: string;       // 用户名
    playerName: string;     // 当前登录人物名
    createTime: string;     // 登入时间
    expiresAt: string;      // 过期时间
}

export interface GetSessionDetailsVo {
    id: string;             // 会话ID
    username: string;       // 用户名
    playerName: string;     // 当前登录人物名
    createTime: string;     // 登入时间
    expiresAt: string;      // 过期时间
    permissions: string[];  // 权限节点
}

export default {
    /**
     * 获取会话列表
     */
    getSessionList: async (dto: GetSessionListDto): Promise<RestPageableView<GetSessionListVo>> => {
        return await Http.postEntity<RestPageableView<GetSessionListVo>>('/admin/session/getSessionList', dto);
    },

    /**
     * 获取会话详情
     */
    getSessionDetails: async (dto: CommonIdDto): Promise<GetSessionDetailsVo> => {
        return await Http.postEntity<GetSessionDetailsVo>('/admin/session/getSessionDetails', dto);
    },

    /**
     * 关闭会话
     */
    closeSession: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/session/closeSession', dto);
    }
}
