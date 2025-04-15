/**
 * 恢复角色扮演聊天历史记录项 VO
 */
export default interface RecoverRpChatHistoryVo {

    /**
     * 对话历史ID
     */
    id: string; // Corresponds to Java Long

    /**
     * 角色名称
     */
    name: string;

    /**
     * 头像路径
     */
    avatarPath: string;


    //消息类型：0-用户消息，1-AI消息
    type: 0 | 1;

    /**
     * 消息内容
     * Corresponds to Java String rawContent
     */
    rawContent: string;

    /**
     * 消息创建时间 (ISO 8601 格式字符串)
     * Corresponds to Java Date
     */
    createTime: string; 

} 