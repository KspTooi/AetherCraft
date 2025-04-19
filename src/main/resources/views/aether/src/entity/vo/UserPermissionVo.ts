/**
 * 用户权限VO
 */
export default interface UserPermissionVo {
  /**
   * 权限ID
   */
  id: string;
  
  /**
   * 权限键
   */
  permKey: string;
  
  /**
   * 权限名称
   */
  name: string;
  
  /**
   * 权限描述
   */
  description: string;
  
  /**
   * 是否为系统内置权限
   */
  isSystem: boolean;
} 