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
package com.alibaba.langengine.google.sdk;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Google搜索请求类
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SearchRequest {
    
    /**
     * 搜索查询词（必填）
     */
    private String query;
    
    /**
     * 搜索结果数量
     */
    private Integer count;
    
    /**
     * 搜索类型：web, images, news, videos
     */
    private String searchType;
    
    /**
     * 语言设置
     */
    private String language;
    
    /**
     * 国家/地区设置
     */
    private String country;
    
    /**
     * 排序方式：relevance, date, rating
     */
    private String sortBy;
    
    /**
     * 时间范围过滤
     */
    private String timeRange;
    
    /**
     * 安全搜索设置
     */
    private Boolean safeSearch;
    
    /**
     * 自定义搜索参数
     */
    private List<String> customParams;
    
    /**
     * 是否包含成人内容
     */
    private Boolean includeAdultContent;
    
    /**
     * 搜索区域限制
     */
    private String region;
    
    /**
     * 验证请求参数
     */
    public boolean isValid() {
        return query != null && !query.trim().isEmpty();
    }
    
    /**
     * 获取有效的搜索结果数量
     */
    public int getValidCount() {
        if (count == null || count < GoogleConstant.MIN_RESULT_COUNT) {
            return GoogleConstant.DEFAULT_RESULT_COUNT;
        }
        return Math.min(count, GoogleConstant.MAX_RESULT_COUNT);
    }
    
    /**
     * 获取有效的搜索类型
     */
    public String getValidSearchType() {
        if (searchType == null || searchType.trim().isEmpty()) {
            return GoogleConstant.SEARCH_TYPE_WEB;
        }
        return searchType.toLowerCase();
    }
    
    /**
     * 获取有效的语言设置
     */
    public String getValidLanguage() {
        if (language == null || language.trim().isEmpty()) {
            return "en";
        }
        return language.toLowerCase();
    }
} 