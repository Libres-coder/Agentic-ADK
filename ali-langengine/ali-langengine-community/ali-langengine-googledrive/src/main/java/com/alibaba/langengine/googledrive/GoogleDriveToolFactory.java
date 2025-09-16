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

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.googledrive.client.GoogleDriveClient;
import com.alibaba.langengine.googledrive.tools.GoogleDriveDownloadTool;
import com.alibaba.langengine.googledrive.tools.GoogleDriveFileManageTool;
import com.alibaba.langengine.googledrive.tools.GoogleDriveSearchTool;
import com.alibaba.langengine.googledrive.tools.GoogleDriveUploadTool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Google Drive 工具工厂
 * 
 * @author AIDC-AI
 */
@Slf4j
public class GoogleDriveToolFactory {
    
    private final GoogleDriveConfiguration configuration;
    private GoogleDriveClient googleDriveClient;

    /**
     * 构造函数
     * 
     * @param configuration Google Drive配置
     */
    public GoogleDriveToolFactory(GoogleDriveConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("GoogleDriveConfiguration cannot be null");
        }
        if (!configuration.isValid()) {
            throw new IllegalArgumentException("Invalid GoogleDriveConfiguration: " + configuration);
        }
        
        this.configuration = configuration;
        log.info("GoogleDriveToolFactory initialized with configuration: {}", configuration);
    }

    /**
     * 创建搜索工具
     * 
     * @return Google Drive搜索工具
     */
    public GoogleDriveSearchTool getSearchTool() {
        return new GoogleDriveSearchTool(getOrCreateClient());
    }

    /**
     * 创建上传工具
     * 
     * @return Google Drive上传工具
     */
    public GoogleDriveUploadTool getUploadTool() {
        return new GoogleDriveUploadTool(getOrCreateClient());
    }

    /**
     * 创建下载工具
     * 
     * @return Google Drive下载工具
     */
    public GoogleDriveDownloadTool getDownloadTool() {
        return new GoogleDriveDownloadTool(getOrCreateClient());
    }

    /**
     * 创建文件管理工具
     * 
     * @return Google Drive文件管理工具
     */
    public GoogleDriveFileManageTool getFileManageTool() {
        return new GoogleDriveFileManageTool(getOrCreateClient());
    }

    /**
     * 创建所有Google Drive工具
     * 
     * @return 所有Google Drive工具的列表
     */
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        
        tools.add(getSearchTool());
        tools.add(getUploadTool());
        tools.add(getDownloadTool());
        tools.add(getFileManageTool());
        
        log.info("Created {} Google Drive tools", tools.size());
        return tools;
    }

    /**
     * 根据名称获取工具
     * 
     * @param name 工具名称
     * @return 对应的工具实例
     * @throws IllegalArgumentException 不支持的工具名称
     */
    public BaseTool getToolByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool name cannot be null or empty");
        }
        
        switch (name.toLowerCase().trim()) {
            case "google_drive_search":
                return getSearchTool();
            case "google_drive_upload":
                return getUploadTool();
            case "google_drive_download":
                return getDownloadTool();
            case "google_drive_file_manage":
                return getFileManageTool();
            default:
                throw new IllegalArgumentException("Unsupported tool name: " + name);
        }
    }

    /**
     * 获取支持的工具类型
     * 
     * @return 支持的工具类型列表
     */
    public List<String> getSupportedToolTypes() {
        return Arrays.asList("google_drive_search", "google_drive_upload", "google_drive_download", "google_drive_file_manage");
    }

    /**
     * 创建指定类型的工具
     * 
     * @param toolTypes 工具类型列表
     * @return 对应的工具实例列表
     */
    public List<BaseTool> createTools(List<String> toolTypes) {
        if (toolTypes == null || toolTypes.isEmpty()) {
            return getAllTools();
        }
        
        List<BaseTool> tools = new ArrayList<>();
        for (String toolType : toolTypes) {
            try {
                tools.add(getToolByName(toolType));
            } catch (IllegalArgumentException e) {
                log.warn("Skipping unsupported tool type: {}", toolType);
            }
        }
        
        return tools;
    }

    /**
     * 获取配置
     * 
     * @return Google Drive配置
     */
    public GoogleDriveConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 获取或创建Google Drive客户端
     * 
     * @return Google Drive客户端
     */
    private GoogleDriveClient getOrCreateClient() {
        if (googleDriveClient == null) {
            googleDriveClient = new GoogleDriveClient(configuration);
        }
        return googleDriveClient;
    }

    /**
     * 创建默认的Google Drive工具工厂
     * 
     * @return 使用默认配置的工具工厂
     */
    public static GoogleDriveToolFactory createDefault() {
        GoogleDriveConfiguration testConfig = GoogleDriveConfiguration.createTest();
        return new GoogleDriveToolFactory(testConfig);
    }

    /**
     * 创建带认证信息的Google Drive工具工厂
     * 
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param refreshToken 刷新令牌
     * @return 工具工厂
     */
    public static GoogleDriveToolFactory create(String clientId, String clientSecret, String refreshToken) {
        GoogleDriveConfiguration config = new GoogleDriveConfiguration(clientId, clientSecret, refreshToken);
        return new GoogleDriveToolFactory(config);
    }

    /**
     * 创建带访问令牌的Google Drive工具工厂
     * 
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param refreshToken 刷新令牌
     * @param accessToken 访问令牌
     * @return 工具工厂
     */
    public static GoogleDriveToolFactory create(String clientId, String clientSecret, String refreshToken, String accessToken) {
        GoogleDriveConfiguration config = new GoogleDriveConfiguration(clientId, clientSecret, refreshToken, accessToken);
        return new GoogleDriveToolFactory(config);
    }

    @Override
    public String toString() {
        return "GoogleDriveToolFactory{" +
                "configuration=" + configuration +
                '}';
    }
}
