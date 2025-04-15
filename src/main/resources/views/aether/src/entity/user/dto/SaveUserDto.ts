/**
 * 用户保存参数
 */
export default interface SaveUserDto {
  /**
   * 用户ID，创建时为空
   */
  id?: string;
  
  /**
   * 用户名
   */
  username: string;
  
  /**
   * 用户密码，创建时必填，编辑时可选
   */
  password?: string;
  
  /**
   * 用户昵称
   */
  nickname?: string;
  
  /**
   * 用户邮箱
   */
  email?: string;
  
  /**
   * 用户状态：0-禁用，1-启用
   */
  status?: number;
  
  /**
   * 用户组ID列表
   */
  groupIds?: string[];
} 