package com.ksptool.ql.commons.utils;

import com.ksptool.ql.commons.exception.BizException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FluxHttpClient {

    private final OkHttpClient httpClient;

    /**
     * 包装一个现有的 OkHttpClient 实例，为其提供响应式流接口。
     * @param httpClient 要包装的 OkHttpClient 实例。
     */
    public FluxHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 发送一个HTTP请求，并以响应式流的方式返回响应体的每一行。
     * @param request 要发送的 okhttp3.Request 对象。
     * @return 一个 Flux<String>，每个元素是响应体的一行文本。
     */
    public Flux<String> send(Request request){
        return Flux.create(sink -> {
            // 在虚拟线程中执行阻塞的网络IO操作，以避免阻塞平台线程
            Thread.startVirtualThread(() -> {
                try (Response response = this.httpClient.newCall(request).execute()) {

                    if (!response.isSuccessful()) {
                        // 将失败的HTTP响应作为错误信号发出
                        sink.error(new BizException("HTTP request failed: " + response.code() + " " + response.message()));
                        return;
                    }

                    if (response.body() == null) {
                        sink.error(new BizException("HTTP response body is null"));
                        return;
                    }

                    try (var reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        // 循环读取每一行，直到流结束或订阅被取消
                        while ((line = reader.readLine()) != null && !sink.isCancelled()) {
                            sink.next(line); // 发出原始行
                        }
                        // 正常结束信号
                        sink.complete();
                    }
                } catch (Exception e) {
                    // 捕获所有其他异常（如IO异常）并作为错误信号发出
                    sink.error(e);
                }
            });
        });
    }

    /**
     * 获取底层的 OkHttpClient 实例。
     * @return 底层的 OkHttpClient.
     */
    public OkHttpClient getClient(){
        return this.httpClient;
    }

}
