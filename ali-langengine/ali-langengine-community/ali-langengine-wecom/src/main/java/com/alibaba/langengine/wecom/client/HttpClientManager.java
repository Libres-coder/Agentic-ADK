package com.alibaba.langengine.wecom.client;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;


@Component
public class HttpClientManager {
    
    private static final Logger log = LoggerFactory.getLogger(HttpClientManager.class);
    
    private static volatile CloseableHttpClient sharedClient;
    private static PoolingHttpClientConnectionManager connectionManager;
    
    static {
        initializeHttpClient();
    }
    
    /**
     * 初始化HTTP客户端
     */
    private static synchronized void initializeHttpClient() {
        if (sharedClient == null) {
            // 创建连接池管理器
            connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(100); // 最大连接数
            connectionManager.setDefaultMaxPerRoute(20); // 每个路由的最大连接数
            
            // 配置请求参数
            RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(30000)) // 连接超时30秒
                .setResponseTimeout(Timeout.ofMilliseconds(30000)) // 响应超时30秒
                .build();
            
            // 创建HTTP客户端
            sharedClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
            
            log.info("HTTP客户端初始化完成，连接池大小：{}", connectionManager.getMaxTotal());
        }
    }
    
    /**
     * 获取共享的HTTP客户端实例
     * 
     * @return HTTP客户端
     */
    public static CloseableHttpClient getClient() {
        if (sharedClient == null) {
            initializeHttpClient();
        }
        return sharedClient;
    }
    
    /**
     * 获取连接池统计信息
     * 
     * @return 连接池状态信息
     */
    public String getConnectionPoolStats() {
        if (connectionManager != null) {
            return String.format("连接池状态 - 总连接数：%d，可用连接数：%d，租用连接数：%d",
                connectionManager.getTotalStats().getMax(),
                connectionManager.getTotalStats().getAvailable(),
                connectionManager.getTotalStats().getLeased());
        }
        return "连接池未初始化";
    }
    
    /**
     * 关闭HTTP客户端和连接池
     */
    @PreDestroy
    public void shutdown() {
        try {
            if (sharedClient != null) {
                sharedClient.close();
                log.info("HTTP客户端已关闭");
            }
            if (connectionManager != null) {
                connectionManager.close();
                log.info("连接池管理器已关闭");
            }
        } catch (Exception e) {
            log.error("关闭HTTP客户端时发生异常", e);
        }
    }
}
