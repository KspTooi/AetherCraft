import type PageQuery from '@/entity/PageQuery.ts';

export default interface GetModelRoleListDto extends PageQuery {
    /**
     * 关键字搜索(角色名称或描述)
     */
    keyword?: string;
} 