/**
 * 分页视图数据接口
 * T - 列表行的类型
 */
export default interface PageableView<T> {

    /**
     * 当前页的数据列表
     */
    rows: T[];

    /**
     * 总记录数
     */
    count: number; // Corresponds to Java long

    /**
     * 当前页码
     */
    currentPage: number; // Corresponds to Java int

    /**
     * 每页记录数
     */
    pageSize: number; // Corresponds to Java int

}