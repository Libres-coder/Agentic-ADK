/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.onedrive;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * OneDrive 配置类
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class OneDriveConfiguration {
    
    /**
     * Azure 应用程序（客户端）ID
     */
    private String clientId;
    
    /**
     * Azure 应用程序（客户端）密钥
     */
    private String clientSecret;
    
    /**
     * 租户ID
     */
    private String tenantId;
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 重定向URI
     */
    private String redirectUri;
    
    /**
     * 默认驱动器ID
     */
    private String defaultDriveId;
    
    /**
     * 是否启用调试模式
     */
    private boolean debugMode = false;
    
    /**
     * 请求超时时间（毫秒）
     */
    private int timeoutMs = 30000;
    
    /**
     * 默认构造函数
     */
    public OneDriveConfiguration() {
        this.redirectUri = "http://localhost:8080/auth/callback";
        this.tenantId = "common";
    }
    
    /**
     * 构造函数
     * 
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param accessToken 访问令牌
     */
    public OneDriveConfiguration(String clientId, String clientSecret, String accessToken) {
        this();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessToken = accessToken;
    }
    
    /**
     * 构造函数
     * 
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param tenantId 租户ID
     * @param accessToken 访问令牌
     */
    public OneDriveConfiguration(String clientId, String clientSecret, String tenantId, String accessToken) {
        this(clientId, clientSecret, accessToken);
        this.tenantId = tenantId;
    }
    
    /**
     * 验证配置是否有效
     * 
     * @return 配置是否有效
     */
    public boolean isValid() {
        if (StringUtils.isBlank(clientId)) {
            log.warn("OneDrive clientId is required");
            return false;
        }
        
        if (StringUtils.isBlank(clientSecret)) {
            log.warn("OneDrive clientSecret is required");
            return false;
        }
        
        if (StringUtils.isBlank(accessToken) && StringUtils.isBlank(refreshToken)) {
            log.warn("Either accessToken or refreshToken is required");
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取有效的访问令牌
     * 
     * @return 访问令牌
     */
    public String getEffectiveAccessToken() {
        if (StringUtils.isNotBlank(accessToken)) {
            return accessToken;
        }
        
        if (StringUtils.isNotBlank(refreshToken)) {
            // 这里应该实现刷新令牌的逻辑
            // 为了简化，直接返回刷新令牌
            return refreshToken;
        }
        
        return null;
    }
    
    /**
     * 设置访问令牌
     * 
     * @param accessToken 访问令牌
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        log.info("OneDrive access token updated");
    }
    
    /**
     * 清除访问令牌
     */
    public void clearAccessToken() {
        this.accessToken = null;
        log.info("OneDrive access token cleared");
    }
    
    /**
     * 获取授权URL
     * 
     * @return 授权URL
     */
    public String getAuthorizationUrl() {
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(redirectUri)) {
            return null;
        }
        
        return String.format(
            "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?client_id=%s&response_type=code&redirect_uri=%s&scope=Files.ReadWrite&response_mode=query",
            tenantId, clientId, redirectUri
        );
    }
    
    /**
     * 创建默认配置
     * 
     * @return 默认配置实例
     */
    public static OneDriveConfiguration createDefault() {
        return new OneDriveConfiguration();
    }
    
    /**
     * 创建测试配置
     * 
     * @return 测试配置实例
     */
    public static OneDriveConfiguration createTest() {
        OneDriveConfiguration config = new OneDriveConfiguration();
        config.setClientId("test_client_id");
        config.setClientSecret("test_client_secret");
        config.setTenantId("test_tenant_id");
        config.setAccessToken("test_access_token");
        config.setDebugMode(true);
        return config;
    }
    
    @Override
    public String toString() {
        return "OneDriveConfiguration{" +
                "clientId='" + (clientId != null ? clientId.substring(0, Math.min(8, clientId.length())) + "..." : "null") + '\'' +
                ", clientSecret='" + (clientSecret != null ? "***" : "null") + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", accessToken='" + (accessToken != null ? accessToken.substring(0, Math.min(8, accessToken.length())) + "..." : "null") + '\'' +
                ", refreshToken='" + (refreshToken != null ? refreshToken.substring(0, Math.min(8, refreshToken.length())) + "..." : "null") + '\'' +
                ", redirectUri='" + redirectUri + '\'' +
                ", defaultDriveId='" + defaultDriveId + '\'' +
                ", debugMode=" + debugMode +
                ", timeoutMs=" + timeoutMs +
                '}';
    }
}
