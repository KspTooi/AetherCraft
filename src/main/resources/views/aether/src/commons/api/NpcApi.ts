import type RestPageableView from "@/entity/RestPageableView.ts";
import Http from "@/commons/Http.ts";
import type PageQuery from "@/entity/PageQuery.ts";
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts";

export interface GetNpcListDto extends PageQuery {
    keyword?: string | null;    // 关键字搜索(角色名称或描述)
}

export interface GetNpcListVo {
    id: string;             // NPC ID
    name: string;           // 角色名称
    avatarPath: string;     // 头像路径
    threadCount: number;    // 对话数量
    active: number;         // 是否当前活动 0:否 1:是
}

export interface GetNpcDetailsVo {
    id: string;             // 角色ID
    name: string;           // 角色名称
    avatarPath: string;     // 头像路径
    description: string;    // 角色描述
    roleSummary: string;    // 角色设定摘要
    scenario: string;       // 情景
    firstMessage: string;   // 首次对话内容
    tags: string;           // 角色标签，多个标签用逗号分隔
    sortOrder: number;      // 排序号
    status: number;         // 状态：0-禁用，1-启用
}

export interface SaveNpcDto {
    id?: string;            // 角色ID，新建时为null
    name: string;           // 角色名称
    description?: string;   // 角色描述
    avatarPath?: string;    // 头像路径
    roleSummary?: string;   // 角色设定摘要
    scenario?: string;      // 情景
    firstMessage?: string;  // 首次对话内容
    tags?: string;          // 角色标签，多个标签用逗号分隔
    sortOrder?: number;     // 排序号
    status: number;         // 状态：0-禁用，1-启用
}

export default {
    /**
     * 获取NPC列表
     */
    getNpcListVo: async (dto: GetNpcListDto): Promise<RestPageableView<GetNpcListVo>> => {
        return await Http.postEntity<RestPageableView<GetNpcListVo>>('/npc/getNpcListVo', dto);
    },

    /**
     * 获取NPC详情
     */
    getNpcDetails: async (dto: CommonIdDto): Promise<GetNpcDetailsVo> => {
        return await Http.postEntity<GetNpcDetailsVo>('/npc/getNpcDetails', dto);
    },

    /**
     * 复制NPC
     */
    copyNpc: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/npc/copyNpc', dto);
    },

    /**
     * 保存NPC
     */
    saveNpc: async (dto: SaveNpcDto): Promise<string> => {
        return await Http.postEntity<string>('/npc/saveNpc', dto);
    },

    /**
     * 删除NPC
     */
    removeNpc: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/npc/removeNpc', dto);
    },

    /**
     * 上传头像
     */
    uploadAvatar: async (file: File): Promise<string> => {
        const formData = new FormData();
        formData.append('file', file);
        return await Http.postEntity<string>('/npc/uploadAvatar', formData);
    }
}
