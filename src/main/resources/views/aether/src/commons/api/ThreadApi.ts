import Http from "@/commons/Http.ts";
import type PageQuery from "@/entity/PageQuery.ts"; // 假设已存在
import type RestPageableView from "@/entity/RestPageableView.ts"; // 假设已存在
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts"; // 假设已存在

// --- DTOs ---
export interface CreateThreadDto {
    modelVariantId: string; // 模型变体ID
    type: number;           // Thread类型 0:标准会话 1:RP会话 2:标准增强会话
    npcId: string;          // NpcId
}

export interface SelectThreadDto extends PageQuery {
    npcId?: string;         // NPC_ID 用于获取该NPC下最近的一次会话
    threadId?: string;      // ThreadId 直接获取该Thread下的所有会话
    modelVariantId?: string; // 模型变体ID
}

export interface GetThreadListDto extends PageQuery {
    type: number;           // Thread类型 0:标准会话 1:RP会话 2:标准增强会话
    npcId?: string;         // NpcID 当type为1时必填
    title?: string;         // 标题 模糊查询
}

export interface EditThreadTitleDto {
    threadId: string;       // 会话ID
    title: string;          // 新标题
}

// --- VOs ---
export interface CreateThreadVo {
    threadId: string;           // 会话ID
}

export interface SelectThreadMessageVo {
    id: string;                 // 消息ID
    senderName: string;         // 发送人名称
    senderAvatarUrl: string;    // 发送人头像
    senderRole: number;         // 发送人角色 0:Player 1:Model
    content: string;            // 消息内容
    contentThoughts: string;    // 消息思考内容
    createTime: string;         // 消息发送时间 yyyy年mm月dd日 HH:mm:ss
}

export interface SelectThreadVo {
    threadId: string;           // 会话ID
    modelVariantId: string;     // 模型变体ID
    messages: RestPageableView<SelectThreadMessageVo>; // 消息列表
}

export interface GetThreadListVo {
    id: string;                 // ThreadId
    title: string;              // (明文)会话标题
    lastMessage: string;        // 最后一条消息预览
    publicInfo: string;         // (明文)会话公开信息
    modelVariantId: string;     // 模型变体ID
    modelVariantName: string;   // 模型变体名称
    active: number;             // 是否为当前激活的对话 0:缓解 1:激活
    createTime: string;         // 创建时间
    updateTime: string;         // 更新时间
    messageCount: number;       // 消息数量
}


export default {
    /**
     * 创建新的空NPC对话
     */
    createThread: async (dto: CreateThreadDto): Promise<CreateThreadVo> => {
        return await Http.postEntity<CreateThreadVo>('/thread/createThread', dto);
    },

    /**
     * 选择会话 (获取会话消息列表)
     */
    selectThread: async (dto: SelectThreadDto): Promise<SelectThreadVo> => {
        return await Http.postEntity<SelectThreadVo>('/thread/selectThread', dto);
    },

    /**
     * 获取会话列表
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
