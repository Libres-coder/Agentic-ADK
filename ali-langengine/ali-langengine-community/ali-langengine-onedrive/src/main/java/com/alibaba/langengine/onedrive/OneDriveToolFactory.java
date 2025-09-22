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

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.onedrive.client.OneDriveClient;
import com.alibaba.langengine.onedrive.tools.OneDriveDownloadTool;
import com.alibaba.langengine.onedrive.tools.OneDriveFileManageTool;
import com.alibaba.langengine.onedrive.tools.OneDriveSearchTool;
import com.alibaba.langengine.onedrive.tools.OneDriveUploadTool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * OneDrive 工具工厂
 * 
 * @author AIDC-AI
 */
@Slf4j
public class OneDriveToolFactory {
    
    private final OneDriveConfiguration configuration;
    private OneDriveClient oneDriveClient;

    /**
     * 构造函数
     * 
     * @param configuration OneDrive配置
     */
    public OneDriveToolFactory(OneDriveConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("OneDriveConfiguration cannot be null");
        }
        if (!configuration.isValid()) {
            throw new IllegalArgumentException("Invalid OneDriveConfiguration: " + configuration);
        }
        
        this.configuration = configuration;
        log.info("OneDriveToolFactory initialized with configuration: {}", configuration);
    }

    /**
     * 创建搜索工具
     * 
     * @return OneDrive搜索工具
     */
    public OneDriveSearchTool getSearchTool() {
        return new OneDriveSearchTool(getOrCreateClient());
    }

    /**
     * 创建上传工具
     * 
     * @return OneDrive上传工具
     */
    public OneDriveUploadTool getUploadTool() {
        return new OneDriveUploadTool(getOrCreateClient());
    }

    /**
     * 创建下载工具
     * 
     * @return OneDrive下载工具
     */
    public OneDriveDownloadTool getDownloadTool() {
        return new OneDriveDownloadTool(getOrCreateClient());
    }

    /**
     * 创建文件管理工具
     * 
     * @return OneDrive文件管理工具
     */
    public OneDriveFileManageTool getFileManageTool() {
        return new OneDriveFileManageTool(getOrCreateClient());
    }

    /**
     * 创建所有OneDrive工具
     * 
     * @return 所有OneDrive工具的列表
     */
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        
        tools.add(getSearchTool());
        tools.add(getUploadTool());
        tools.add(getDownloadTool());
        tools.add(getFileManageTool());
        
        log.info("Created {} OneDrive tools", tools.size());
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
            case "onedrive_search":
                return getSearchTool();
            case "onedrive_upload":
                return getUploadTool();
            case "onedrive_download":
                return getDownloadTool();
            case "onedrive_file_manage":
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
        return Arrays.asList("onedrive_search", "onedrive_upload", "onedrive_download", "onedrive_file_manage");
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
     * @return OneDrive配置
     */
    public OneDriveConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 获取或创建OneDrive客户端
     * 
     * @return OneDrive客户端
     */
    private OneDriveClient getOrCreateClient() {
        if (oneDriveClient == null) {
            oneDriveClient = new OneDriveClient(configuration);
        }
        return oneDriveClient;
    }

    /**
     * 创建默认的OneDrive工具工厂
     * 
     * @return 使用默认配置的工具工厂
     */
    public static OneDriveToolFactory createDefault() {
        OneDriveConfiguration testConfig = OneDriveConfiguration.createTest();
        return new OneDriveToolFactory(testConfig);
    }

    /**
     * 创建带认证信息的OneDrive工具工厂
     * 
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param accessToken 访问令牌
     * @return 工具工厂
     */
    public static OneDriveToolFactory create(String clientId, String clientSecret, String accessToken) {
        OneDriveConfiguration config = new OneDriveConfiguration(clientId, clientSecret, accessToken);
        return new OneDriveToolFactory(config);
    }

    /**
     * 创建带租户信息的OneDrive工具工厂
     * 
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param tenantId 租户ID
     * @param accessToken 访问令牌
     * @return 工具工厂
     */
    public static OneDriveToolFactory create(String clientId, String clientSecret, String tenantId, String accessToken) {
        OneDriveConfiguration config = new OneDriveConfiguration(clientId, clientSecret, tenantId, accessToken);
        return new OneDriveToolFactory(config);
    }

    @Override
    public String toString() {
        return "OneDriveToolFactory{" +
                "configuration=" + configuration +
                '}';
    }
}
