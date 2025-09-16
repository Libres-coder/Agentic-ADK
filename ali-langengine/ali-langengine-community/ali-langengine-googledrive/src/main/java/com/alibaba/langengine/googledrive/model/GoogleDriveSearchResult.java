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
 * Google Drive 搜索结果模型
 * 
 * @author AIDC-AI
 */
@Data
public class GoogleDriveSearchResult {
    
    /**
     * 文件列表
     */
    private List<GoogleDriveFile> files;
    
    /**
     * 下一页令牌
     */
    private String nextPageToken;
    
    /**
     * 总文件数
     */
    private Integer totalFiles;
    
    /**
     * 是否有更多结果
     * 
     * @return 是否有更多结果
     */
    public boolean hasMore() {
        return nextPageToken != null && !nextPageToken.isEmpty();
    }
    
    /**
     * 获取文件数量
     * 
     * @return 文件数量
     */
    public int getFileCount() {
        return files != null ? files.size() : 0;
    }
}
