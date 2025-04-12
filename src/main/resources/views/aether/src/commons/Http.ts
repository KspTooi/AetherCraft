import type Result from "@/entity/Result.ts";
import axios from "axios";
import { useRouter } from 'vue-router';

// 创建axios实例
const axiosInstance = axios.create();

// 添加响应拦截器
axiosInstance.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 302 || error.response?.status === 301) {
            const location = error.response.headers.location || error.response.headers.Location;
            if (location?.includes('/login')) {
                // 如果是在浏览器环境
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
        
        if (result.code === 0 && result.data !== null) {
            return result.data;
        }
        
        throw new Error(result.message || "请求失败");
    }
}