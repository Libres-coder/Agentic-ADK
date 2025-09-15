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
package com.alibaba.langengine.googlescholar.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

import static com.alibaba.langengine.googlescholar.GoogleScholarConfiguration.*;

/**
 * Google Scholar 搜索请求模型
 * 封装搜索参数和配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleScholarSearchRequest {
    
    /**
     * 搜索查询词
     */
    private String query;
    
    /**
     * 结果数量
     */
    private Integer count;
    
    /**
     * 排序方式
     */
    private String sortBy;
    
    /**
     * 时间范围
     */
    private String timeRange;
    
    /**
     * 文档类型
     */
    private String documentType;
    
    /**
     * 作者过滤
     */
    private String author;
    
    /**
     * 期刊过滤
     */
    private String journal;
    
    /**
     * 语言
     */
    private String language;
    
    /**
     * 国家/地区
     */
    private String country;
    
    /**
     * 是否包含引用
     */
    private Boolean includeCitations;
    
    /**
     * 是否包含摘要
     */
    private Boolean includeAbstract;
    
    /**
     * 是否包含PDF链接
     */
    private Boolean includePdfLinks;
    
    /**
     * 是否包含相关文章
     */
    private Boolean includeRelated;
    
    /**
     * 是否包含版本信息
     */
    private Boolean includeVersions;
    
    /**
     * 自定义参数列表
     */
    private List<String> customParams;
    
    /**
     * 构造函数
     */
    public GoogleScholarSearchRequest(String query) {
        this.query = query;
        this.count = getDefaultResultCount();
        this.sortBy = SUPPORTED_SORT_OPTIONS[0];
        this.timeRange = SUPPORTED_TIME_RANGES[0];
        this.documentType = SUPPORTED_DOCUMENT_TYPES[0];
        this.language = getDefaultLanguage();
        this.country = getDefaultCountry();
        this.includeCitations = true;
        this.includeAbstract = true;
        this.includePdfLinks = true;
        this.includeRelated = false;
        this.includeVersions = false;
        this.customParams = new ArrayList<>();
    }
    
    /**
     * 验证请求是否有效
     */
    public boolean isValid() {
        return query != null && !query.trim().isEmpty();
    }
    
    /**
     * 获取有效的查询词
     */
    public String getValidQuery() {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return query.trim();
    }
    
    /**
     * 获取有效的结果数量
     */
    public int getValidCount() {
        return validateResultCount(count != null ? count : getDefaultResultCount());
    }
    
    /**
     * 获取有效的排序方式
     */
    public String getValidSortBy() {
        return validateSortOption(sortBy);
    }
    
    /**
     * 获取有效的时间范围
     */
    public String getValidTimeRange() {
        return validateTimeRange(timeRange);
    }
    
    /**
     * 获取有效的文档类型
     */
    public String getValidDocumentType() {
        return validateDocumentType(documentType);
    }
    
    /**
     * 获取有效的语言
     */
    public String getValidLanguage() {
        return language != null && !language.trim().isEmpty() ? language : getDefaultLanguage();
    }
    
    /**
     * 获取有效的国家/地区
     */
    public String getValidCountry() {
        return country != null && !country.trim().isEmpty() ? country : getDefaultCountry();
    }
    
    /**
     * 设置查询词
     */
    public GoogleScholarSearchRequest setQuery(String query) {
        this.query = query;
        return this;
    }
    
    /**
     * 设置结果数量
     */
    public GoogleScholarSearchRequest setCount(Integer count) {
        this.count = count;
        return this;
    }
    
    /**
     * 设置排序方式
     */
    public GoogleScholarSearchRequest setSortBy(String sortBy) {
        this.sortBy = sortBy;
        return this;
    }
    
    /**
     * 设置时间范围
     */
    public GoogleScholarSearchRequest setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        return this;
    }
    
    /**
     * 设置文档类型
     */
    public GoogleScholarSearchRequest setDocumentType(String documentType) {
        this.documentType = documentType;
        return this;
    }
    
    /**
     * 设置作者过滤
     */
    public GoogleScholarSearchRequest setAuthor(String author) {
        this.author = author;
        return this;
    }
    
    /**
     * 设置期刊过滤
     */
    public GoogleScholarSearchRequest setJournal(String journal) {
        this.journal = journal;
        return this;
    }
    
    /**
     * 设置语言
     */
    public GoogleScholarSearchRequest setLanguage(String language) {
        this.language = language;
        return this;
    }
    
    /**
     * 设置国家/地区
     */
    public GoogleScholarSearchRequest setCountry(String country) {
        this.country = country;
        return this;
    }
    
    /**
     * 设置是否包含引用
     */
    public GoogleScholarSearchRequest setIncludeCitations(Boolean includeCitations) {
        this.includeCitations = includeCitations;
        return this;
    }
    
    /**
     * 设置是否包含摘要
     */
    public GoogleScholarSearchRequest setIncludeAbstract(Boolean includeAbstract) {
        this.includeAbstract = includeAbstract;
        return this;
    }
    
    /**
     * 设置是否包含PDF链接
     */
    public GoogleScholarSearchRequest setIncludePdfLinks(Boolean includePdfLinks) {
        this.includePdfLinks = includePdfLinks;
        return this;
    }
    
    /**
     * 设置是否包含相关文章
     */
    public GoogleScholarSearchRequest setIncludeRelated(Boolean includeRelated) {
        this.includeRelated = includeRelated;
        return this;
    }
    
    /**
     * 设置是否包含版本信息
     */
    public GoogleScholarSearchRequest setIncludeVersions(Boolean includeVersions) {
        this.includeVersions = includeVersions;
        return this;
    }
    
    /**
     * 添加自定义参数
     */
    public GoogleScholarSearchRequest addCustomParam(String param) {
        if (customParams == null) {
            customParams = new ArrayList<>();
        }
        if (param != null && !param.trim().isEmpty()) {
            customParams.add(param.trim());
        }
        return this;
    }
    
    /**
     * 构建搜索URL参数
     */
    public String buildSearchUrl() {
        StringBuilder url = new StringBuilder(SCHOLAR_BASE_URL);
        url.append("?q=").append(java.net.URLEncoder.encode(getValidQuery(), java.nio.charset.StandardCharsets.UTF_8));
        
        // 添加排序参数
        String sort = getValidSortBy();
        if (!"relevance".equals(sort)) {
            url.append("&sort=").append(sort);
        }
        
        // 添加时间范围参数
        String time = getValidTimeRange();
        if (!"any".equals(time)) {
            url.append("&as_ylo=").append(getYearFromTimeRange(time));
        }
        
        // 添加文档类型参数
        String docType = getValidDocumentType();
        if (!"any".equals(docType)) {
            url.append("&as_dt=").append(docType);
        }
        
        // 添加作者过滤
        if (author != null && !author.trim().isEmpty()) {
            url.append("&as_sauthors=").append(java.net.URLEncoder.encode(author.trim(), java.nio.charset.StandardCharsets.UTF_8));
        }
        
        // 添加期刊过滤
        if (journal != null && !journal.trim().isEmpty()) {
            url.append("&as_publication=").append(java.net.URLEncoder.encode(journal.trim(), java.nio.charset.StandardCharsets.UTF_8));
        }
        
        // 添加语言参数
        String lang = getValidLanguage();
        if (!"en".equals(lang)) {
            url.append("&hl=").append(lang);
        }
        
        // 添加国家/地区参数
        String countryCode = getValidCountry();
        if (!"us".equals(countryCode)) {
            url.append("&gl=").append(countryCode);
        }
        
        // 添加自定义参数
        if (customParams != null) {
            for (String param : customParams) {
                if (param.contains("=")) {
                    String[] parts = param.split("=", 2);
                    if (parts.length == 2) {
                        url.append("&").append(parts[0]).append("=").append(parts[1]);
                    }
                }
            }
        }
        
        return url.toString();
    }
    
    /**
     * 从时间范围获取年份
     */
    private String getYearFromTimeRange(String timeRange) {
        switch (timeRange) {
            case "y":
                return String.valueOf(java.time.LocalDate.now().getYear() - 1);
            case "2y":
                return String.valueOf(java.time.LocalDate.now().getYear() - 2);
            case "5y":
                return String.valueOf(java.time.LocalDate.now().getYear() - 5);
            case "10y":
                return String.valueOf(java.time.LocalDate.now().getYear() - 10);
            default:
                return "";
        }
    }
    
    /**
     * 获取请求摘要
     */
    public String getRequestSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("搜索查询: ").append(getValidQuery()).append("\n");
        sb.append("结果数量: ").append(getValidCount()).append("\n");
        sb.append("排序方式: ").append(getValidSortBy()).append("\n");
        sb.append("时间范围: ").append(getValidTimeRange()).append("\n");
        sb.append("文档类型: ").append(getValidDocumentType()).append("\n");
        
        if (author != null && !author.trim().isEmpty()) {
            sb.append("作者过滤: ").append(author).append("\n");
        }
        
        if (journal != null && !journal.trim().isEmpty()) {
            sb.append("期刊过滤: ").append(journal).append("\n");
        }
        
        sb.append("语言: ").append(getValidLanguage()).append("\n");
        sb.append("国家/地区: ").append(getValidCountry()).append("\n");
        
        return sb.toString();
    }
}
