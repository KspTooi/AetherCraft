import Http from "@/commons/Http.ts";
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts"; // 假设已存在

// --- DTOs ---
export interface EditMessageDto {
    messageId: string;      // 消息ID, JavaType: Long
    content: string;        // 消息内容, JavaType: String
}

export default {
    /**
     * 编辑对话消息
     */
    editMessage: async (dto: EditMessageDto): Promise<string> => {
        return await Http.postEntity<string>('/message/editMessage', dto);
    },

    /**
     * 移除聊天消息
     */
    removeMessage: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/message/removeMessage', dto);
    }
}
