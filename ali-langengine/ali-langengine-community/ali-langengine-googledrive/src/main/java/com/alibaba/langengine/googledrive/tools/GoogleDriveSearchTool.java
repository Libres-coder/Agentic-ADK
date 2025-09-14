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
package com.alibaba.langengine.googledrive.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.googledrive.GoogleDriveConfiguration;
import com.alibaba.langengine.googledrive.client.GoogleDriveClient;
import com.alibaba.langengine.googledrive.exception.GoogleDriveException;
import com.alibaba.langengine.googledrive.model.GoogleDriveSearchResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Google Drive 搜索工具
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class GoogleDriveSearchTool extends DefaultTool {
    
    private GoogleDriveClient googleDriveClient;
    
    /**
     * 默认构造函数
     */
    public GoogleDriveSearchTool() {
        setName("google_drive_search");
        setFunctionName("searchGoogleDrive");
        setHumanName("Google Drive搜索");
        setDescription("搜索Google Drive中的文件和文件夹，支持关键词搜索、类型过滤和分页");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"搜索查询，支持文件名、内容搜索\"\n" +
                "    },\n" +
                "    \"mimeType\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"MIME类型过滤，如'image/*'、'application/pdf'等\"\n" +
                "    },\n" +
                "    \"folderId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"在指定文件夹中搜索\"\n" +
                "    },\n" +
                "    \"pageSize\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"每页结果数量，默认10\"\n" +
                "    },\n" +
                "    \"pageToken\": {\n" +
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
     * @param configuration Google Drive配置
     */
    public GoogleDriveSearchTool(GoogleDriveConfiguration configuration) {
        this();
        this.googleDriveClient = new GoogleDriveClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param googleDriveClient Google Drive客户端
     */
    public GoogleDriveSearchTool(GoogleDriveClient googleDriveClient) {
        this();
        this.googleDriveClient = googleDriveClient;
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
            if (googleDriveClient == null) {
                return new ToolExecuteResult("Google Drive客户端未初始化，请先配置Google Drive认证信息", true);
            }
            
            log.info("执行Google Drive搜索，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String query = input.getString("query");
            String mimeType = input.getString("mimeType");
            String folderId = input.getString("folderId");
            Integer pageSize = input.getInteger("pageSize");
            String pageToken = input.getString("pageToken");
            
            // 验证输入参数
            if (StringUtils.isBlank(query)) {
                return new ToolExecuteResult("搜索查询参数不能为空", true);
            }
            
            // 构建搜索查询
            StringBuilder searchQuery = new StringBuilder();
            searchQuery.append("name contains '").append(query).append("'");
            
            if (StringUtils.isNotBlank(mimeType)) {
                searchQuery.append(" and mimeType='").append(mimeType).append("'");
            }
            
            if (StringUtils.isNotBlank(folderId)) {
                searchQuery.append(" and '").append(folderId).append("' in parents");
            }
            
            // 执行搜索
            GoogleDriveSearchResult result = googleDriveClient.searchFiles(
                    searchQuery.toString(), 
                    pageSize != null ? pageSize : 10, 
                    pageToken
            );
            
            if (result == null) {
                return new ToolExecuteResult("搜索失败，未返回结果", true);
            }
            
            // 构建返回结果
            JSONObject response = new JSONObject();
            response.put("type", "search_result");
            response.put("files", result.getFiles());
            response.put("nextPageToken", result.getNextPageToken());
            response.put("hasMore", result.hasMore());
            response.put("totalFiles", result.getTotalFiles());
            response.put("fileCount", result.getFileCount());
            
            String output = JSON.toJSONString(response);
            log.info("Google Drive搜索完成，找到{}个文件", result.getFileCount());
            
            return new ToolExecuteResult(output, false);
            
        } catch (GoogleDriveException e) {
            log.error("Google Drive搜索失败", e);
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
