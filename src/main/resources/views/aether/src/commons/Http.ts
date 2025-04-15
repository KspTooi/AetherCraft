import type Result from "@/entity/Result.ts";
import axios from "axios";
import { useRouter } from 'vue-router';

// 创建axios实例
const axiosInstance = axios.create({
    // 为所有请求添加自定义请求头
    headers: {
        'AE-Request-With': 'XHR'
    }
});

// 添加响应拦截器
axiosInstance.interceptors.response.use(
    response => response,
    error => {
        console.log("----------------- ERROR -----------------");

        // 检查是否为 401 Unauthorized 状态码
        if (error.response?.status === 401) { 
            console.log("捕获到 401 Unauthorized 错误，准备重定向到登录页面");
            // 如果是在浏览器环境
            if (typeof window !== 'undefined') {
                // 检查响应头中的 Location 字段
                const location = error.response.headers.location || error.response.headers.Location;
                if (location) {
                    window.location.href = location;
                } else {
                    window.location.href = '/login';
                }
            }
        } else if (error.response?.status === 302 || error.response?.status === 301) {
             // 保留对 301/302 的处理以防万一，虽然理论上XHR不应收到
             const location = error.response.headers.location || error.response.headers.Location;
             if (location?.includes('/login')) {
                 if (typeof window !== 'undefined') {
                     window.location.href = '/login';
                 }
             }
        }
        return Promise.reject(error);
    }
);

export default {
    /**
     * 发送POST请求并返回Result包装的响应
     * @param url 请求URL
     * @param body 请求体
     * @returns Result包装的响应
     */
    async postRaw<T>(url:string, body:any): Promise<Result<T>> {
        try {
            const response = await axiosInstance.post<Result<T>>(url, body);
            return response.data;
        } catch (error) {
            // 如果错误是由 401 拦截器处理并重定向，这里可能不会执行
            // 但保留它以处理其他类型的请求错误
            return {
                code: 2,
                data: null as T,
                message: error instanceof Error ? error.message : "请求失败"
            };
        }
    },

    /**
     * 发送POST请求并直接返回数据部分
     * @param url 请求URL
     * @param body 请求体
     * @returns 响应数据
     */
    async postEntity<T>(url:string, body:any): Promise<T> {
        const result = await this.postRaw<T>(url, body);
        
        // 如果 code 不是 0，postRaw 内部已经处理或拦截器已处理 401
        // 这里只需要处理 code 为 0 但 data 为 null 的情况或其他业务错误
        if (result.code === 0) {
            return result.data;
        }
        
        // 抛出业务错误信息或通用错误
        throw new Error(result.message || "请求失败或数据无效");
    }
}