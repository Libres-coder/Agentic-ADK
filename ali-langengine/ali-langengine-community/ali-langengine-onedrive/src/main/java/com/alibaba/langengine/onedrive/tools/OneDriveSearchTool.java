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
package com.alibaba.langengine.onedrive.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.onedrive.OneDriveConfiguration;
import com.alibaba.langengine.onedrive.client.OneDriveClient;
import com.alibaba.langengine.onedrive.exception.OneDriveException;
import com.alibaba.langengine.onedrive.model.OneDriveSearchResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * OneDrive 搜索工具
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class OneDriveSearchTool extends DefaultTool {
    
    private OneDriveClient oneDriveClient;
    
    /**
     * 默认构造函数
     */
    public OneDriveSearchTool() {
        setName("onedrive_search");
        setFunctionName("searchOneDrive");
        setHumanName("OneDrive搜索");
        setDescription("搜索OneDrive中的文件和文件夹，支持关键词搜索、类型过滤和分页");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"搜索查询，支持文件名、内容搜索\"\n" +
                "    },\n" +
                "    \"fileType\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"文件类型过滤，如'image'、'document'等\"\n" +
                "    },\n" +
                "    \"folderId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"在指定文件夹中搜索\"\n" +
                "    },\n" +
                "    \"pageSize\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"每页结果数量，默认10\"\n" +
                "    },\n" +
                "    \"skipToken\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"分页令牌\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"query\"]\n" +
                "}");
    }
    
    /**
     * 构造函数
     * 
     * @param configuration OneDrive配置
     */
    public OneDriveSearchTool(OneDriveConfiguration configuration) {
        this();
        this.oneDriveClient = new OneDriveClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param oneDriveClient OneDrive客户端
     */
    public OneDriveSearchTool(OneDriveClient oneDriveClient) {
        this();
        this.oneDriveClient = oneDriveClient;
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
            if (oneDriveClient == null) {
                return new ToolExecuteResult("OneDrive客户端未初始化，请先配置OneDrive认证信息", true);
            }
            
            log.info("执行OneDrive搜索，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String query = input.getString("query");
            String fileType = input.getString("fileType");
            String folderId = input.getString("folderId");
            Integer pageSize = input.getInteger("pageSize");
            String skipToken = input.getString("skipToken");
            
            // 验证输入参数
            if (StringUtils.isBlank(query)) {
                return new ToolExecuteResult("搜索查询参数不能为空", true);
            }
            
            // 构建搜索查询
            StringBuilder searchQuery = new StringBuilder();
            searchQuery.append(query);
            
            if (StringUtils.isNotBlank(fileType)) {
                searchQuery.append(" kind:").append(fileType);
            }
            
            // 执行搜索
            OneDriveSearchResult result = oneDriveClient.searchFiles(
                    searchQuery.toString(), 
                    pageSize != null ? pageSize : 10, 
                    skipToken
            );
            
            if (result == null) {
                return new ToolExecuteResult("搜索失败，未返回结果", true);
            }
            
            // 构建返回结果
            JSONObject response = new JSONObject();
            response.put("type", "search_result");
            response.put("files", result.getFiles());
            response.put("nextLink", result.getNextLink());
            response.put("hasMore", result.hasMore());
            response.put("totalFiles", result.getTotalFiles());
            response.put("fileCount", result.getFileCount());
            
            String output = JSON.toJSONString(response);
            log.info("OneDrive搜索完成，找到{}个文件", result.getFileCount());
            
            return new ToolExecuteResult(output, false);
            
        } catch (OneDriveException e) {
            log.error("OneDrive搜索失败", e);
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
