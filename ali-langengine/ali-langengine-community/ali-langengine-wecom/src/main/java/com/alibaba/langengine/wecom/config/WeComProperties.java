package com.alibaba.langengine.wecom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@ConfigurationProperties(prefix = "wecom")
@Validated
public class WeComProperties {
    
    @NotBlank(message = "企业ID不能为空")
    private String corpId;
    
    @NotBlank(message = "应用密钥不能为空")
    private String corpSecret;
    
    @NotBlank(message = "应用ID不能为空")
    private String agentId;
    
    /**
     * 回调验证Token（可选）
     */
    private String token;
    
    /**
     * 回调加密密钥（可选）
     */
    private String encodingAESKey;
    
    /**
     * API相关配置
     */
    @Valid
    @NotNull
    private Api api = new Api();
    
    /**
     * 重试相关配置
     */
    @Valid
    @NotNull
    private Retry retry = new Retry();
    
    /**
     * 缓存相关配置
     */
    @Valid
    @NotNull
    private Cache cache = new Cache();
    
    /**
     * API配置
     */
    public static class Api {
        /**
         * API基础URL
         */
        private String baseUrl = "https://qyapi.weixin.qq.com";
        
        /**
         * 连接超时时间（毫秒）
         */
        @Min(value = 1000, message = "连接超时时间不能小于1秒")
        @Max(value = 60000, message = "连接超时时间不能大于60秒")
        private int connectTimeout = 30000;
        
        /**
         * 读取超时时间（毫秒）
         */
        @Min(value = 1000, message = "读取超时时间不能小于1秒")
        @Max(value = 120000, message = "读取超时时间不能大于2分钟")
        private int readTimeout = 30000;
        
        /**
         * 是否启用调试模式
         */
        private boolean debug = false;
        
        // Getters and Setters
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public int getConnectTimeout() {
            return connectTimeout;
        }
        
        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }
        
        public int getReadTimeout() {
            return readTimeout;
        }
        
        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }
        
        public boolean isDebug() {
            return debug;
        }
        
        public void setDebug(boolean debug) {
            this.debug = debug;
        }
    }
    
    /**
     * 重试配置
     */
    public static class Retry {
        /**
         * 最大重试次数
         */
        @Min(value = 0, message = "重试次数不能小于0")
        @Max(value = 10, message = "重试次数不能大于10")
        private int maxAttempts = 3;
        
        /**
         * 重试间隔（毫秒）
         */
        @Min(value = 100, message = "重试间隔不能小于100毫秒")
        @Max(value = 10000, message = "重试间隔不能大于10秒")
        private long interval = 1000;
        
        // Getters and Setters
        public int getMaxAttempts() {
            return maxAttempts;
        }
        
        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
        
        public long getInterval() {
            return interval;
        }
        
        public void setInterval(long interval) {
            this.interval = interval;
        }
    }
    
    /**
     * 缓存配置
     */
    public static class Cache {
        /**
         * Access Token缓存时间（秒）
         */
        @Min(value = 60, message = "Token缓存时间不能小于60秒")
        @Max(value = 7200, message = "Token缓存时间不能大于7200秒")
        private int accessTokenCacheTime = 7200;
        
        /**
         * 是否启用缓存
         */
        private boolean enabled = true;
        
        // Getters and Setters
        public int getAccessTokenCacheTime() {
            return accessTokenCacheTime;
        }
        
        public void setAccessTokenCacheTime(int accessTokenCacheTime) {
            this.accessTokenCacheTime = accessTokenCacheTime;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    // Main class getters and setters
    public String getCorpId() {
        return corpId;
    }
    
    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }
    
    public String getCorpSecret() {
        return corpSecret;
    }
    
    public void setCorpSecret(String corpSecret) {
        this.corpSecret = corpSecret;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getEncodingAESKey() {
        return encodingAESKey;
    }
    
    public void setEncodingAESKey(String encodingAESKey) {
        this.encodingAESKey = encodingAESKey;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
    
    public Retry getRetry() {
        return retry;
    }
    
    public void setRetry(Retry retry) {
        this.retry = retry;
    }
    
    public Cache getCache() {
        return cache;
    }
    
    public void setCache(Cache cache) {
        this.cache = cache;
    }
}
