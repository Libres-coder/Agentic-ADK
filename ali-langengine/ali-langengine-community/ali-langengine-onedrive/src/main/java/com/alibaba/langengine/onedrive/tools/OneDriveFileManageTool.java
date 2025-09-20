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
import com.alibaba.langengine.onedrive.model.OneDriveFile;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * OneDrive 文件管理工具
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class OneDriveFileManageTool extends DefaultTool {
    
    private OneDriveClient oneDriveClient;
    
    /**
     * 默认构造函数
     */
    public OneDriveFileManageTool() {
        setName("onedrive_file_manage");
        setFunctionName("manageOneDriveFile");
        setHumanName("OneDrive文件管理");
        setDescription("管理OneDrive文件，包括创建文件夹、删除文件、移动文件等操作");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"action\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"enum\": [\"create_folder\", \"delete_file\", \"move_file\", \"get_file_info\"],\n" +
                "      \"description\": \"操作类型\"\n" +
                "    },\n" +
                "    \"fileId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"文件ID（删除、移动、获取信息时需要）\"\n" +
                "    },\n" +
                "    \"folderName\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"文件夹名称（创建文件夹时需要）\"\n" +
                "    },\n" +
                "    \"parentFolderId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"父文件夹ID（创建文件夹、移动文件时需要）\"\n" +
                "    },\n" +
                "    \"newParentId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"新的父文件夹ID（移动文件时需要）\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"action\"]\n" +
                "}");
    }
    
    /**
     * 构造函数
     * 
     * @param configuration OneDrive配置
     */
    public OneDriveFileManageTool(OneDriveConfiguration configuration) {
        this();
        this.oneDriveClient = new OneDriveClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param oneDriveClient OneDrive客户端
     */
    public OneDriveFileManageTool(OneDriveClient oneDriveClient) {
        this();
        this.oneDriveClient = oneDriveClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行文件管理操作
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (oneDriveClient == null) {
                return new ToolExecuteResult("OneDrive客户端未初始化，请先配置OneDrive认证信息", true);
            }
            
            log.info("执行OneDrive文件管理操作，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String action = input.getString("action");
            
            if (StringUtils.isBlank(action)) {
                return new ToolExecuteResult("操作类型不能为空", true);
            }
            
            JSONObject response = new JSONObject();
            response.put("type", "file_manage_result");
            
            switch (action.toLowerCase()) {
                case "create_folder":
                    return createFolder(input, response);
                case "delete_file":
                    return deleteFile(input, response);
                case "move_file":
                    return moveFile(input, response);
                case "get_file_info":
                    return getFileInfo(input, response);
                default:
                    return new ToolExecuteResult("不支持的操作类型: " + action, true);
            }
            
        } catch (OneDriveException e) {
            log.error("OneDrive文件管理操作失败", e);
            return new ToolExecuteResult("操作失败: " + e.getMessage(), true);
        } catch (Exception e) {
            log.error("文件管理操作过程中发生未知错误", e);
            return new ToolExecuteResult("操作过程中发生未知错误: " + e.getMessage(), true);
        }
    }
    
    /**
     * 创建文件夹
     */
    private ToolExecuteResult createFolder(JSONObject input, JSONObject response) {
        String folderName = input.getString("folderName");
        String parentFolderId = input.getString("parentFolderId");
        
        if (StringUtils.isBlank(folderName)) {
            return new ToolExecuteResult("文件夹名称不能为空", true);
        }
        
        OneDriveFile folder = oneDriveClient.createFolder(folderName, parentFolderId);
        
        response.put("action", "create_folder");
        response.put("success", true);
        response.put("folder", folder);
        response.put("message", "文件夹创建成功");
        
        return new ToolExecuteResult(JSON.toJSONString(response), false);
    }
    
    /**
     * 删除文件
     */
    private ToolExecuteResult deleteFile(JSONObject input, JSONObject response) {
        String fileId = input.getString("fileId");
        
        if (StringUtils.isBlank(fileId)) {
            return new ToolExecuteResult("文件ID不能为空", true);
        }
        
        boolean success = oneDriveClient.deleteFile(fileId);
        
        response.put("action", "delete_file");
        response.put("success", success);
        response.put("fileId", fileId);
        response.put("message", success ? "文件删除成功" : "文件删除失败");
        
        return new ToolExecuteResult(JSON.toJSONString(response), false);
    }
    
    /**
     * 移动文件
     */
    private ToolExecuteResult moveFile(JSONObject input, JSONObject response) {
        String fileId = input.getString("fileId");
        String newParentId = input.getString("newParentId");
        
        if (StringUtils.isBlank(fileId)) {
            return new ToolExecuteResult("文件ID不能为空", true);
        }
        
        if (StringUtils.isBlank(newParentId)) {
            return new ToolExecuteResult("新的父文件夹ID不能为空", true);
        }
        
        OneDriveFile movedFile = oneDriveClient.moveFile(fileId, newParentId);
        
        response.put("action", "move_file");
        response.put("success", true);
        response.put("file", movedFile);
        response.put("message", "文件移动成功");
        
        return new ToolExecuteResult(JSON.toJSONString(response), false);
    }
    
    /**
     * 获取文件信息
     */
    private ToolExecuteResult getFileInfo(JSONObject input, JSONObject response) {
        String fileId = input.getString("fileId");
        
        if (StringUtils.isBlank(fileId)) {
            return new ToolExecuteResult("文件ID不能为空", true);
        }
        
        OneDriveFile file = oneDriveClient.getFileInfo(fileId);
        
        response.put("action", "get_file_info");
        response.put("success", true);
        response.put("file", file);
        response.put("message", "文件信息获取成功");
        
        return new ToolExecuteResult(JSON.toJSONString(response), false);
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
