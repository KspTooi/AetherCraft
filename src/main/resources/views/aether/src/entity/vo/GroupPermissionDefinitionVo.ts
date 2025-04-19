/**
 * 组权限定义视图对象
 */
export default interface GroupPermissionDefinitionVo {
    /**
     * 权限节点ID
     */
    id: string;

    /**
     * 权限节点标识
     */
    code: string;

    /**
     * 权限节点名称
     */
    name: string;

    /**
     * 当前组是否拥有 0:拥有 1:不拥有
     */
    has: number;
} 