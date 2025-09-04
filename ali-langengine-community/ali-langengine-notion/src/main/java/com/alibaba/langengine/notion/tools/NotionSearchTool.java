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
package com.alibaba.langengine.notion.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.notion.NotionConfiguration;
import com.alibaba.langengine.notion.client.NotionClient;
import com.alibaba.langengine.notion.exception.NotionException;
import com.alibaba.langengine.notion.model.NotionSearchResult;
import com.alibaba.langengine.notion.tools.schema.NotionSearchSchema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;


@Slf4j
@Data
public class NotionSearchTool extends DefaultTool {
    
    private NotionClient notionClient;
    
    /**
     * 默认构造函数
     */
    public NotionSearchTool() {
        setName("notion_search");
        setFunctionName("searchNotion");
        setHumanName("Notion搜索");
        setDescription("搜索Notion工作区中的页面和数据库，支持关键词搜索、类型过滤和排序");
        // 工具已初始化
    }
    
    /**
     * 构造函数
     * 
     * @param configuration Notion配置
     */
    public NotionSearchTool(NotionConfiguration configuration) {
        this();
        this.notionClient = new NotionClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param notionClient Notion客户端
     */
    public NotionSearchTool(NotionClient notionClient) {
        this();
        this.notionClient = notionClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行搜索
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (notionClient == null) {
                return new ToolExecuteResult("Notion客户端未初始化，请先配置Notion token", true);
            }
            
            log.info("执行Notion搜索，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String query = input.getString("query");
            JSONObject filter = input.getJSONObject("filter");
            JSONObject sort = input.getJSONObject("sort");
            
            // 验证输入参数
            if (StringUtils.isBlank(query) && filter == null) {
                return new ToolExecuteResult("搜索查询参数不能为空，请提供query或filter参数", true);
            }
            
            // 执行搜索
            NotionSearchResult result = notionClient.search(query, filter, sort);
            
            if (result == null) {
                return new ToolExecuteResult("搜索失败，未返回结果", true);
            }
            
            // 构建返回结果
            JSONObject response = new JSONObject();
            response.put("type", "search_result");
            response.put("results", result.getResults());
            response.put("next_cursor", result.getNextCursor());
            response.put("has_more", result.getHasMore());
            
            String output = JSON.toJSONString(response);
            log.info("Notion搜索完成，返回结果: {}", output);
            
            return new ToolExecuteResult(output, false);
            
        } catch (NotionException e) {
            log.error("Notion搜索失败", e);
            return new ToolExecuteResult("搜索失败: " + e.getMessage(), true);
        } catch (Exception e) {
            log.error("搜索过程中发生未知错误", e);
            return new ToolExecuteResult("搜索过程中发生未知错误: " + e.getMessage(), true);
        }
    }

    /**
     * 支持Map参数的execute方法（用于测试）
     * 
     * @param parameters 参数Map
     * @return 执行结果
     */
    public ToolExecuteResult execute(Map<String, Object> parameters) {
        return execute(JSON.toJSONString(parameters));
    }
}
