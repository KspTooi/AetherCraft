package com.ksptool.ql.commons.utils;

import com.ksptool.ql.commons.exception.BizException;
import okhttp3.OkHttpClient;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * HTTP客户端工具类
 */
public class HttpClientUtils {
    
    private static final Pattern PROXY_PATTERN = Pattern.compile("^(http://|socks5://)([\\d.]+):(\\d{1,5})$");
    
    /**
     * 创建HTTP客户端
     * @param proxy 代理配置，格式为 http://127.0.0.1:8080 或 socks5://127.0.0.1:1080，为null或空字符串时不使用代理
     * @param timeout 读取超时时间（秒）
     * @return 配置好的OkHttpClient
     * @throws BizException 如果代理配置格式错误
     */
    public static OkHttpClient createHttpClient(String proxy, int timeout) throws BizException {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
            .readTimeout(timeout, TimeUnit.SECONDS);
        
        if (StringUtils.hasText(proxy)) {
            if (!PROXY_PATTERN.matcher(proxy).matches()) {
                throw new BizException("代理配置格式错误，正确格式为: http://127.0.0.1:8080 或 socks5://127.0.0.1:1080");
            }
            
            String[] parts = proxy.split("://");
            String proxyType = parts[0];
            String[] hostPort = parts[1].split(":");
            String proxyHost = hostPort[0];
            int proxyPort = Integer.parseInt(hostPort[1]);
            
            if (proxyPort <= 0 || proxyPort > 65535) {
                throw new BizException("代理端口必须在1-65535之间");
            }
            
            if (proxyType.equals("http")) {
                clientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
            }
            
            if (proxyType.equals("socks5")) {
                clientBuilder.proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort)));
            }
        }

        clientBuilder.readTimeout(timeout, TimeUnit.SECONDS);
        return clientBuilder.build();
    }
} 