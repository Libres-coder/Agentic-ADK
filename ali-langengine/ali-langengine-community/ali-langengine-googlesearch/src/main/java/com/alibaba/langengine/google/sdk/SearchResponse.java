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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Google搜索响应类
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SearchResponse {
    
    /**
     * 搜索查询词
     */
    private String query;
    
    /**
     * 搜索结果列表
     */
    private List<SearchResult> results;
    
    /**
     * 搜索结果总数
     */
    private Long totalResults;
    
    /**
     * 当前页结果数量
     */
    private Integer currentResultCount;
    
    /**
     * 搜索类型
     */
    private String searchType;
    
    /**
     * 搜索语言
     */
    private String language;
    
    /**
     * 搜索国家/地区
     */
    private String country;
    
    /**
     * 搜索时间
     */
    private LocalDateTime searchTime;
    
    /**
     * 搜索耗时（毫秒）
     */
    private Long searchDuration;
    
    /**
     * 是否有更多结果
     */
    private Boolean hasMoreResults;
    
    /**
     * 下一页令牌
     */
    private String nextPageToken;
    
    /**
     * 搜索建议
     */
    private List<String> suggestions;
    
    /**
     * 相关搜索
     */
    private List<String> relatedQueries;
    
    /**
     * 搜索统计信息
     */
    private Map<String, Object> statistics;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 响应状态码
     */
    private Integer statusCode;
    
    /**
     * 扩展信息
     */
    private Map<String, Object> metadata;
    
    /**
     * 构造函数
     */
    public SearchResponse(String query) {
        this.query = query;
        this.results = new ArrayList<>();
        this.searchTime = LocalDateTime.now();
        this.success = true;
        this.statusCode = 200;
    }
    
    /**
     * 添加搜索结果
     */
    public SearchResponse addResult(SearchResult result) {
        if (results == null) {
            results = new ArrayList<>();
        }
        if (result != null && result.isValid()) {
            results.add(result);
            updateCurrentResultCount();
        }
        return this;
    }
    
    /**
     * 添加多个搜索结果
     */
    public SearchResponse addResults(List<SearchResult> resultList) {
        if (results == null) {
            results = new ArrayList<>();
        }
        if (resultList != null) {
            for (SearchResult result : resultList) {
                if (result != null && result.isValid()) {
                    results.add(result);
                }
            }
            updateCurrentResultCount();
        }
        return this;
    }
    
    /**
     * 更新当前结果数量
     */
    private void updateCurrentResultCount() {
        this.currentResultCount = results != null ? results.size() : 0;
    }
    
    /**
     * 获取有效结果数量
     */
    public int getValidResultCount() {
        if (results == null) {
            return 0;
        }
        return (int) results.stream().filter(SearchResult::isValid).count();
    }
    
    /**
     * 检查是否有结果
     */
    public boolean hasResults() {
        return getValidResultCount() > 0;
    }
    
    /**
     * 获取第一个结果
     */
    public SearchResult getFirstResult() {
        if (hasResults()) {
            return results.stream().filter(SearchResult::isValid).findFirst().orElse(null);
        }
        return null;
    }
    
    /**
     * 获取最后一个结果
     */
    public SearchResult getLastResult() {
        if (hasResults()) {
            List<SearchResult> validResults = results.stream().filter(SearchResult::isValid).toList();
            return validResults.get(validResults.size() - 1);
        }
        return null;
    }
    
    /**
     * 设置错误状态
     */
    public SearchResponse setError(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
        this.statusCode = 500;
        return this;
    }
    
    /**
     * 设置错误状态（带状态码）
     */
    public SearchResponse setError(String errorMessage, Integer statusCode) {
        this.errorMessage = errorMessage;
        this.success = false;
        this.statusCode = statusCode != null ? statusCode : 500;
        return this;
    }
} 