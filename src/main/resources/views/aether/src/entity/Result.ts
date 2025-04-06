/**
 * 通用后端响应结果接口
 * T - 返回数据的类型
 */
export default interface Result<T> {

    /**
     * 业务状态码
     * 0 - 业务正常
     * 1 - 业务异常
     * 2 - 内部服务器错误
     */
    code: number;

    /**
     * 描述信息
     */
    message: string;

    /**
     * 返回数据
     */
    data: T;

}
