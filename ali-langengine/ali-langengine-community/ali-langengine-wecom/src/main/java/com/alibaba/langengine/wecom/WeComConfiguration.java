package com.alibaba.langengine.wecom;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;


@Data
public class WeComConfiguration {
    
    /**
     * 企业ID
     */
    private String corpId;
    
    /**
     * 应用的凭证密钥
     */
    private String corpSecret;
    
    /**
     * 应用ID
     */
    private String agentId;
    
    /**
     * 回调URL验证Token
     */
    private String token;
    
    /**
     * API基础URL
     */
    private String baseUrl = "https://qyapi.weixin.qq.com";
    
    /**
     * 回调消息加解密参数
     */
    private String encodingAESKey;
    
    /**
     * API调用域名，默认为企业微信官方域名
     */
    private String apiUrl = "https://qyapi.weixin.qq.com";
    
    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 30000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 30000;
    
    /**
     * 是否开启调试模式
     */
    private boolean debug = false;
    
    /**
     * 最大重试次数
     */
    private int maxRetries = 3;
    
    /**
     * 重试间隔（毫秒）
     */
    private int retryInterval = 1000;
    
    /**
     * AccessToken缓存时间（秒）
     */
    private int accessTokenCacheTime = 7200;
    
    /**
     * 默认构造函数，从系统属性加载配置
     */
    public WeComConfiguration() {
        loadFromSystemProperties();
    }
    
    /**
     * 构造函数
     */
    public WeComConfiguration(String corpId, String corpSecret, String agentId) {
        this.corpId = corpId;
        this.corpSecret = corpSecret;
        this.agentId = agentId;
    }
    
    /**
     * 全参数构造函数
     */
    public WeComConfiguration(String corpId, String corpSecret, String agentId, String token, String encodingAESKey) {
        this.corpId = corpId;
        this.corpSecret = corpSecret;
        this.agentId = agentId;
        this.token = token;
        this.encodingAESKey = encodingAESKey;
    }
    
    /**
     * 构造函数（包含baseUrl参数）
     */
    public WeComConfiguration(String corpId, String corpSecret, String agentId, String baseUrl) {
        this.corpId = corpId;
        this.corpSecret = corpSecret;
        this.agentId = agentId;
        if (StringUtils.isNotBlank(baseUrl)) {
            this.baseUrl = baseUrl;
        }
    }
    
    /**
     * 从系统属性加载配置
     */
    private void loadFromSystemProperties() {
        this.corpId = System.getProperty("wecom.corp.id");
        this.corpSecret = System.getProperty("wecom.corp.secret");
        this.agentId = System.getProperty("wecom.agent.id");
        this.token = System.getProperty("wecom.token");
        this.encodingAESKey = System.getProperty("wecom.encoding.aes.key");
        
        String timeoutStr = System.getProperty("wecom.connect.timeout");
        if (StringUtils.isNotBlank(timeoutStr)) {
            try {
                this.connectTimeout = Integer.parseInt(timeoutStr);
            } catch (NumberFormatException e) {
                // 保持默认值
            }
        }
        
        String readTimeoutStr = System.getProperty("wecom.read.timeout");
        if (StringUtils.isNotBlank(readTimeoutStr)) {
            try {
                this.readTimeout = Integer.parseInt(readTimeoutStr);
            } catch (NumberFormatException e) {
                // 保持默认值
            }
        }
        
        String debugStr = System.getProperty("wecom.debug");
        if (StringUtils.isNotBlank(debugStr)) {
            this.debug = Boolean.parseBoolean(debugStr);
        }
    }
    
    /**
     * 验证配置是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(corpId) 
            && StringUtils.isNotBlank(corpSecret) 
            && StringUtils.isNotBlank(agentId);
    }
    
    /**
     * 验证回调配置是否有效
     * 
     * @return 是否有效
     */
    public boolean isCallbackValid() {
        return isValid() 
            && StringUtils.isNotBlank(token) 
            && StringUtils.isNotBlank(encodingAESKey);
    }
    
    /**
     * 获取调试信息（安全版本，不泄露敏感信息）
     * 
     * @return 调试信息
     */
    public String getDebugInfo() {
        return String.format("WeComConfiguration{corpIdPresent=%s, agentIdPresent=%s, baseUrl='%s', debug=%s}", 
            StringUtils.isNotBlank(corpId), 
            StringUtils.isNotBlank(agentId), 
            baseUrl, debug);
    }
    
    @Override
    public String toString() {
        return String.format("WeComConfiguration{corpId='%s', agentId='%s', baseUrl='%s'}", 
            corpId != null ? corpId.substring(0, Math.min(3, corpId.length())) + "***_id" : null,
            agentId, baseUrl);
    }

    // 手动添加getter方法确保编译通过
    public String getCorpId() {
        return corpId;
    }
    
    public String getCorpSecret() {
        return corpSecret;
    }
    
    public String getAgentId() {
        return agentId;
    }
}