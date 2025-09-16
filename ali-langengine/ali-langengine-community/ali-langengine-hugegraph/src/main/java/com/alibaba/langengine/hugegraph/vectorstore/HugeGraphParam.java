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
package com.alibaba.langengine.hugegraph.vectorstore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.hugegraph.HugeGraphConfiguration.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HugeGraphParam {
    
    /**
     * 服务器配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerConfig {
        
        /**
         * HugeGraph服务器地址
         */
        @Builder.Default
        private String host = getConfigValue(HUGEGRAPH_URL, "localhost");
        
        /**
         * HugeGraph服务器端口
         */
        @Builder.Default
        private int port = getIntConfigValue("hugegraph.port", 8080);
        
        /**
         * 图名称
         */
        @Builder.Default
        private String graph = getConfigValue(HUGEGRAPH_GRAPH, "hugegraph");
        
        /**
         * 用户名
         */
        @Builder.Default
        private String username = getConfigValue(HUGEGRAPH_USERNAME, null);
        
        /**
         * 密码
         */
        @Builder.Default
        private String password = getConfigValue(HUGEGRAPH_PASSWORD, null);
        
        /**
         * 协议类型（http, https）
         */
        @Builder.Default
        private String protocol = getConfigValue("hugegraph.protocol", "http");
        
        /**
         * API版本
         */
        @Builder.Default
        private String apiVersion = "v1";
        
        /**
         * 信任证书文件路径（HTTPS）
         */
        @Builder.Default
        private String trustStoreFile = getConfigValue("hugegraph.trust-store-file", null);
        
        /**
         * 信任证书密码（HTTPS）
         */
        @Builder.Default
        private String trustStorePassword = getConfigValue("hugegraph.trust-store-password", null);
        
        /**
         * 获取完整的服务器URL
         */
        public String getFullUrl() {
            return String.format("%s://%s:%d", protocol, host, port);
        }
        
        /**
         * 获取图API的基础URL
         */
        public String getGraphApiUrl() {
            return String.format("%s/graphs/%s", getFullUrl(), graph);
        }
    }
    
    /**
     * 向量配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VectorConfig {
        
        /**
         * 顶点标签
         */
        @Builder.Default
        private String vertexLabel = DEFAULT_VERTEX_LABEL;
        
        /**
         * 内容属性名
         */
        @Builder.Default
        private String contentPropertyName = DEFAULT_CONTENT_PROPERTY;
        
        /**
         * 向量属性名
         */
        @Builder.Default
        private String vectorPropertyName = DEFAULT_VECTOR_PROPERTY;
        
        /**
         * 元数据属性名
         */
        @Builder.Default
        private String metadataPropertyName = DEFAULT_METADATA_PROPERTY;
        
        /**
         * ID属性名
         */
        @Builder.Default
        private String idPropertyName = DEFAULT_ID_PROPERTY;
        
        /**
         * 向量维度
         */
        @Builder.Default
        private int vectorDimension = DEFAULT_VECTOR_DIMENSION;
        
        /**
         * 是否启用向量索引
         */
        @Builder.Default
        private boolean enableVectorIndex = true;
        
        /**
         * 向量索引名称
         */
        @Builder.Default
        private String vectorIndexName = "vector_index";
        
        /**
         * 向量索引类型
         */
        @Builder.Default
        private String vectorIndexType = "FLAT";
        
        /**
         * 索引参数
         */
        @Builder.Default
        private Map<String, Object> indexParameters = new HashMap<>();
    }
    
    /**
     * 连接配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionConfig {
        
        /**
         * 连接超时时间（毫秒）
         */
        @Builder.Default
        private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        
        /**
         * 读取超时时间（毫秒）
         */
        @Builder.Default
        private int readTimeout = DEFAULT_READ_TIMEOUT;
        
        /**
         * 连接池大小
         */
        @Builder.Default
        private int connectionPoolSize = DEFAULT_CONNECTION_POOL_SIZE;
        
        /**
         * 最大空闲连接数
         */
        @Builder.Default
        private int maxIdleConnections = DEFAULT_MAX_IDLE_CONNECTIONS;
        
        /**
         * 连接保活时间（毫秒）
         */
        @Builder.Default
        private long keepAliveDuration = DEFAULT_KEEP_ALIVE_DURATION;
        
        /**
         * 重试次数
         */
        @Builder.Default
        private int maxRetries = 3;
        
        /**
         * 重试间隔（毫秒）
         */
        @Builder.Default
        private long retryInterval = 1000;
        
        /**
         * 是否启用连接池
         */
        @Builder.Default
        private boolean enableConnectionPool = true;
        
        /**
         * 是否验证SSL证书
         */
        @Builder.Default
        private boolean validateSslCertificate = true;
    }
    
    /**
     * 批处理配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchConfig {
        
        /**
         * 批处理大小
         */
        @Builder.Default
        private int batchSize = DEFAULT_BATCH_SIZE;
        
        /**
         * 批处理插入大小
         */
        @Builder.Default
        private int batchInsertSize = DEFAULT_BATCH_INSERT_SIZE;
        
        /**
         * 是否启用批处理插入
         */
        @Builder.Default
        private boolean enableBatchInsert = DEFAULT_ENABLE_BATCH_INSERT;
        
        /**
         * 批处理超时时间（毫秒）
         */
        @Builder.Default
        private long batchTimeout = 30000;
        
        /**
         * 批处理队列大小
         */
        @Builder.Default
        private int batchQueueSize = 1000;
        
        /**
         * 批处理刷新间隔（毫秒）
         */
        @Builder.Default
        private long batchFlushInterval = 5000;
        
        /**
         * 是否启用异步批处理
         */
        @Builder.Default
        private boolean enableAsyncBatch = true;
    }
    
    /**
     * 缓存配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheConfig {
        
        /**
         * 缓存大小
         */
        @Builder.Default
        private int cacheSize = DEFAULT_CACHE_SIZE;
        
        /**
         * 是否启用结果缓存
         */
        @Builder.Default
        private boolean enableResultCache = DEFAULT_ENABLE_RESULT_CACHE;
        
        /**
         * 缓存TTL（秒）
         */
        @Builder.Default
        private int cacheTtl = DEFAULT_CACHE_TTL;
        
        /**
         * 缓存类型（MEMORY, REDIS, CAFFEINE）
         */
        @Builder.Default
        private String cacheType = "MEMORY";
        
        /**
         * 是否启用查询缓存
         */
        @Builder.Default
        private boolean enableQueryCache = true;
        
        /**
         * 查询缓存TTL（秒）
         */
        @Builder.Default
        private int queryCacheTtl = 300;
        
        /**
         * 最大缓存内存（MB）
         */
        @Builder.Default
        private int maxCacheMemoryMb = 100;
    }
    
    /**
     * 性能配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceConfig {
        
        /**
         * 线程池大小
         */
        @Builder.Default
        private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        
        /**
         * 是否启用性能监控
         */
        @Builder.Default
        private boolean enableMetrics = DEFAULT_ENABLE_METRICS;
        
        /**
         * 最大搜索结果数
         */
        @Builder.Default
        private int maxSearchResults = DEFAULT_MAX_SEARCH_RESULTS;
        
        /**
         * 查询超时时间（毫秒）
         */
        @Builder.Default
        private long queryTimeout = 60000;
        
        /**
         * 是否启用并行查询
         */
        @Builder.Default
        private boolean enableParallelQuery = true;
        
        /**
         * 并行查询线程数
         */
        @Builder.Default
        private int parallelQueryThreads = 4;
        
        /**
         * 内存使用阈值（百分比）
         */
        @Builder.Default
        private double memoryThreshold = 0.8;
        
        /**
         * 是否启用预取
         */
        @Builder.Default
        private boolean enablePrefetch = true;
        
        /**
         * 预取大小
         */
        @Builder.Default
        private int prefetchSize = 100;
    }
    
    /**
     * 初始化参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitParam {
        
        /**
         * 是否在初始化时创建图结构
         */
        @Builder.Default
        private Boolean createSchemaOnInit = true;
        
        /**
         * 是否在初始化时创建索引
         */
        @Builder.Default
        private Boolean createIndexOnInit = true;
        
        /**
         * 是否检查现有结构
         */
        @Builder.Default
        private Boolean checkExistingSchema = true;
        
        /**
         * 初始化超时时间（毫秒）
         */
        @Builder.Default
        private long initTimeout = 120000;
        
        /**
         * 是否启用初始化验证
         */
        @Builder.Default
        private boolean enableInitValidation = true;
        
        /**
         * 是否强制重新初始化
         */
        @Builder.Default
        private boolean forceReinit = false;
    }
    
    // 主配置属性
    
    /**
     * 服务器配置
     */
    @Builder.Default
    private ServerConfig serverConfig = ServerConfig.builder().build();
    
    /**
     * 向量配置
     */
    @Builder.Default
    private VectorConfig vectorConfig = VectorConfig.builder().build();
    
    /**
     * 连接配置
     */
    @Builder.Default
    private ConnectionConfig connectionConfig = ConnectionConfig.builder().build();
    
    /**
     * 批处理配置
     */
    @Builder.Default
    private BatchConfig batchConfig = BatchConfig.builder().build();
    
    /**
     * 缓存配置
     */
    @Builder.Default
    private CacheConfig cacheConfig = CacheConfig.builder().build();
    
    /**
     * 性能配置
     */
    @Builder.Default
    private PerformanceConfig performanceConfig = PerformanceConfig.builder().build();
    
    /**
     * 初始化参数
     */
    @Builder.Default
    private InitParam initParam = InitParam.builder().build();
    
    /**
     * 自定义属性
     */
    @Builder.Default
    private Map<String, Object> customProperties = new HashMap<>();
    
    /**
     * 获取默认配置
     */
    public static HugeGraphParam getDefaultConfig() {
        return HugeGraphParam.builder().build();
    }
    
    /**
     * 创建本地开发配置
     */
    public static HugeGraphParam createLocalConfig() {
        return HugeGraphParam.builder()
                .serverConfig(ServerConfig.builder()
                        .host("localhost")
                        .port(8080)
                        .graph("hugegraph")
                        .protocol("http")
                        .build())
                .connectionConfig(ConnectionConfig.builder()
                        .connectionTimeout(30000)
                        .readTimeout(60000)
                        .maxRetries(3)
                        .build())
                .build();
    }
    
    /**
     * 创建生产环境配置
     */
    public static HugeGraphParam createProductionConfig(String host, int port, String graph, 
                                                       String username, String password) {
        return HugeGraphParam.builder()
                .serverConfig(ServerConfig.builder()
                        .host(host)
                        .port(port)
                        .graph(graph)
                        .username(username)
                        .password(password)
                        .protocol("https")
                        .build())
                .connectionConfig(ConnectionConfig.builder()
                        .connectionTimeout(30000)
                        .readTimeout(120000)
                        .connectionPoolSize(20)
                        .maxRetries(5)
                        .enableConnectionPool(true)
                        .build())
                .performanceConfig(PerformanceConfig.builder()
                        .enableMetrics(true)
                        .enableParallelQuery(true)
                        .parallelQueryThreads(8)
                        .queryTimeout(300000)
                        .build())
                .cacheConfig(CacheConfig.builder()
                        .enableResultCache(true)
                        .cacheSize(5000)
                        .cacheTtl(1800)
                        .maxCacheMemoryMb(500)
                        .build())
                .build();
    }
    
    /**
     * 添加自定义属性
     */
    public void addCustomProperty(String key, Object value) {
        if (customProperties == null) {
            customProperties = new HashMap<>();
        }
        customProperties.put(key, value);
    }
    
    /**
     * 获取自定义属性
     */
    public Object getCustomProperty(String key) {
        return customProperties != null ? customProperties.get(key) : null;
    }
    
    /**
     * 验证配置参数
     */
    public void validate() {
        if (serverConfig == null) {
            throw new IllegalArgumentException("ServerConfig cannot be null");
        }
        
        if (vectorConfig == null) {
            throw new IllegalArgumentException("VectorConfig cannot be null");
        }
        
        if (vectorConfig.getVectorDimension() <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive");
        }
        
        if (batchConfig != null && batchConfig.getBatchSize() <= 0) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        
        if (connectionConfig != null) {
            if (connectionConfig.getConnectionTimeout() <= 0) {
                throw new IllegalArgumentException("Connection timeout must be positive");
            }
            if (connectionConfig.getReadTimeout() <= 0) {
                throw new IllegalArgumentException("Read timeout must be positive");
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format("HugeGraphParam{server=%s:%d, graph=%s, vectorDim=%d}", 
                           serverConfig.getHost(), 
                           serverConfig.getPort(), 
                           serverConfig.getGraph(),
                           vectorConfig.getVectorDimension());
    }
}
