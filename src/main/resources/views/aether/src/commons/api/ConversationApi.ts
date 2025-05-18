import type RestPageableView from "@/entity/RestPageableView.ts";
import Http from "@/commons/Http.ts";
import type PageQuery from "@/entity/PageQuery.ts";
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts";

// --- DTOs ---

/**
 * 选择会话 DTO
 */
export interface SelectThreadDto {
    threadId: string; // 会话ID
}

/**
 * 获取会话列表 DTO
 */
export interface GetThreadListDto extends PageQuery {
    type: number;    // Thread类型 0:标准会话 1:RP会话 2:标准增强会话
    npcId?: string;   // NpcId (当type为1时，此项可能需要)
    keyword?: string; // 搜索关键词 (例如：标题)
}

/**
 * 编辑会话标题 DTO
 */
export interface EditThreadTitleDto {
    threadId: string; // 会话ID
    title: string;    // 新标题
}

// --- VOs ---

/**
 * 单条消息 VO (用于 SelectThreadVo)
 */
export interface SelectThreadMessageVo {
    id: string;                 // 消息ID
    senderName: string;         // 发送人名称
    senderAvatarUrl?: string;    // 发送人头像
    senderRole: number;         // 发送人角色 0:Player 1:Model
    content: string;            // 消息内容
    createTime: string;         // 消息发送时间 yyyy年mm月dd日 HH:mm:ss
}

/**
 * 选择会话 VO
 */
export interface SelectThreadVo {
    threadId: string;         // 会话ID
    modelCode: string;      // 模型代码
    messages: RestPageableView<SelectThreadMessageVo>; // 消息列表
}

/**
 * 获取会话列表项 VO
 */
export interface GetThreadListVo {
    id: string;             // 会话ID
    title: string;          // 会话标题
    lastMessage?: string;   // 最后一条消息预览
    publicInfo?: string;    // (明文)会话公开信息
    modelCode: string;      // 模型代码
    active: number;         // 是否为当前激活的对话 0:归档/不活跃 1:激活
    createTime: string;     // 创建时间
    updateTime: string;     // 更新时间
}


export default {
    /**
     * 选择或创建并选择一个会话
     */
    selectThread: async (dto: SelectThreadDto): Promise<SelectThreadVo> => {
        return await Http.postEntity<SelectThreadVo>('/thread/selectThread', dto);
    },

    /**
     * 获取会话列表 (分页)
     */
    getThreadList: async (dto: GetThreadListDto): Promise<RestPageableView<GetThreadListVo>> => {
        return await Http.postEntity<RestPageableView<GetThreadListVo>>('/thread/getThreadList', dto);
    },

    /**
     * 编辑会话标题
     */
    editThreadTitle: async (dto: EditThreadTitleDto): Promise<string> => {
        return await Http.postEntity<string>('/thread/editThreadTitle', dto);
    },

    /**
     * 删除会话
     */
    removeThread: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/thread/removeThread', dto);
    }
}
