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
package com.alibaba.langengine.onedrive;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.onedrive.tools.OneDriveSearchTool;
import com.alibaba.langengine.onedrive.tools.OneDriveUploadTool;
import com.alibaba.langengine.onedrive.tools.OneDriveDownloadTool;
import com.alibaba.langengine.onedrive.tools.OneDriveFileManageTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OneDrive 工具测试
 * 
 * @author AIDC-AI
 */
@ExtendWith(MockitoExtension.class)
public class OneDriveToolsTest {
    
    private OneDriveConfiguration testConfig;
    
    @BeforeEach
    void setUp() {
        testConfig = OneDriveConfiguration.createTest();
    }
    
    @Test
    void testSearchToolCreation() {
        OneDriveSearchTool searchTool = new OneDriveSearchTool(testConfig);
        
        assertNotNull(searchTool);
        assertEquals("onedrive_search", searchTool.getName());
        assertEquals("searchOneDrive", searchTool.getFunctionName());
        assertEquals("OneDrive搜索", searchTool.getHumanName());
    }
    
    @Test
    void testUploadToolCreation() {
        OneDriveUploadTool uploadTool = new OneDriveUploadTool(testConfig);
        
        assertNotNull(uploadTool);
        assertEquals("onedrive_upload", uploadTool.getName());
        assertEquals("uploadToOneDrive", uploadTool.getFunctionName());
        assertEquals("OneDrive文件上传", uploadTool.getHumanName());
    }
    
    @Test
    void testDownloadToolCreation() {
        OneDriveDownloadTool downloadTool = new OneDriveDownloadTool(testConfig);
        
        assertNotNull(downloadTool);
        assertEquals("onedrive_download", downloadTool.getName());
        assertEquals("downloadFromOneDrive", downloadTool.getFunctionName());
        assertEquals("OneDrive文件下载", downloadTool.getHumanName());
    }
    
    @Test
    void testFileManageToolCreation() {
        OneDriveFileManageTool manageTool = new OneDriveFileManageTool(testConfig);
        
        assertNotNull(manageTool);
        assertEquals("onedrive_file_manage", manageTool.getName());
        assertEquals("manageOneDriveFile", manageTool.getFunctionName());
        assertEquals("OneDrive文件管理", manageTool.getHumanName());
    }
    
    @Test
    void testSearchToolWithInvalidInput() {
        OneDriveSearchTool searchTool = new OneDriveSearchTool(testConfig);
        
        Map<String, Object> invalidParams = new HashMap<>();
        // 缺少必需的query参数
        
        ToolExecuteResult result = searchTool.execute(invalidParams);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("搜索查询参数不能为空"));
    }
    
    @Test
    void testUploadToolWithInvalidInput() {
        OneDriveUploadTool uploadTool = new OneDriveUploadTool(testConfig);
        
        Map<String, Object> invalidParams = new HashMap<>();
        // 缺少必需的filePath参数
        
        ToolExecuteResult result = uploadTool.execute(invalidParams);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("文件路径不能为空"));
    }
    
    @Test
    void testDownloadToolWithInvalidInput() {
        OneDriveDownloadTool downloadTool = new OneDriveDownloadTool(testConfig);
        
        Map<String, Object> invalidParams = new HashMap<>();
        // 缺少必需的fileId和outputPath参数
        
        ToolExecuteResult result = downloadTool.execute(invalidParams);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("文件ID不能为空"));
    }
    
    @Test
    void testFileManageToolWithInvalidInput() {
        OneDriveFileManageTool manageTool = new OneDriveFileManageTool(testConfig);
        
        Map<String, Object> invalidParams = new HashMap<>();
        // 缺少必需的action参数
        
        ToolExecuteResult result = manageTool.execute(invalidParams);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("操作类型不能为空"));
    }
    
    @Test
    void testFileManageToolWithUnsupportedAction() {
        OneDriveFileManageTool manageTool = new OneDriveFileManageTool(testConfig);
        
        Map<String, Object> params = new HashMap<>();
        params.put("action", "unsupported_action");
        
        ToolExecuteResult result = manageTool.execute(params);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("不支持的操作类型"));
    }
}
