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
package com.alibaba.langengine.googledrive;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Google Drive 配置类
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class GoogleDriveConfiguration {
    
    /**
     * Google OAuth 2.0 客户端ID
     */
    private String clientId;
    
    /**
     * Google OAuth 2.0 客户端密钥
     */
    private String clientSecret;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 应用程序名称
     */
    private String applicationName;
    
    /**
     * 默认文件夹ID（根目录）
     */
    private String defaultFolderId;
    
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
    public GoogleDriveConfiguration() {
        this.applicationName = "LangEngine-GoogleDrive-Integration";
        this.defaultFolderId = "root";
    }
    
    /**
     * 构造函数
     * 
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param refreshToken 刷新令牌
     */
    public GoogleDriveConfiguration(String clientId, String clientSecret, String refreshToken) {
        this();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
    }
    
    /**
     * 构造函数
     * 
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param refreshToken 刷新令牌
     * @param accessToken 访问令牌
     */
    public GoogleDriveConfiguration(String clientId, String clientSecret, String refreshToken, String accessToken) {
        this(clientId, clientSecret, refreshToken);
        this.accessToken = accessToken;
    }
    
    /**
     * 验证配置是否有效
     * 
     * @return 配置是否有效
     */
    public boolean isValid() {
        if (StringUtils.isBlank(clientId)) {
            log.warn("Google Drive clientId is required");
            return false;
        }
        
        if (StringUtils.isBlank(clientSecret)) {
            log.warn("Google Drive clientSecret is required");
            return false;
        }
        
        if (StringUtils.isBlank(refreshToken) && StringUtils.isBlank(accessToken)) {
            log.warn("Either refreshToken or accessToken is required");
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
        log.info("Google Drive access token updated");
    }
    
    /**
     * 清除访问令牌
     */
    public void clearAccessToken() {
        this.accessToken = null;
        log.info("Google Drive access token cleared");
    }
    
    /**
     * 创建默认配置
     * 
     * @return 默认配置实例
     */
    public static GoogleDriveConfiguration createDefault() {
        return new GoogleDriveConfiguration();
    }
    
    /**
     * 创建测试配置
     * 
     * @return 测试配置实例
     */
    public static GoogleDriveConfiguration createTest() {
        GoogleDriveConfiguration config = new GoogleDriveConfiguration();
        config.setClientId("test_client_id");
        config.setClientSecret("test_client_secret");
        config.setRefreshToken("test_refresh_token");
        config.setAccessToken("test_access_token");
        config.setDebugMode(true);
        return config;
    }
    
    @Override
    public String toString() {
        return "GoogleDriveConfiguration{" +
                "clientId='" + (clientId != null ? clientId.substring(0, Math.min(8, clientId.length())) + "..." : "null") + '\'' +
                ", clientSecret='" + (clientSecret != null ? "***" : "null") + '\'' +
                ", refreshToken='" + (refreshToken != null ? refreshToken.substring(0, Math.min(8, refreshToken.length())) + "..." : "null") + '\'' +
                ", accessToken='" + (accessToken != null ? accessToken.substring(0, Math.min(8, accessToken.length())) + "..." : "null") + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", defaultFolderId='" + defaultFolderId + '\'' +
                ", debugMode=" + debugMode +
                ", timeoutMs=" + timeoutMs +
                '}';
    }
}
