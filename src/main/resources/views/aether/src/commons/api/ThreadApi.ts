import Http from "@/commons/Http.ts";
import type PageQuery from "@/entity/PageQuery.ts"; // 假设已存在
import type RestPageableView from "@/entity/RestPageableView.ts"; // 假设已存在
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts"; // 假设已存在

// --- DTOs ---
export interface CreateThreadDto {
    modelCode: string;      // 模型代码, JavaType: String
    type: number;           // Thread类型 0:标准会话 1:RP会话 2:标准增强会话, JavaType: Integer
    npcId: string;          // NpcId, JavaType: Long
}

export interface SelectThreadDto extends PageQuery {
    npcId?: string;         // NPC_ID 用于获取该NPC下最近的一次会话, JavaType: Long
    threadId?: string;      // ThreadId 直接获取该Thread下的所有会话, JavaType: Long
    modelCode?: string;      // 模型代码
}

export interface GetThreadListDto extends PageQuery {
    type: number;           // Thread类型 0:标准会话 1:RP会话 2:标准增强会话, JavaType: Integer
    npcId?: string;         // NpcID 当type为1时必填, JavaType: Long
}

export interface EditThreadTitleDto {
    threadId: string;       // 会话ID, JavaType: Long
    title: string;          // 新标题, JavaType: String
}

// --- VOs ---
export interface CreateThreadVo {
    threadId: string;           // JavaType: Long
}

export interface SelectThreadMessageVo {
    id: string;                 // JavaType: Long
    senderName: string;         // 发送人名称, JavaType: String
    senderAvatarUrl: string;    // 发送人头像, JavaType: String
    senderRole: number;         // 发送人角色 0:Player 1:Model, JavaType: Integer
    content: string;            // 消息内容, JavaType: String
    createTime: string;         // 消息发送时间 yyyy年mm月dd日 HH:mm:ss, JavaType: String
}

export interface SelectThreadVo {
    threadId: string;           // JavaType: Long
    modelCode: string;          // JavaType: String
    messages: RestPageableView<SelectThreadMessageVo>; // JavaType: RestPageableView<SelectThreadMessageVo>
}

export interface GetThreadListVo {
    id: string;                 // ThreadId, JavaType: Long
    title: string;              // (明文)会话标题, JavaType: String
    lastMessage: string;        // 最后一条消息预览, JavaType: String
    publicInfo: string;         // (明文)会话公开信息, JavaType: String
    modelCode: string;          // 模型代码, JavaType: String
    active: number;             // 是否为当前激活的对话 0:缓解 1:激活, JavaType: Integer
    createTime: string;         // 创建时间, JavaType: Date (TS string)
    updateTime: string;         // 更新时间, JavaType: Date (TS string)
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
