/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * U        log.info("内容添加成功: {}", !result.isInterrupted());less required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.notion.examples;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.notion.NotionConfiguration;
import com.alibaba.langengine.notion.NotionToolFactory;
import com.alibaba.langengine.notion.tools.NotionDatabaseQueryTool;
import com.alibaba.langengine.notion.tools.NotionPageContentTool;
import com.alibaba.langengine.notion.tools.NotionPageCreateTool;
import com.alibaba.langengine.notion.tools.NotionSearchTool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Notion工具使用示例
 * 
 * @author xiaoxuan.lp
 */
@Slf4j
public class NotionToolsExample {
    
    private static final String NOTION_TOKEN = "secret_your_notion_integration_token_here";
    
    public static void main(String[] args) {
        try {
            // 示例1: 创建所有工具
            exampleCreateAllTools();
            
            // 示例2: 搜索Notion内容
            exampleSearch();
            
            // 示例3: 查询数据库
            exampleQueryDatabase();
            
            // 示例4: 创建页面
            exampleCreatePage();
            
            // 示例5: 添加页面内容
            exampleAddPageContent();
            
        } catch (Exception e) {
            log.error("运行示例时发生异常", e);
        }
    }
    
    /**
     * 示例1: 创建所有工具
     */
    private static void exampleCreateAllTools() {
        log.info("=== 示例1: 创建所有Notion工具 ===");
        
        NotionConfiguration config = new NotionConfiguration(NOTION_TOKEN);
        List<BaseTool> tools = new NotionToolFactory(config).getAllTools();
        
        log.info("成功创建 {} 个Notion工具:", tools.size());
        for (BaseTool tool : tools) {
            log.info("- {}: {}", tool.getName(), tool.getDescription());
        }
    }
    
    /**
     * 示例2: 搜索Notion内容
     */
    private static void exampleSearch() {
        log.info("=== 示例2: 搜索Notion内容 ===");
        
        NotionConfiguration config = new NotionConfiguration(NOTION_TOKEN);
        NotionSearchTool searchTool = new NotionToolFactory(config).getSearchTool();
        
        // 搜索所有页面
        JSONObject searchInput = new JSONObject();
        searchInput.put("query", "项目");
        
        // 添加过滤器，只搜索页面
        JSONObject filter = new JSONObject();
        filter.put("property", "object");
        filter.put("value", "page");
        searchInput.put("filter", filter);
        
        // 添加排序
        JSONObject sort = new JSONObject();
        sort.put("direction", "descending");
        sort.put("timestamp", "last_edited_time");
        searchInput.put("sort", sort);
        
        log.info("搜索输入: {}", searchInput.toJSONString());
        
        ToolExecuteResult result = searchTool.run(searchInput.toJSONString());
        log.info("搜索结果: {}", result.getOutput());
        log.info("搜索成功: {}", !result.isInterrupted());
    }
    
    /**
     * 示例3: 查询数据库
     */
    private static void exampleQueryDatabase() {
        log.info("=== 示例3: 查询Notion数据库 ===");
        
        NotionConfiguration config = new NotionConfiguration(NOTION_TOKEN);
        NotionDatabaseQueryTool queryTool = new NotionToolFactory(config).getDatabaseQueryTool();
        
        // 假设有一个任务数据库
        String databaseId = "your_database_id_here";
        
        JSONObject queryInput = new JSONObject();
        queryInput.put("database_id", databaseId);
        
        // 添加过滤器，查询未完成的任务
        JSONObject filter = new JSONObject();
        filter.put("property", "Status");
        JSONObject checkbox = new JSONObject();
        checkbox.put("equals", false);
        filter.put("checkbox", checkbox);
        queryInput.put("filter", filter);
        
        // 添加排序
        JSONArray sorts = new JSONArray();
        JSONObject sort = new JSONObject();
        sort.put("property", "Created");
        sort.put("direction", "descending");
        sorts.add(sort);
        queryInput.put("sorts", sorts);
        
        queryInput.put("page_size", 10);
        
        log.info("查询输入: {}", queryInput.toJSONString());
        
        ToolExecuteResult result = queryTool.run(queryInput.toJSONString());
        log.info("查询结果: {}", result.getOutput());
        log.info("查询成功: {}", !result.isInterrupted());
    }
    
