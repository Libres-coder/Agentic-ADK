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
package com.alibaba.langengine.googledrive;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.googledrive.tools.GoogleDriveSearchTool;
import com.alibaba.langengine.googledrive.tools.GoogleDriveUploadTool;
import com.alibaba.langengine.googledrive.tools.GoogleDriveDownloadTool;
import com.alibaba.langengine.googledrive.tools.GoogleDriveFileManageTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Google Drive 工具测试
 * 
 * @author AIDC-AI
 */
@ExtendWith(MockitoExtension.class)
public class GoogleDriveToolsTest {
    
    private GoogleDriveConfiguration testConfig;
    
    @BeforeEach
    void setUp() {
        testConfig = GoogleDriveConfiguration.createTest();
    }
    
    @Test
    void testSearchToolCreation() {
        GoogleDriveSearchTool searchTool = new GoogleDriveSearchTool(testConfig);
        
        assertNotNull(searchTool);
        assertEquals("google_drive_search", searchTool.getName());
        assertEquals("searchGoogleDrive", searchTool.getFunctionName());
        assertEquals("Google Drive搜索", searchTool.getHumanName());
    }
    
    @Test
    void testUploadToolCreation() {
        GoogleDriveUploadTool uploadTool = new GoogleDriveUploadTool(testConfig);
        
        assertNotNull(uploadTool);
        assertEquals("google_drive_upload", uploadTool.getName());
        assertEquals("uploadToGoogleDrive", uploadTool.getFunctionName());
        assertEquals("Google Drive文件上传", uploadTool.getHumanName());
    }
    
    @Test
    void testDownloadToolCreation() {
        GoogleDriveDownloadTool downloadTool = new GoogleDriveDownloadTool(testConfig);
        
        assertNotNull(downloadTool);
        assertEquals("google_drive_download", downloadTool.getName());
        assertEquals("downloadFromGoogleDrive", downloadTool.getFunctionName());
        assertEquals("Google Drive文件下载", downloadTool.getHumanName());
    }
    
    @Test
    void testFileManageToolCreation() {
        GoogleDriveFileManageTool manageTool = new GoogleDriveFileManageTool(testConfig);
        
        assertNotNull(manageTool);
        assertEquals("google_drive_file_manage", manageTool.getName());
        assertEquals("manageGoogleDriveFile", manageTool.getFunctionName());
        assertEquals("Google Drive文件管理", manageTool.getHumanName());
    }
    
    @Test
    void testSearchToolWithInvalidInput() {
        GoogleDriveSearchTool searchTool = new GoogleDriveSearchTool(testConfig);
        
        Map<String, Object> invalidParams = new HashMap<>();
        // 缺少必需的query参数
        
        ToolExecuteResult result = searchTool.execute(invalidParams);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("搜索查询参数不能为空"));
    }
    
    @Test
    void testUploadToolWithInvalidInput() {
        GoogleDriveUploadTool uploadTool = new GoogleDriveUploadTool(testConfig);
        
        Map<String, Object> invalidParams = new HashMap<>();
        // 缺少必需的filePath参数
        
        ToolExecuteResult result = uploadTool.execute(invalidParams);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("文件路径不能为空"));
    }
    
    @Test
    void testDownloadToolWithInvalidInput() {
        GoogleDriveDownloadTool downloadTool = new GoogleDriveDownloadTool(testConfig);
        
        Map<String, Object> invalidParams = new HashMap<>();
        // 缺少必需的fileId和outputPath参数
        
        ToolExecuteResult result = downloadTool.execute(invalidParams);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("文件ID不能为空"));
    }
    
    @Test
    void testFileManageToolWithInvalidInput() {
        GoogleDriveFileManageTool manageTool = new GoogleDriveFileManageTool(testConfig);
        
        Map<String, Object> invalidParams = new HashMap<>();
        // 缺少必需的action参数
        
        ToolExecuteResult result = manageTool.execute(invalidParams);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("操作类型不能为空"));
    }
    
    @Test
    void testFileManageToolWithUnsupportedAction() {
        GoogleDriveFileManageTool manageTool = new GoogleDriveFileManageTool(testConfig);
        
        Map<String, Object> params = new HashMap<>();
        params.put("action", "unsupported_action");
        
        ToolExecuteResult result = manageTool.execute(params);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("不支持的操作类型"));
    }
}
