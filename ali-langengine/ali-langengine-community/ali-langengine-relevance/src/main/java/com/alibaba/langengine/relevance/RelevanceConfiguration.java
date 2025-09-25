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
package com.alibaba.langengine.relevance;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class RelevanceConfiguration {

    /**
     * Relevance AI API 基础 URL
     */
    public static String RELEVANCE_API_URL = WorkPropertiesUtils.get("relevance_api_url", "https://api.relevanceai.com");

    /**
     * Relevance AI API 密钥
     */
    public static String RELEVANCE_API_KEY = WorkPropertiesUtils.get("relevance_api_key");

    /**
     * 项目 ID
     */
    public static String RELEVANCE_PROJECT_ID = WorkPropertiesUtils.get("relevance_project_id");

    /**
     * 默认向量维度
     */
    public static String RELEVANCE_DEFAULT_VECTOR_SIZE = WorkPropertiesUtils.get("relevance_default_vector_size", "1536");

    /**
     * 默认相似度度量
     */
    public static String RELEVANCE_DEFAULT_METRIC = WorkPropertiesUtils.get("relevance_default_metric", "cosine");

    /**
     * 请求超时时间（毫秒）
     */
    public static String RELEVANCE_REQUEST_TIMEOUT = WorkPropertiesUtils.get("relevance_request_timeout", "30000");

    /**
     * 连接超时时间（毫秒）
     */
    public static String RELEVANCE_CONNECTION_TIMEOUT = WorkPropertiesUtils.get("relevance_connection_timeout", "10000");

    /**
     * 最大重试次数
     */
    public static String RELEVANCE_MAX_RETRIES = WorkPropertiesUtils.get("relevance_max_retries", "3");
}