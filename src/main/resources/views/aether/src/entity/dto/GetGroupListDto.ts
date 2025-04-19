import type PageQuery from "@/entity/PageQuery.ts";


export default interface GetGroupListDto extends PageQuery{

    // 模糊匹配 组标识、组名称、组描述
    keyword?: string;

    // 组状态：0:禁用 1:启用
    status?: number;
    
} 