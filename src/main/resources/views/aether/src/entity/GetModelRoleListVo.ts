/**
 * 获取模型角色列表项 VO
 */
export default interface GetModelRoleListVo {

    /**
     * 角色ID
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

}