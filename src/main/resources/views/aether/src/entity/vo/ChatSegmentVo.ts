export default interface ChatSegmentVo{

    /** 会话ID */
    threadId?: string; // 后端是 Long，前端统一用 string

    /** 历史记录ID */
    historyId?: string; // 后端是 Long，前端统一用 string

    /** 角色 0-用户 1-AI助手 */
    role?: number;

    /** 片段序号 */
    sequence?: number;

    /** 片段内容 */
    content?: string;

    /** 
     * 片段类型
     * 0 - 开始 (根据Java注释，0是开始，但你的描述中是无数据? 以Java注释为准)
     * 1 - 数据
     * 2 - 结束
     * 10 - 错误
     * null - 无数据 (这里用 undefined 或 null 表示)
     */
    type?: number | null; 

    /** 发送者名称 */
    name?: string;
    
    /** 头像路径 (注意Java类中大小写) */
    avatarPath?: string; // 对应Java的 AvatarPath

}