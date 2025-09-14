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
 * OneDrive 文件下载工具
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class OneDriveDownloadTool extends DefaultTool {
    
    private OneDriveClient oneDriveClient;
    
    /**
     * 默认构造函数
     */
    public OneDriveDownloadTool() {
        setName("onedrive_download");
        setFunctionName("downloadFromOneDrive");
        setHumanName("OneDrive文件下载");
        setDescription("从OneDrive下载文件到本地");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"fileId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"OneDrive文件ID\"\n" +
                "    },\n" +
                "    \"outputPath\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"本地保存路径\"\n" +
                "    },\n" +
                "    \"fileName\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"下载后的文件名，不指定则使用原文件名\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"fileId\", \"outputPath\"]\n" +
                "}");
    }
    
    /**
     * 构造函数
     * 
     * @param configuration OneDrive配置
     */
    public OneDriveDownloadTool(OneDriveConfiguration configuration) {
        this();
        this.oneDriveClient = new OneDriveClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param oneDriveClient OneDrive客户端
     */
    public OneDriveDownloadTool(OneDriveClient oneDriveClient) {
        this();
        this.oneDriveClient = oneDriveClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行文件下载
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (oneDriveClient == null) {
                return new ToolExecuteResult("OneDrive客户端未初始化，请先配置OneDrive认证信息", true);
            }
            
            log.info("执行OneDrive文件下载，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String fileId = input.getString("fileId");
            String outputPath = input.getString("outputPath");
            String fileName = input.getString("fileName");
            
            // 验证输入参数
            if (StringUtils.isBlank(fileId)) {
                return new ToolExecuteResult("文件ID不能为空", true);
            }
            
            if (StringUtils.isBlank(outputPath)) {
                return new ToolExecuteResult("输出路径不能为空", true);
            }
            
            // 获取文件信息
            OneDriveFile fileInfo = oneDriveClient.getFileInfo(fileId);
            if (fileInfo == null) {
                return new ToolExecuteResult("文件不存在或无法访问: " + fileId, true);
            }
            
            // 如果是文件夹，不能下载
            if (fileInfo.isFolder()) {
                return new ToolExecuteResult("不能下载文件夹: " + fileInfo.getName(), true);
            }
            
            // 确定最终的文件名
            String finalFileName = StringUtils.isNotBlank(fileName) ? fileName : fileInfo.getName();
            String finalOutputPath = outputPath.endsWith("/") ? outputPath + finalFileName : outputPath;
            
            // 执行下载
            String downloadedPath = oneDriveClient.downloadFile(fileId, finalOutputPath);
            
            if (StringUtils.isBlank(downloadedPath)) {
                return new ToolExecuteResult("文件下载失败", true);
            }
            
            // 构建返回结果
            JSONObject response = new JSONObject();
            response.put("type", "download_result");
            response.put("success", true);
            response.put("fileId", fileId);
            response.put("fileName", fileInfo.getName());
            response.put("downloadedPath", downloadedPath);
            response.put("fileSize", fileInfo.getSize());
            response.put("mimeType", fileInfo.getMimeType());
            response.put("message", "文件下载成功");
            
            String output = JSON.toJSONString(response);
            log.info("OneDrive文件下载完成: {} -> {}", fileInfo.getName(), downloadedPath);
            
            return new ToolExecuteResult(output, false);
            
        } catch (OneDriveException e) {
            log.error("OneDrive文件下载失败", e);
            return new ToolExecuteResult("文件下载失败: " + e.getMessage(), true);
        } catch (Exception e) {
            log.error("文件下载过程中发生未知错误", e);
            return new ToolExecuteResult("文件下载过程中发生未知错误: " + e.getMessage(), true);
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
