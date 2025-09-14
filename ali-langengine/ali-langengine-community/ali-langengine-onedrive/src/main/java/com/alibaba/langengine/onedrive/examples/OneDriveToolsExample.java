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
package com.alibaba.langengine.onedrive.examples;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.onedrive.OneDriveConfiguration;
import com.alibaba.langengine.onedrive.OneDriveToolFactory;
import com.alibaba.langengine.onedrive.tools.OneDriveSearchTool;
import com.alibaba.langengine.onedrive.tools.OneDriveUploadTool;
import com.alibaba.langengine.onedrive.tools.OneDriveDownloadTool;
import com.alibaba.langengine.onedrive.tools.OneDriveFileManageTool;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * OneDrive 工具使用示例
 * 
 * @author AIDC-AI
 */
@Slf4j
public class OneDriveToolsExample {
    
    public static void main(String[] args) {
        // 创建配置
        OneDriveConfiguration config = new OneDriveConfiguration(
            "your_client_id",
            "your_client_secret",
            "your_tenant_id",
            "your_access_token"
        );
        
        // 创建工具工厂
        OneDriveToolFactory factory = new OneDriveToolFactory(config);
        
        // 示例1: 搜索文件
        searchFilesExample(factory);
        
        // 示例2: 上传文件
        uploadFileExample(factory);
        
        // 示例3: 下载文件
        downloadFileExample(factory);
        
        // 示例4: 文件管理
        fileManageExample(factory);
    }
    
    /**
     * 搜索文件示例
     */
    private static void searchFilesExample(OneDriveToolFactory factory) {
        log.info("=== OneDrive 搜索文件示例 ===");
        
        OneDriveSearchTool searchTool = factory.getSearchTool();
        
        // 搜索所有文档文件
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("query", "report");
        searchParams.put("fileType", "document");
        searchParams.put("pageSize", 5);
        
        ToolExecuteResult result = searchTool.execute(searchParams);
        log.info("搜索结果: {}", result.getOutput());
    }
    
    /**
     * 上传文件示例
     */
    private static void uploadFileExample(OneDriveToolFactory factory) {
        log.info("=== OneDrive 上传文件示例 ===");
        
        OneDriveUploadTool uploadTool = factory.getUploadTool();
        
        // 上传文件到根目录
        Map<String, Object> uploadParams = new HashMap<>();
        uploadParams.put("filePath", "/path/to/local/file.pdf");
        uploadParams.put("fileName", "uploaded_file.pdf");
        
        ToolExecuteResult result = uploadTool.execute(uploadParams);
        log.info("上传结果: {}", result.getOutput());
    }
    
    /**
     * 下载文件示例
     */
    private static void downloadFileExample(OneDriveToolFactory factory) {
        log.info("=== OneDrive 下载文件示例 ===");
        
        OneDriveDownloadTool downloadTool = factory.getDownloadTool();
        
        // 下载文件
        Map<String, Object> downloadParams = new HashMap<>();
        downloadParams.put("fileId", "your_file_id_here");
        downloadParams.put("outputPath", "/path/to/download/location/");
        
        ToolExecuteResult result = downloadTool.execute(downloadParams);
        log.info("下载结果: {}", result.getOutput());
    }
    
    /**
     * 文件管理示例
     */
    private static void fileManageExample(OneDriveToolFactory factory) {
        log.info("=== OneDrive 文件管理示例 ===");
        
        OneDriveFileManageTool manageTool = factory.getFileManageTool();
        
        // 创建文件夹
        Map<String, Object> createFolderParams = new HashMap<>();
        createFolderParams.put("action", "create_folder");
        createFolderParams.put("folderName", "新文件夹");
        
        ToolExecuteResult result = manageTool.execute(createFolderParams);
        log.info("创建文件夹结果: {}", result.getOutput());
        
        // 获取文件信息
        Map<String, Object> getInfoParams = new HashMap<>();
        getInfoParams.put("action", "get_file_info");
        getInfoParams.put("fileId", "your_file_id_here");
        
        result = manageTool.execute(getInfoParams);
        log.info("获取文件信息结果: {}", result.getOutput());
    }
}