    /**
     * 示例4: 创建页面
     */
    private static void exampleCreatePage() {
        log.info("=== 示例4: 创建Notion页面 ===");
        
        NotionConfiguration config = new NotionConfiguration(NOTION_TOKEN);
        NotionPageCreateTool createTool = new NotionToolFactory(config).getPageCreateTool();
        
        String databaseId = "your_database_id_here";
        
        JSONObject createInput = new JSONObject();
        createInput.put("database_id", databaseId);
        
        // 设置页面属性
        JSONObject properties = new JSONObject();
        
        // 标题属性
        JSONObject titleProperty = new JSONObject();
        JSONArray titleArray = new JSONArray();
        JSONObject titleText = new JSONObject();
        JSONObject text = new JSONObject();
        text.put("content", "新任务: 集成Notion API");
        titleText.put("text", text);
        titleArray.add(titleText);
        titleProperty.put("title", titleArray);
        properties.put("Name", titleProperty);
        
        // 状态属性（复选框）
        JSONObject statusProperty = new JSONObject();
        statusProperty.put("checkbox", false);
        properties.put("Completed", statusProperty);
        
        // 优先级属性（选择）
        JSONObject priorityProperty = new JSONObject();
        JSONObject select = new JSONObject();
        select.put("name", "高");
        priorityProperty.put("select", select);
        properties.put("Priority", priorityProperty);
        
        createInput.put("properties", properties);
        
        // 添加页面内容
        JSONArray children = new JSONArray();
        
        // 添加段落
        JSONObject paragraph = new JSONObject();
        paragraph.put("type", "paragraph");
        JSONObject paragraphContent = new JSONObject();
        JSONArray richTextArray = new JSONArray();
        JSONObject richText = new JSONObject();
        JSONObject textContent = new JSONObject();
        textContent.put("content", "这是通过API创建的任务页面，包含了详细的任务描述和要求。");
        richText.put("text", textContent);
        richTextArray.add(richText);
        paragraphContent.put("rich_text", richTextArray);
        paragraph.put("paragraph", paragraphContent);
        children.add(paragraph);
        
        createInput.put("children", children);
        
        log.info("创建输入: {}", createInput.toJSONString());
        
        ToolExecuteResult result = createTool.run(createInput.toJSONString());
        log.info("创建结果: {}", result.getOutput());
        log.info("创建成功: {}", !result.isInterrupted());
    }
    
    /**
     * 示例5: 添加页面内容
     */
    private static void exampleAddPageContent() {
        log.info("=== 示例5: 添加页面内容 ===");
        
        NotionConfiguration config = new NotionConfiguration(NOTION_TOKEN);
        NotionPageContentTool contentTool = new NotionToolFactory(config).getPageContentTool();
        
        String pageId = "your_page_id_here";
        
        JSONObject contentInput = new JSONObject();
        contentInput.put("page_id", pageId);
        
        // 创建多种类型的内容块
        JSONArray children = new JSONArray();
        
        // 标题1
        JSONObject heading1 = new JSONObject();
        heading1.put("type", "heading_1");
        JSONObject heading1Content = new JSONObject();
        JSONArray heading1Text = new JSONArray();
        JSONObject heading1RichText = new JSONObject();
        JSONObject heading1TextContent = new JSONObject();
        heading1TextContent.put("content", "任务详情");
        heading1RichText.put("text", heading1TextContent);
        heading1Text.add(heading1RichText);
        heading1Content.put("rich_text", heading1Text);
        heading1.put("heading_1", heading1Content);
        children.add(heading1);
        
        // 项目符号列表
        JSONObject bulletItem = new JSONObject();
        bulletItem.put("type", "bulleted_list_item");
        JSONObject bulletContent = new JSONObject();
        JSONArray bulletText = new JSONArray();
        JSONObject bulletRichText = new JSONObject();
        JSONObject bulletTextContent = new JSONObject();
        bulletTextContent.put("content", "实现Notion API客户端");
        bulletRichText.put("text", bulletTextContent);
        bulletText.add(bulletRichText);
        bulletContent.put("rich_text", bulletText);
        bulletItem.put("bulleted_list_item", bulletContent);
        children.add(bulletItem);
        
        // 代码块
        JSONObject codeBlock = new JSONObject();
        codeBlock.put("type", "code");
        JSONObject codeContent = new JSONObject();
        JSONArray codeText = new JSONArray();
        JSONObject codeRichText = new JSONObject();
        JSONObject codeTextContent = new JSONObject();
        codeTextContent.put("content", "NotionClient client = new NotionClient(config);");
        codeRichText.put("text", codeTextContent);
        codeText.add(codeRichText);
        codeContent.put("rich_text", codeText);
        codeContent.put("language", "java");
        codeBlock.put("code", codeContent);
        children.add(codeBlock);
        
        contentInput.put("children", children);
        
        log.info("内容输入: {}", contentInput.toJSONString());
        
        ToolExecuteResult result = contentTool.run(contentInput.toJSONString());
        log.info("内容添加结果: {}", result.getOutput());
        log.info("添加成功: {}", !result.isInterrupted());
    }
}
