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
package com.alibaba.langengine.notion;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.notion.client.NotionClient;
import com.alibaba.langengine.notion.tools.NotionContentManageTool;
import com.alibaba.langengine.notion.tools.NotionDatabaseQueryTool;
import com.alibaba.langengine.notion.tools.NotionPageContentTool;
import com.alibaba.langengine.notion.tools.NotionPageCreateTool;
import com.alibaba.langengine.notion.tools.NotionSearchTool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class NotionToolFactory {
    
    private final NotionConfiguration configuration;

    /**
     * 构造函数
     * 
     * @param configuration Notion配置
     */
    public NotionToolFactory(NotionConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("NotionConfiguration cannot be null");
        }
        if (!configuration.isValid()) {
            throw new IllegalArgumentException("Invalid NotionConfiguration: " + configuration);
        }
        
        this.configuration = configuration;
        log.info("NotionToolFactory initialized with configuration: {}", configuration);
    }

    /**
     * 创建搜索工具
     * 
     * @return Notion搜索工具
     */
    public NotionSearchTool getSearchTool() {
        return new NotionSearchTool(configuration);
    }

    /**
     * 创建数据库查询工具
     * 
     * @return Notion数据库查询工具
     */
    public NotionDatabaseQueryTool getDatabaseQueryTool() {
        return new NotionDatabaseQueryTool(configuration);
    }

    /**
     * 创建页面创建工具
     * 
     * @return Notion页面创建工具
     */
    public NotionPageCreateTool getPageCreateTool() {
        return new NotionPageCreateTool(configuration);
    }

    /**
     * 创建页面内容管理工具
     * 
     * @return Notion页面内容管理工具
     */
    public NotionPageContentTool getPageContentTool() {
        return new NotionPageContentTool(configuration);
    }

    /**
     * 创建内容管理工具
     * 
     * @return Notion内容管理工具
     */
    public NotionContentManageTool getContentManageTool() {
        return new NotionContentManageTool(new NotionClient(configuration));
    }

    /**
     * 创建所有Notion工具
     * 
     * @return 所有Notion工具的列表
     */
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        
        tools.add(getSearchTool());
        tools.add(getDatabaseQueryTool());
        tools.add(getPageCreateTool());
        tools.add(getPageContentTool());
        tools.add(getContentManageTool());
        
        log.info("Created {} Notion tools", tools.size());
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
            case "notion_search":
                return getSearchTool();
            case "notion_database_query":
                return getDatabaseQueryTool();
            case "notion_page_create":
                return getPageCreateTool();
            case "notion_page_content":
                return getPageContentTool();
            case "notion_content_manage":
                return getContentManageTool();
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
        return Arrays.asList("notion_search", "notion_database_query", "notion_page_create", "notion_page_content", "notion_content_manage");
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
     * @return Notion配置
     */
    public NotionConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 创建默认的Notion工具工厂
     * 
     * @return 使用默认配置的工具工厂
     */
    public static NotionToolFactory createDefault() {
        // 创建一个带有测试值的配置
        NotionConfiguration testConfig = new NotionConfiguration("test_token");
        return new NotionToolFactory(testConfig);
    }

    /**
     * 创建带令牌的Notion工具工厂
     * 
     * @param token Notion集成令牌
     * @return 工具工厂
     */
    public static NotionToolFactory create(String token) {
        NotionConfiguration config = new NotionConfiguration(token);
        return new NotionToolFactory(config);
    }

    /**
     * 创建带令牌和版本的Notion工具工厂
     * 
     * @param token Notion集成令牌
     * @param version API版本
     * @return 工具工厂
     */
    public static NotionToolFactory create(String token, String version) {
        NotionConfiguration config = new NotionConfiguration(token, version);
        return new NotionToolFactory(config);
    }

    @Override
    public String toString() {
        return "NotionToolFactory{" +
                "configuration=" + configuration +
                '}';
    }
}
