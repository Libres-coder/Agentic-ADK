package com.alibaba.langengine.wecom.health;

import com.alibaba.langengine.wecom.client.WeComClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;


@Component("weComHealthIndicator")
public class WeComHealthIndicator {
    
    private static final Logger logger = LoggerFactory.getLogger(WeComHealthIndicator.class);
    
    private final WeComClient weComClient;
    
    public WeComHealthIndicator(WeComClient weComClient) {
        this.weComClient = weComClient;
    }
    
    /**
     * 检查企业微信服务健康状态
     * 
     * @return 健康检查结果Map
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 尝试获取token验证连通性
            String token = weComClient.getAccessToken();
            
            if (token != null && !token.isEmpty()) {
                result.put("status", "UP");
                result.put("details", Map.of(
                    "message", "企业微信连接正常",
                    "tokenStatus", "Access Token获取成功",
                    "timestamp", Instant.now().toString(),
                    "apiBaseUrl", weComClient.getConfiguration().getBaseUrl()
                ));
            } else {
                result.put("status", "DOWN");
                result.put("details", Map.of(
                    "message", "企业微信Token获取失败",
                    "error", "返回的Token为空",
                    "timestamp", Instant.now().toString()
                ));
            }
            
        } catch (Exception e) {
            logger.warn("企业微信健康检查失败", e);
            result.put("status", "DOWN");
            result.put("details", Map.of(
                "message", "企业微信连接异常",
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName(),
                "timestamp", Instant.now().toString()
            ));
        }
        
        return result;
    }
}
