/**
 * 分页查询基础接口
 */
export default interface PageQuery {
    /**
     * 页码，从1开始
     */
    page: number;
    
    /**
     * 每页数量
     */
    pageSize: number;
} 