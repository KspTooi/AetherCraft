/**
 * 用户组VO
 */
export default interface UserGroupVo {
  /**
   * 用户组ID
   */
  id: string;
  
  /**
   * 用户组名称
   */
  name: string;
  
  /**
   * 用户组描述
   */
  description?: string;
  
  /**
   * 排序顺序
   */
  sortOrder?: number;
  
  /**
   * 是否为系统内置组
   */
  isSystem?: boolean;
  
  /**
   * 用户是否属于此组
   */
  hasGroup?: boolean;
} 