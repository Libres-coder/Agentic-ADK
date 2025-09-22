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
package com.alibaba.langengine.googledrive.model;

import lombok.Data;

import java.util.List;

/**
 * Google Drive 文件模型
 * 
 * @author AIDC-AI
 */
@Data
public class GoogleDriveFile {
    
    /**
     * 文件ID
     */
    private String id;
    
    /**
     * 文件名
     */
    private String name;
    
    /**
     * MIME类型
     */
    private String mimeType;
    
    /**
     * 文件大小（字节）
     */
    private Long size;
    
    /**
     * 创建时间
     */
    private Long createdTime;
    
    /**
     * 修改时间
     */
    private Long modifiedTime;
    
    /**
     * 父文件夹ID列表
     */
    private List<String> parents;
    
    /**
     * Web查看链接
     */
    private String webViewLink;
    
    /**
     * Web内容链接
     */
    private String webContentLink;
    
    /**
     * 是否为文件夹
     * 
     * @return 是否为文件夹
     */
    public boolean isFolder() {
        return "application/vnd.google-apps.folder".equals(mimeType);
    }
    
    /**
     * 是否为Google文档
     * 
     * @return 是否为Google文档
     */
    public boolean isGoogleDoc() {
        return mimeType != null && mimeType.startsWith("application/vnd.google-apps.");
    }
    
    /**
     * 获取文件扩展名
     * 
     * @return 文件扩展名
     */
    public String getFileExtension() {
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * 获取格式化的文件大小
     * 
     * @return 格式化的文件大小
     */
    public String getFormattedSize() {
        if (size == null || size == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }
}
