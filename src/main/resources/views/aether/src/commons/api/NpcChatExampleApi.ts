import type CommonIdDto from "@/entity/dto/CommonIdDto.ts";
import Http from "@/commons/Http.ts";

export interface SaveNpcChatExampleItemDto {
    id?: string;           // NPC对话示例ID
    content: string;       // (加密)对话内容
    sortOrder: number;     // 排序号
}

export interface SaveNpcChatExampleDto {
    npcId: string;        // NPC ID
    examples: SaveNpcChatExampleItemDto[];
}

export interface GetNpcChatExampleListVo {
    id: string;           // NPC对话示例ID
    content: string;      // (加密)对话内容
    sortOrder: string;    // 排序号
}

export default {
    /**
     * 获取NPC对话示例列表
     */
    getNpcChatExampleList: async (dto: CommonIdDto): Promise<GetNpcChatExampleListVo[]> => {
        return await Http.postEntity<GetNpcChatExampleListVo[]>('/npc/chatExample/getNpcChatExampleList', dto);
    },

    /**
     * 保存NPC对话示例
     */
    saveNpcChatExample: async (dto: SaveNpcChatExampleDto): Promise<string> => {
        return await Http.postEntity<string>('/npc/chatExample/saveNpcChatExample', dto);
    },

    /**
     * 删除NPC对话示例
     */
    removeNpcChatExample: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/npc/chatExample/removeNpcChatExample', dto);
    }
}
