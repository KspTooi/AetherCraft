import type RecoverRpChatHistoryVo from './RecoverRpChatHistoryVo';

/**
 * 恢复角色扮演聊天响应 VO
 */
export default interface RecoverRpChatVo {

    /**
     * 角色对话ID
     * Corresponds to Java Long
     */
    threadId: string; 

    /**
     * AI模型代码
     * Corresponds to Java String
     */
    modelCode: string;

    /**
     * 对话串
     * Corresponds to Java List<RecoverRpChatHistoryVo>
     */
    messages: RecoverRpChatHistoryVo[];

} 