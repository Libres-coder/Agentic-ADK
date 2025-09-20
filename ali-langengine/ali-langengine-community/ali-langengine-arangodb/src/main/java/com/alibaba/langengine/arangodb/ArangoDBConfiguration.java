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
package com.alibaba.langengine.arangodb;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import lombok.Data;


@Data
public class ArangoDBConfiguration {
    
    /**
     * ArangoDB 连接主机
     */
    public static final String ARANGODB_HOST = WorkPropertiesUtils.get("arangodb_host", "localhost");
    
    /**
     * ArangoDB 连接端口
     */
    public static final int ARANGODB_PORT = Integer.parseInt(WorkPropertiesUtils.get("arangodb_port", "8529"));
    
    /**
     * ArangoDB 用户名
     */
    public static final String ARANGODB_USERNAME = WorkPropertiesUtils.get("arangodb_username", "root");
    
    /**
     * ArangoDB 密码
     */
    public static final String ARANGODB_PASSWORD = WorkPropertiesUtils.get("arangodb_password", "");
    
    /**
     * ArangoDB 数据库名称
     */
    public static final String ARANGODB_DATABASE = WorkPropertiesUtils.get("arangodb_database", "langengine");
    
    /**
     * 默认向量维度
     */
    public static final int DEFAULT_VECTOR_DIMENSION = 768;
    
    /**
     * 默认相似度阈值
     */
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;
    
    /**
     * 默认批处理大小
     */
    public static final int DEFAULT_BATCH_SIZE = 100;
    
    /**
     * 默认最大缓存大小
     */
    public static final int DEFAULT_MAX_CACHE_SIZE = 1000;
    
    /**
     * 连接超时时间(毫秒)
     */
    public static final int DEFAULT_TIMEOUT_MS = 30000;
    
    /**
     * 默认相似度距离函数
     */
    public static final String DEFAULT_DISTANCE_FUNCTION = "COSINE";
    
    /**
     * 默认集合名称前缀
     */
    public static final String DEFAULT_COLLECTION_PREFIX = "vectors_";
    
    /**
     * 默认索引名称后缀
     */
    public static final String DEFAULT_INDEX_SUFFIX = "_vector_idx";

    // 实例配置字段
    private String host = ARANGODB_HOST;
    private int port = ARANGODB_PORT;
    private String username = ARANGODB_USERNAME;
    private String password = ARANGODB_PASSWORD;
    private String database = ARANGODB_DATABASE;
    private int vectorDimension = DEFAULT_VECTOR_DIMENSION;
    private double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;
    private int batchSize = DEFAULT_BATCH_SIZE;
    private int maxCacheSize = DEFAULT_MAX_CACHE_SIZE;
    private int timeoutMs = DEFAULT_TIMEOUT_MS;
    private String distanceFunction = DEFAULT_DISTANCE_FUNCTION;
    
    /**
     * 构造函数
     */
    public ArangoDBConfiguration() {}
    
    /**
     * 带参数的构造函数
     */
    public ArangoDBConfiguration(String host, int port, String username, String password, String database) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }
    
    /**
     * 验证配置
     */
    public void validate() {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("ArangoDB host cannot be null or empty");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("ArangoDB port must be between 1 and 65535");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("ArangoDB username cannot be null or empty");
        }
        if (database == null || database.trim().isEmpty()) {
            throw new IllegalArgumentException("ArangoDB database cannot be null or empty");
        }
        if (vectorDimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive");
        }
        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        if (maxCacheSize <= 0) {
            throw new IllegalArgumentException("Max cache size must be positive");
        }
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
    }
}
