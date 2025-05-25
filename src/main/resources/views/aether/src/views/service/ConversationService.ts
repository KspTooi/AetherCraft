import type {MessageFragmentVo, SendMessageDto, SendMessageVo, QueryStreamDto, RegenerateDto} from "@/commons/api/ConversationApi.ts";
import ConversationApi from "@/commons/api/ConversationApi.ts";

// 私有轮询方法
const pollMessage = async (streamId: string, callback: (fragment: MessageFragmentVo) => void): Promise<void> => {
    let isPolling = true;
    
    while (isPolling) {
        try {
            const queryStreamDto: QueryStreamDto = {
                streamId: streamId
            };

            const segment = await ConversationApi.queryStream(queryStreamDto);

            // 所有类型的消息片段都调用回调函数
            callback(segment);

            // 检查是否结束
            if (segment.type === 2) { // 结束片段
                isPolling = false;
                break;
            }
            if (segment.type === 10) { // 错误片段
                console.error('AI生成错误:', segment.content);
                isPolling = false;
                break;
            }

        } catch (error) {
            console.error('轮询AI响应请求失败:', error);
            isPolling = false;
            break;
        }
    }
}

export default {

    async sendMessage(param: SendMessageDto, callback: (fragment: MessageFragmentVo) => void): Promise<SendMessageVo> {
        try {
            // 发送消息
            const response = await ConversationApi.sendMessage(param);
            
            // 开始轮询响应流
            pollMessage(response.streamId, callback);
            
            return response;
        } catch (error) {
            console.error('发送消息失败:', error);
            throw error;
        }
    },
    
    async regenerate(param: RegenerateDto, callback: (fragment: MessageFragmentVo) => void): Promise<SendMessageVo> {
        try {
            // 重新生成消息
            const response = await ConversationApi.regenerate(param);
            
            // 开始轮询响应流
            pollMessage(response.streamId, callback);
            
            return response;
        } catch (error) {
            console.error('重新生成消息失败:', error);
            throw error;
        }
    }
    
}