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
package com.alibaba.langengine.lancedb.vectorstore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanceDbParam {

    /**
     * 服务器URI
     */
    private String uri;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 向量维度
     */
    private Integer vectorDimension;

    /**
     * 维度（向量维度的别名，用于兼容测试）
     */
    private Integer dimension;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectionTimeout;

    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeout;

    /**
     * 写入超时时间（毫秒）
     */
    private Integer writeTimeout;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 重试间隔（毫秒）
     */
    private Long retryIntervalMs;

    /**
     * 批量操作大小
     */
    private Integer batchSize;

    /**
     * 是否启用SSL
     */
    private Boolean enableSsl;

    /**
     * 连接池大小
     */
    private Integer connectionPoolSize;

    /**
     * 相似度阈值
     */
    private Double similarityThreshold;

    /**
     * 缓存大小
     */
    private Integer cacheSize;

    /**
     * 缓存过期时间（分钟）
     */
    private Integer cacheExpirationMinutes;

    /**
     * 度量类型
     */
    private String metric;

    /**
     * 是否启用缓存
     */
    private Boolean cacheEnabled;

    /**
     * 创建默认参数
     *
     * @return 默认参数
     */
    public static LanceDbParam defaultParams() {
        return LanceDbParam.builder()
                .uri("http://localhost:8000")
                .tableName("default_table")
                .vectorDimension(384)
                .connectionTimeout(30000)
                .readTimeout(60000)
                .writeTimeout(60000)
                .maxRetries(3)
                .retryIntervalMs(1000L)
                .batchSize(100)
                .enableSsl(false)
                .connectionPoolSize(10)
                .similarityThreshold(0.7)
                .cacheSize(1000)
                .cacheExpirationMinutes(60)
                .metric("cosine")
                .cacheEnabled(true)
                .build();
    }

    /**
     * 创建本地开发参数
     *
     * @param tableName 表名
     * @return 本地参数
     */
    public static LanceDbParam localParams(String tableName) {
        return LanceDbParam.builder()
                .uri("http://localhost:8000")
                .tableName(tableName)
                .vectorDimension(384)
                .connectionTimeout(10000)
                .readTimeout(30000)
                .writeTimeout(30000)
                .maxRetries(1)
                .retryIntervalMs(500L)
                .batchSize(50)
                .enableSsl(false)
                .connectionPoolSize(5)
                .similarityThreshold(0.6)
                .cacheSize(500)
                .cacheExpirationMinutes(30)
                .metric("cosine")
                .cacheEnabled(true)
                .build();
    }

    /**
     * 创建生产环境参数
     *
     * @param uri       服务器URI
     * @param apiKey    API密钥
     * @param tableName 表名
     * @return 生产参数
     */
    public static LanceDbParam productionParams(String uri, String apiKey, String tableName) {
        return LanceDbParam.builder()
                .uri(uri)
                .apiKey(apiKey)
                .tableName(tableName)
                .vectorDimension(768)
                .connectionTimeout(60000)
                .readTimeout(120000)
                .writeTimeout(120000)
                .maxRetries(5)
                .retryIntervalMs(2000L)
                .batchSize(200)
                .enableSsl(true)
                .connectionPoolSize(20)
                .similarityThreshold(0.8)
                .cacheSize(2000)
                .cacheExpirationMinutes(120)
                .metric("cosine")
                .cacheEnabled(true)
                .build();
    }

    /**
     * 验证参数有效性
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return uri != null && !uri.trim().isEmpty() &&
               tableName != null && !tableName.trim().isEmpty() &&
               (vectorDimension == null || vectorDimension > 0) &&
               (connectionTimeout == null || connectionTimeout > 0) &&
               (readTimeout == null || readTimeout > 0) &&
               (writeTimeout == null || writeTimeout > 0) &&
               (maxRetries == null || maxRetries >= 0) &&
               (retryIntervalMs == null || retryIntervalMs >= 0) &&
               (batchSize == null || batchSize > 0) &&
               (connectionPoolSize == null || connectionPoolSize > 0) &&
               (similarityThreshold == null || (similarityThreshold >= 0.0 && similarityThreshold <= 1.0)) &&
               (cacheSize == null || cacheSize >= 0) &&
               (cacheExpirationMinutes == null || cacheExpirationMinutes >= 0);
    }

    /**
     * 获取有效的URI
     *
     * @return URI
     */
    public String getEffectiveUri() {
        return uri != null ? uri : "http://localhost:8000";
    }

    /**
     * 获取有效的表名
     *
     * @return 表名
     */
    public String getEffectiveTableName() {
        return tableName != null ? tableName : "default_table";
    }

    /**
     * 获取有效的向量维度
     *
     * @return 向量维度
     */
    public int getEffectiveVectorDimension() {
        return vectorDimension != null ? vectorDimension : 384;
    }

    /**
     * 获取有效的相似度阈值
     *
     * @return 相似度阈值
     */
    public double getEffectiveSimilarityThreshold() {
        return similarityThreshold != null ? similarityThreshold : 0.7;
    }

    /**
     * 获取有效的缓存大小
     *
     * @return 缓存大小
     */
    public int getEffectiveCacheSize() {
        return cacheSize != null ? cacheSize : 1000;
    }

    /**
     * 是否启用SSL
     *
     * @return 是否启用SSL
     */
    public boolean isEffectiveEnableSsl() {
        return enableSsl != null ? enableSsl : false;
    }

    /**
     * 是否启用缓存
     *
     * @return 是否启用缓存
     */
    public boolean isEffectiveCacheEnabled() {
        return cacheEnabled != null ? cacheEnabled : true;
    }
}
