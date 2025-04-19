/**
 * 用户列表数据
 */
export default interface GetUserListVo {
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
  nickname: string;
  
  /**
   * 用户邮箱
   */
  email: string;
  
  /**
   * 创建时间
   */
  createTime: string;
  
  /**
   * 最后登录时间
   */
  lastLoginTime: string;
  
  /**
   * 用户状态
   */
  status: number;
} 