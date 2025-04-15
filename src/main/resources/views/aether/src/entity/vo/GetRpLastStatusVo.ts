/**
 * 获取角色扮演最后状态响应 VO
 */
export default interface GetRpLastStatusVo {

    /**
     * 最后活动的聊天线程ID
     * Corresponds to Java String lastThread
     */
    lastThread: string; // 设置为可选，因为Java代码中做了非空判断

    /**
     * 最后活动的角色ID
     * Corresponds to Java String lastRole
     */
    lastRole: string; // 设置为可选，因为Java代码中做了非空判断

} 