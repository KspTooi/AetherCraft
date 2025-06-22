import Http from "@/commons/Http.ts";

// --- DTOs ---
export interface SendMessageDto {
    threadId?: string;      // 为-1时自动创建新会话
    type: number;           // 0:标准会话 1:RP会话 2:增强会话
    modelVariantId: string; // 模型变体ID
    message: string;        // 消息内容
}

export interface QueryStreamDto {
    streamId: string;       // 响应流ID
}

export interface RegenerateDto {
    threadId: string;       // 对话ThreadId
    modelVariantId: string; // 模型变体ID
    rootMessageId: string;  // 根消息ID
}

export interface AbortConversationDto {
    threadId: string;       // 对话ThreadId
}

// --- VOs ---
export interface SendMessageVo {
    threadId: string;           // 对话ThreadId
    messageId: string;          // 用户消息ID
    streamId: string;           // 响应流ID
    content: string;            // 消息内容
    senderName: string;         // 发送人姓名
    senderAvatarUrl: string;    // 发送人头像URL
    sendTime: string;           // 发送时间
    title: string;              // 对话Thread 标题
    newThreadCreated: number;   // 是否创建了新Thread 0:否 1:是
}

export interface MessageFragmentVo {
    type: number;               // 片段类型(旧) 0:起始 1:数据 2:结束 10:错误 (新) 0:起始 1:结束 2:错误 50:思考片段 51:文本
    threadId: string;           // 对话串ID
    messageId: string;          // 消息ID (-1为临时消息)
    content: string;            // 消息内容
    seq: number;                // 顺序
    senderRole: number;         // 发送人角色 0:玩家 1:模型
    senderName: string;         // 发送人姓名
    senderAvatarUrl: string;    // 发送人头像URL
    sendTime: string;           // 发送时间 yyyy年MM月dd日 HH:mm:ss
}


export default {
    /**
     * 发送消息
     */
    sendMessage: async (dto: SendMessageDto): Promise<SendMessageVo> => {
        return await Http.postEntity<SendMessageVo>('/conversation/sendMessage', dto);
    },

    /**
     * 查询流式消息片段
     */
    queryStream: async (dto: QueryStreamDto): Promise<MessageFragmentVo> => {
        return await Http.postEntity<MessageFragmentVo>('/conversation/queryStream', dto);
    },

    /**
     * 重新生成消息
     */
    regenerate: async (dto: RegenerateDto): Promise<SendMessageVo> => {
        return await Http.postEntity<SendMessageVo>('/conversation/regenerate', dto);
    },

    /**
     * 中止会话
     */
    abortConversation: async (dto: AbortConversationDto): Promise<string> => {
        return await Http.postEntity<string>('/conversation/abortConversation', dto);
    }
}
