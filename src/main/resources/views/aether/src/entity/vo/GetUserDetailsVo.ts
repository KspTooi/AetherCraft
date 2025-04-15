import UserGroupVo from './UserGroupVo.ts';
import UserPermissionVo from './UserPermissionVo.ts';

/**
 * 用户详情数据
 */
export default interface GetUserDetailsVo {
  /**
   * 用户ID
   */
  id: string;
  
  /**
   * 用户名
   */
  username: string;
  
  /**
   * 用户昵称
   */
  nickname?: string;
  
  /**
   * 用户邮箱
   */
  email?: string;
  
  /**
   * 用户状态
   */
  status?: number;
  
  /**
   * 创建时间
   */
  createTime?: string;
  
  /**
   * 最后登录时间
   */
  lastLoginTime?: string;
  
  /**
   * 用户组列表
   */
  groups?: UserGroupVo[];
  
  /**
   * 用户权限列表
   */
  permissions?: UserPermissionVo[];
} 