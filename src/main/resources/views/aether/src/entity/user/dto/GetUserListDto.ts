/**
 * 用户列表查询参数
 */
export default interface GetUserListDto {
  /**
   * 当前页码
   */
  page: number;

  /**
   * 每页数量
   */
  pageSize: number;

  /**
   * 用户名查询
   */
  username?: string;
} 