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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.notion.NotionConfiguration;
import com.alibaba.langengine.notion.client.NotionClient;
import com.alibaba.langengine.notion.exception.NotionException;
import com.alibaba.langengine.notion.model.NotionBlocksResult;
import com.alibaba.langengine.notion.tools.schema.NotionPageContentSchema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;


@Slf4j
@Data
public class NotionPageContentTool extends DefaultTool {
    
    private NotionClient notionClient;
    
    /**
     * 默认构造函数
     */
    public NotionPageContentTool() {
        setName("notion_page_content");
        setFunctionName("manageNotionPageContent");
        setHumanName("Notion页面内容管理");
        setDescription("管理Notion页面内容，可以添加文本、标题、列表、代码块等内容");
        // 工具已初始化
    }
    
    /**
     * 构造函数
     * 
     * @param configuration Notion配置
     */
    public NotionPageContentTool(NotionConfiguration configuration) {
        this();
        this.notionClient = new NotionClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param notionClient Notion客户端
     */
    public NotionPageContentTool(NotionClient notionClient) {
        this();
        this.notionClient = notionClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行页面内容管理
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (notionClient == null) {
                return new ToolExecuteResult("Notion客户端未初始化，请先配置Notion token", true);
            }
            
            log.info("执行Notion页面内容管理，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String pageId = input.getString("page_id");
            JSONArray children = input.getJSONArray("children");
            
            // 验证必需参数
            if (StringUtils.isBlank(pageId)) {
                return new ToolExecuteResult("页面ID不能为空", true);
            }
            
            if (children == null || children.isEmpty()) {
                return new ToolExecuteResult("内容块不能为空", true);
            }
            
            // 验证内容块格式
            for (int i = 0; i < children.size(); i++) {
                JSONObject block = children.getJSONObject(i);
                if (block == null || !block.containsKey("type")) {
                    return new ToolExecuteResult(String.format("第 %d 个内容块缺少type属性", i + 1), true);
                }
            }
            
            // 执行内容添加
            NotionBlocksResult result = notionClient.appendBlocks(pageId, children);
            
            if (result == null) {
                return new ToolExecuteResult("添加失败，未返回结果", true);
            }
            
            // 构建返回结果
            JSONObject response = new JSONObject();
            response.put("type", "page_content_result");
            response.put("page_id", pageId);
            response.put("results", result.getResults());
            response.put("blocks_added", result.getResults() != null ? result.getResults().size() : 0);
            
            String output = JSON.toJSONString(response);
            log.info("Notion页面内容管理完成，返回结果: {}", output);
            
            return new ToolExecuteResult(output, false);
            
        } catch (NotionException e) {
            log.error("Notion页面内容管理失败", e);
            return new ToolExecuteResult("内容管理失败: " + e.getMessage(), true);
        } catch (Exception e) {
            log.error("内容管理过程中发生未知错误", e);
            return new ToolExecuteResult("内容管理过程中发生未知错误: " + e.getMessage(), true);
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
