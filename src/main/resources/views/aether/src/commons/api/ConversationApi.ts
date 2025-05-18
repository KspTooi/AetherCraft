import Http from "@/commons/Http.ts";

// --- DTOs ---
export interface SendMessageDto {
    threadId?: string;      // 为-1时自动创建新会话, JavaType: Long
    type: number;           // 0:标准会话 1:RP会话 2:增强会话, JavaType: Integer
    modelCode: string;      // JavaType: String
    message: string;        // JavaType: String
}

export interface QueryStreamDto {
    streamId: string;       // JavaType: String
}

export interface RegenerateDto {
    threadId: string;       // JavaType: Long
    modelCode: string;      // JavaType: String
    rootMessageId: string;  // JavaType: Long
}

export interface AbortConversationDto {
    threadId: string;       // JavaType: Long
}

// --- VOs ---
export interface SendMessageVo {
    threadId: string;           // 对话ThreadId, JavaType: Long
    messageId: string;          // 用户消息ID, JavaType: Long
    streamId: string;           // 响应流ID, JavaType: String
    content: string;            // 消息内容, JavaType: String
    senderName: string;         // 发送人姓名, JavaType: String
    senderAvatarUrl: string;    // 发送人头像URL, JavaType: String
    sendTime: string;           // 发送时间, JavaType: String
    title: string;              // 对话Thread 标题, JavaType: String
    newThreadCreated: number;   // 是否创建了新Thread 0:否 1:是, JavaType: Integer
}

export interface MessageFragmentVo {
    type: number;               // 片段类型 0:起始 1:数据 2:结束 10:错误, JavaType: Integer
    threadId: string;           // 对话串ID, JavaType: Long
    messageId: string;          // 消息ID (-1为临时消息), JavaType: Long
    content: string;            // 消息内容, JavaType: String
    seq: number;                // 顺序, JavaType: Integer
    senderRole: number;         // 发送人角色 0:玩家 1:模型, JavaType: Integer
    senderName: string;         // 发送人姓名, JavaType: String
    senderAvatarUrl: string;    // 发送人头像URL, JavaType: String
    sendTime: string;           // 发送时间 yyyy年MM月dd日 HH:mm:ss, JavaType: String
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
