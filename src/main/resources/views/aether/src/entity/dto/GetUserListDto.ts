import type PageQuery from "@/entity/PageQuery.ts";


/**
 * 用户列表查询参数
 */
export default interface GetUserListDto extends PageQuery{

  /**
   * 用户名查询
   */
  username?: string;
} 