/**
 * 分页视图数据接口
 * T - 列表行的类型
 */
export default class PageableView<T> {

    /**
     * 当前页的数据列表
     */
    rows: T[];

    /**
     * 总记录数
     */
    private _count?: number | string;

    /**
     * 当前页码
     */
    currentPage: number; // Corresponds to Java int

    /**
     * 每页记录数
     */
    pageSize: number; // Corresponds to Java int

    constructor(data: {
        rows: T[],
        count: number | string,
        currentPage: number,
        pageSize: number
    }) {
        this.rows = data.rows;
        this._count = data.count;
        this.currentPage = data.currentPage;
        this.pageSize = data.pageSize;
    }

    /**
     * 获取总记录数
     * 当后端返回字符串时自动转换为数字
     */
    get count(): number | undefined {
        return typeof this._count === 'string' ? parseInt(this._count, 10) : this._count;
    }

    /**
     * 设置总记录数
     */
    set count(value: number | string) {
        this._count = value;
    }
}