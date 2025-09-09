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

/**
 * Google搜索常量定义
 */
public final class GoogleConstant {
    
    private GoogleConstant() {}
    
    /**
     * Google搜索基础URL
     */
    public static final String BASE_URL = "https://www.google.com/search";
    
    /**
     * 默认用户代理
     */
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    
    /**
     * 默认超时时间（秒）
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;
    
    /**
     * 默认搜索结果数量
     */
    public static final int DEFAULT_RESULT_COUNT = 10;
    
    /**
     * 最大搜索结果数量
     */
    public static final int MAX_RESULT_COUNT = 100;
    
    /**
     * 最小搜索结果数量
     */
    public static final int MIN_RESULT_COUNT = 1;
    
    /**
     * 支持的搜索类型
     */
    public static final String SEARCH_TYPE_WEB = "web";
    public static final String SEARCH_TYPE_IMAGES = "images";
    public static final String SEARCH_TYPE_NEWS = "news";
    public static final String SEARCH_TYPE_VIDEOS = "videos";
    
    /**
     * 支持的排序方式
     */
    public static final String SORT_RELEVANCE = "relevance";
    public static final String SORT_DATE = "date";
    public static final String SORT_RATING = "rating";
} 