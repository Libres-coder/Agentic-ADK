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

import java.io.File;
import java.util.Map;

/**
 * OneDrive 文件上传工具
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class OneDriveUploadTool extends DefaultTool {
    
    private OneDriveClient oneDriveClient;
    
    /**
     * 默认构造函数
     */
    public OneDriveUploadTool() {
        setName("onedrive_upload");
        setFunctionName("uploadToOneDrive");
        setHumanName("OneDrive文件上传");
        setDescription("上传文件到OneDrive，支持指定目标文件夹");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"filePath\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"本地文件路径\"\n" +
                "    },\n" +
                "    \"folderId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"目标文件夹ID，不指定则上传到根目录\"\n" +
                "    },\n" +
                "    \"fileName\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"上传后的文件名，不指定则使用原文件名\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"filePath\"]\n" +
                "}");
    }
    
    /**
     * 构造函数
     * 
     * @param configuration OneDrive配置
     */
    public OneDriveUploadTool(OneDriveConfiguration configuration) {
        this();
        this.oneDriveClient = new OneDriveClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param oneDriveClient OneDrive客户端
     */
    public OneDriveUploadTool(OneDriveClient oneDriveClient) {
        this();
        this.oneDriveClient = oneDriveClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行文件上传
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (oneDriveClient == null) {
                return new ToolExecuteResult("OneDrive客户端未初始化，请先配置OneDrive认证信息", true);
            }
            
            log.info("执行OneDrive文件上传，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String filePath = input.getString("filePath");
            String folderId = input.getString("folderId");
            String fileName = input.getString("fileName");
            
            // 验证输入参数
            if (StringUtils.isBlank(filePath)) {
                return new ToolExecuteResult("文件路径不能为空", true);
            }
            
            // 检查文件是否存在
            File file = new File(filePath);
            if (!file.exists()) {
                return new ToolExecuteResult("文件不存在: " + filePath, true);
            }
            
            if (!file.isFile()) {
                return new ToolExecuteResult("路径不是文件: " + filePath, true);
            }
            
            // 执行上传
            OneDriveFile uploadedFile = oneDriveClient.uploadFile(filePath, folderId);
            
            if (uploadedFile == null) {
                return new ToolExecuteResult("文件上传失败", true);
            }
            
            // 如果需要重命名
            if (StringUtils.isNotBlank(fileName) && !fileName.equals(uploadedFile.getName())) {
                // 这里可以实现重命名逻辑
                log.info("文件上传成功，但重命名功能暂未实现");
            }
            
            // 构建返回结果
            JSONObject response = new JSONObject();
            response.put("type", "upload_result");
            response.put("success", true);
            response.put("file", uploadedFile);
            response.put("message", "文件上传成功");
            
            String output = JSON.toJSONString(response);
            log.info("OneDrive文件上传完成: {}", uploadedFile.getName());
            
            return new ToolExecuteResult(output, false);
            
        } catch (OneDriveException e) {
            log.error("OneDrive文件上传失败", e);
            return new ToolExecuteResult("文件上传失败: " + e.getMessage(), true);
        } catch (Exception e) {
            log.error("文件上传过程中发生未知错误", e);
            return new ToolExecuteResult("文件上传过程中发生未知错误: " + e.getMessage(), true);
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
