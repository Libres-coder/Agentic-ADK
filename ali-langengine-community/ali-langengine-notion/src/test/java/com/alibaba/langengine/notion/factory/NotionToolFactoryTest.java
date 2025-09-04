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
package com.alibaba.langengine.notion.factory;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.notion.NotionConfiguration;
import com.alibaba.langengine.notion.NotionToolFactory;
import com.alibaba.langengine.notion.tools.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Notion工具工厂测试")
class NotionToolFactoryTest {
    
    private NotionToolFactory factory;
    
    @BeforeEach
    void setUp() {
        NotionConfiguration config = new NotionConfiguration();
        config.setToken("test-token");
        factory = new NotionToolFactory(config);
    }
    
    @Test
    @DisplayName("获取所有工具测试")
    void testGetAllTools() {
        List<BaseTool> tools = factory.getAllTools();
        
        assertNotNull(tools);
        assertFalse(tools.isEmpty());
        assertEquals(5, tools.size());
        
        // 验证工具类型
        assertTrue(tools.stream().anyMatch(tool -> tool instanceof NotionSearchTool));
        assertTrue(tools.stream().anyMatch(tool -> tool instanceof NotionDatabaseQueryTool));
        assertTrue(tools.stream().anyMatch(tool -> tool instanceof NotionPageCreateTool));
        assertTrue(tools.stream().anyMatch(tool -> tool instanceof NotionPageContentTool));
    }
    
    @Test
    @DisplayName("获取搜索工具测试")
    void testGetSearchTool() {
        BaseTool tool = factory.getSearchTool();
        
        assertNotNull(tool);
        assertTrue(tool instanceof NotionSearchTool);
        assertEquals("notion_search", tool.getName());
    }
    
    @Test
    @DisplayName("获取数据库查询工具测试")
    void testGetDatabaseQueryTool() {
        BaseTool tool = factory.getDatabaseQueryTool();
        
        assertNotNull(tool);
        assertTrue(tool instanceof NotionDatabaseQueryTool);
        assertEquals("notion_database_query", tool.getName());
    }
    
    @Test
    @DisplayName("获取页面创建工具测试")
    void testGetPageCreateTool() {
        BaseTool tool = factory.getPageCreateTool();
        
        assertNotNull(tool);
        assertTrue(tool instanceof NotionPageCreateTool);
        assertEquals("notion_page_create", tool.getName());
    }
    
    @Test
    @DisplayName("获取页面内容工具测试")
    void testGetPageContentTool() {
        BaseTool tool = factory.getPageContentTool();
        
        assertNotNull(tool);
        assertTrue(tool instanceof NotionPageContentTool);
        assertEquals("notion_page_content", tool.getName());
    }
    
    @Test
    @DisplayName("工具名称唯一性测试")
    void testToolNameUniqueness() {
        List<BaseTool> tools = factory.getAllTools();
        
        // 验证所有工具名称都是唯一的
        long uniqueNameCount = tools.stream()
                .map(BaseTool::getName)
                .distinct()
                .count();
        
        assertEquals(tools.size(), uniqueNameCount);
    }
    
    @Test
    @DisplayName("工具描述非空测试")
    void testToolDescriptionsNotEmpty() {
        List<BaseTool> tools = factory.getAllTools();
        
        for (BaseTool tool : tools) {
            String description = tool.getDescription();
            assertNotNull(description, "Tool " + tool.getName() + " description should not be null");
            assertFalse(description.trim().isEmpty(), "Tool " + tool.getName() + " description should not be empty");
        }
    }
    
    @Test
    @DisplayName("工具实例独立性测试")
    void testToolInstanceIndependence() {
        // 多次获取同一类型的工具，应该是不同的实例
        BaseTool searchTool1 = factory.getSearchTool();
        BaseTool searchTool2 = factory.getSearchTool();
        
        assertNotSame(searchTool1, searchTool2);
        assertEquals(searchTool1.getName(), searchTool2.getName());
    }
    
    @Test
    @DisplayName("工厂实例可重用性测试")
    void testFactoryReusability() {
        // 测试工厂可以多次使用
        List<BaseTool> tools1 = factory.getAllTools();
        List<BaseTool> tools2 = factory.getAllTools();
        
        assertEquals(tools1.size(), tools2.size());
        
        // 验证工具类型相同但实例不同
        for (int i = 0; i < tools1.size(); i++) {
            assertEquals(tools1.get(i).getClass(), tools2.get(i).getClass());
            assertNotSame(tools1.get(i), tools2.get(i));
        }
    }
    
    @Test
    @DisplayName("按名称获取工具测试")
    void testGetToolByName() {
        BaseTool searchTool = factory.getToolByName("notion_search");
        assertNotNull(searchTool);
        assertTrue(searchTool instanceof NotionSearchTool);
        
        BaseTool queryTool = factory.getToolByName("notion_database_query");
        assertNotNull(queryTool);
        assertTrue(queryTool instanceof NotionDatabaseQueryTool);
        
        BaseTool createTool = factory.getToolByName("notion_page_create");
        assertNotNull(createTool);
        assertTrue(createTool instanceof NotionPageCreateTool);
        
        BaseTool contentTool = factory.getToolByName("notion_page_content");
        assertNotNull(contentTool);
        assertTrue(contentTool instanceof NotionPageContentTool);
    }
    
    @Test
    @DisplayName("不存在的工具名称测试")
    void testGetNonExistentTool() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getToolByName("non_existent_tool");
        });
    }
    
    @Test
    @DisplayName("获取支持的工具类型测试")
    void testGetSupportedToolTypes() {
        List<String> supportedTypes = factory.getSupportedToolTypes();
        
        assertNotNull(supportedTypes);
        assertEquals(5, supportedTypes.size());
        assertTrue(supportedTypes.contains("notion_search"));
        assertTrue(supportedTypes.contains("notion_database_query"));
        assertTrue(supportedTypes.contains("notion_page_create"));
        assertTrue(supportedTypes.contains("notion_page_content"));
    }
    
    @Test
    @DisplayName("批量创建工具测试")
    void testBatchCreateTools() {
        List<String> toolNames = List.of("notion_search", "notion_database_query");
        List<BaseTool> tools = factory.createTools(toolNames);
        
        assertNotNull(tools);
        assertEquals(2, tools.size());
        
        assertEquals("notion_search", tools.get(0).getName());
        assertEquals("notion_database_query", tools.get(1).getName());
    }
    
    @Test
    @DisplayName("批量创建工具 - 包含无效名称测试")
    void testBatchCreateToolsWithInvalidNames() {
        List<String> toolNames = List.of("notion_search", "invalid_tool", "notion_page_create");
        List<BaseTool> tools = factory.createTools(toolNames);
        
        assertNotNull(tools);
        assertEquals(2, tools.size()); // 只有有效的工具被创建
        
        assertEquals("notion_search", tools.get(0).getName());
        assertEquals("notion_page_create", tools.get(1).getName());
    }
    
    @Test
    @DisplayName("工厂线程安全测试")
    void testFactoryThreadSafety() throws InterruptedException {
        final int threadCount = 10;
        final Thread[] threads = new Thread[threadCount];
        final BaseTool[] results = new BaseTool[threadCount];
        
        // 创建多个线程同时获取工具
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = factory.getSearchTool();
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 验证所有工具都被正确创建
        for (BaseTool tool : results) {
            assertNotNull(tool);
            assertTrue(tool instanceof NotionSearchTool);
            assertEquals("notion_search", tool.getName());
        }
    }
}
