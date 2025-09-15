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
package com.alibaba.langengine.googlescholar;

/**
 * Google Scholar 搜索配置类
 * 提供默认配置参数和常量定义
 */
public class GoogleScholarConfiguration {
    
    /**
     * Google Scholar 基础URL
     */
    public static final String SCHOLAR_BASE_URL = "https://scholar.google.com/scholar";
    
    /**
     * 默认用户代理
     */
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    /**
     * 默认超时时间（秒）
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;
    
    /**
     * 默认语言
     */
    public static final String DEFAULT_LANGUAGE = "en";
    
    /**
     * 默认国家/地区
     */
    public static final String DEFAULT_COUNTRY = "us";
    
    /**
     * 默认结果数量
     */
    public static final int DEFAULT_RESULT_COUNT = 10;
    
    /**
     * 最大结果数量
     */
    public static final int MAX_RESULT_COUNT = 100;
    
    /**
     * 最小结果数量
     */
    public static final int MIN_RESULT_COUNT = 1;
    
    /**
     * 支持的排序方式
     */
    public static final String[] SUPPORTED_SORT_OPTIONS = {
        "relevance",    // 相关性
        "date",         // 日期
        "citations"     // 引用次数
    };
    
    /**
     * 支持的时间范围
     */
    public static final String[] SUPPORTED_TIME_RANGES = {
        "any",          // 任何时间
        "y",            // 过去一年
        "2y",           // 过去两年
        "5y",           // 过去五年
        "10y"           // 过去十年
    };
    
    /**
     * 支持的文档类型
     */
    public static final String[] SUPPORTED_DOCUMENT_TYPES = {
        "any",          // 任何类型
        "articles",     // 文章
        "theses",       // 论文
        "books",        // 书籍
        "patents",      // 专利
        "case_law"      // 案例法
    };
    
    /**
     * 获取默认用户代理
     */
    public static String getDefaultUserAgent() {
        return DEFAULT_USER_AGENT;
    }
    
    /**
     * 获取默认超时时间
     */
    public static int getDefaultTimeoutSeconds() {
        return DEFAULT_TIMEOUT_SECONDS;
    }
    
    /**
     * 获取默认语言
     */
    public static String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }
    
    /**
     * 获取默认国家/地区
     */
    public static String getDefaultCountry() {
        return DEFAULT_COUNTRY;
    }
    
    /**
     * 获取默认结果数量
     */
    public static int getDefaultResultCount() {
        return DEFAULT_RESULT_COUNT;
    }
    
    /**
     * 验证结果数量
     */
    public static int validateResultCount(int count) {
        if (count < MIN_RESULT_COUNT) {
            return MIN_RESULT_COUNT;
        }
        if (count > MAX_RESULT_COUNT) {
            return MAX_RESULT_COUNT;
        }
        return count;
    }
    
    /**
     * 验证排序选项
     */
    public static String validateSortOption(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return SUPPORTED_SORT_OPTIONS[0]; // 默认相关性
        }
        
        String lowerSort = sort.toLowerCase().trim();
        for (String option : SUPPORTED_SORT_OPTIONS) {
            if (option.equals(lowerSort)) {
                return lowerSort;
            }
        }
        
        return SUPPORTED_SORT_OPTIONS[0]; // 默认相关性
    }
    
    /**
     * 验证时间范围
     */
    public static String validateTimeRange(String timeRange) {
        if (timeRange == null || timeRange.trim().isEmpty()) {
            return SUPPORTED_TIME_RANGES[0]; // 默认任何时间
        }
        
        String lowerTimeRange = timeRange.toLowerCase().trim();
        for (String range : SUPPORTED_TIME_RANGES) {
            if (range.equals(lowerTimeRange)) {
                return lowerTimeRange;
            }
        }
        
        return SUPPORTED_TIME_RANGES[0]; // 默认任何时间
    }
    
    /**
     * 验证文档类型
     */
    public static String validateDocumentType(String docType) {
        if (docType == null || docType.trim().isEmpty()) {
            return SUPPORTED_DOCUMENT_TYPES[0]; // 默认任何类型
        }
        
        String lowerDocType = docType.toLowerCase().trim();
        for (String type : SUPPORTED_DOCUMENT_TYPES) {
            if (type.equals(lowerDocType)) {
                return lowerDocType;
            }
        }
        
        return SUPPORTED_DOCUMENT_TYPES[0]; // 默认任何类型
    }
}
