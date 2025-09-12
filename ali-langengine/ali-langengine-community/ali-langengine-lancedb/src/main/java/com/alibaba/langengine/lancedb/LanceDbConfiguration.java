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
package com.alibaba.langengine.lancedb;

import lombok.Builder;
import lombok.Value;



@Value
@Builder
public class LanceDbConfiguration {

    /**
     * LanceDB服务器URL，默认本地实例
     */
    public static final String LANCEDB_SERVER_URL = "lancedb.server.url";

    /**
     * 服务器URI
     */
    @Builder.Default
    private String uri = "http://localhost:8000";

    /**
     * 基础URL（用于兼容测试）
     */
    @Builder.Default
    private String baseUrl = "http://localhost:8000";

    /**
     * API密钥，用于认证
     */
    private String apiKey;

    /**
     * 连接超时时间（毫秒）
     */
    @Builder.Default
    private int connectionTimeout = 30000;

    /**
     * 读取超时时间（毫秒）
     */
    @Builder.Default
    private int readTimeout = 60000;

    /**
     * 写入超时时间（毫秒）
     */
    @Builder.Default
    private int writeTimeout = 60000;

    /**
     * 最大重试次数
     */
    @Builder.Default
    private int maxRetries = 3;

    /**
     * 重试间隔（毫秒）
     */
    @Builder.Default
    private long retryIntervalMs = 1000;

    /**
     * 批量操作大小
     */
    @Builder.Default
    private int batchSize = 100;

    /**
     * 是否启用SSL
     */
    @Builder.Default
    private boolean enableSsl = false;

    /**
     * 连接池大小
     */
    @Builder.Default
    private int connectionPoolSize = 10;

    /**
     * 默认向量维度
     */
    @Builder.Default
    private int defaultVectorDimension = 384;

    /**
     * 默认相似度阈值
     */
    @Builder.Default
    private double defaultSimilarityThreshold = 0.7;

    /**
     * 是否启用缓存
     */
    @Builder.Default
    private boolean cacheEnabled = true;

    /**
     * 缓存大小
     */
    @Builder.Default
    private int cacheSize = 1000;

    /**
     * 缓存过期时间（分钟）
     */
    @Builder.Default
    private int cacheExpirationMinutes = 60;

    /**
     * 创建默认配置实例
     */
    public static LanceDbConfiguration createDefault() {
        String serverUrl = System.getProperty(LANCEDB_SERVER_URL);
        LanceDbConfigurationBuilder builder = LanceDbConfiguration.builder();
        if (serverUrl != null && !serverUrl.trim().isEmpty()) {
            builder.uri(serverUrl).baseUrl(serverUrl);
        }
        return builder.build();
    }

    /**
     * 验证配置是否有效
     *
     * @return 配置是否有效
     */
    public boolean isValid() {
        return uri != null && !uri.trim().isEmpty() && 
               connectionTimeout > 0 && 
               readTimeout > 0 && 
               writeTimeout > 0 && 
               maxRetries >= 0 && 
               retryIntervalMs >= 0 && 
               batchSize > 0 && 
               connectionPoolSize > 0 && 
               defaultVectorDimension > 0 && 
               defaultSimilarityThreshold >= 0.0 && 
               defaultSimilarityThreshold <= 1.0;
    }

    /**
     * 获取完整的服务器URL
     *
     * @return 完整的服务器URL
     */
    public String getFullServerUrl() {
        String baseUrl = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
        return enableSsl && !baseUrl.startsWith("https://") ? 
               baseUrl.replace("http://", "https://") : baseUrl;
    }
}
