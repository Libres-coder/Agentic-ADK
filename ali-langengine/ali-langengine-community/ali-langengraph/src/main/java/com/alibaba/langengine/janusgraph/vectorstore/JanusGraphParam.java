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
package com.alibaba.langengine.janusgraph.vectorstore;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.langengine.janusgraph.JanusGraphConfiguration.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JanusGraphParam {

    /**
     * 图数据库配置
     */
    private GraphConfig graphConfig;
    
    /**
     * 向量存储配置
     */
    private VectorConfig vectorConfig;
    
    /**
     * 连接配置
     */
    private ConnectionConfig connectionConfig;
    
    /**
     * 索引配置
     */
    private IndexConfig indexConfig;
    
    /**
     * 批量操作配置
     */
    private BatchConfig batchConfig;
    
    /**
     * 初始化参数配置
     */
    private InitParam initParam;

    /**
     * 图数据库配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GraphConfig {
        /**
         * 存储后端类型：berkeleyje, cassandra, hbase, inmemory
         */
        @Builder.Default
        private String storageBackend = "berkeleyje";
        
        /**
         * 存储后端主机名
         */
        private String storageHostname;
        
        /**
         * 存储后端端口
         */
        private Integer storagePort;
        
        /**
         * Cassandra keyspace名称
         */
        @Builder.Default
        private String cassandraKeyspace = "janusgraph";
        
        /**
         * HBase表名
         */
        @Builder.Default
        private String hbaseTable = "janusgraph";
        
        /**
         * 图的配置属性
         */
        @Builder.Default
        private Map<String, Object> graphProperties = new HashMap<>();
    }

    /**
     * 向量存储配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VectorConfig {
        /**
         * 节点标签名称
         */
        @Builder.Default
        private String vertexLabel = DEFAULT_VERTEX_LABEL;
        
        /**
         * 边标签名称
         */
        @Builder.Default
        private String edgeLabel = DEFAULT_EDGE_LABEL;
        
        /**
         * 向量维度
         */
        @Builder.Default
        private Integer vectorDimension = Integer.parseInt(DEFAULT_VECTOR_DIMENSION);
        
        /**
         * 文档ID属性名
         */
        @Builder.Default
        private String idPropertyName = DEFAULT_ID_PROPERTY;
        
        /**
         * 文档内容属性名
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
         * 相似度分数属性名
         */
        @Builder.Default
        private String scorePropertyName = DEFAULT_SCORE_PROPERTY;
        
        /**
         * 相似度阈值
         */
        @Builder.Default
        private Double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;
        
        /**
         * 是否启用向量索引
         */
        @Builder.Default
        private Boolean enableVectorIndex = true;
    }

    /**
     * 连接配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConnectionConfig {
        /**
         * 最大连接池大小
         */
        @Builder.Default
        private Integer maxConnectionPoolSize = DEFAULT_MAX_CONNECTION_POOL_SIZE;
        
        /**
         * 最小连接池大小
         */
        @Builder.Default
        private Integer minConnectionPoolSize = DEFAULT_MIN_CONNECTION_POOL_SIZE;
        
        /**
         * 连接超时时间(秒)
         */
        @Builder.Default
        private Integer connectionTimeoutSeconds = DEFAULT_CONNECTION_TIMEOUT_SECONDS;
        
        /**
         * 读取超时时间(秒)
         */
        @Builder.Default
        private Integer readTimeoutSeconds = DEFAULT_READ_TIMEOUT_SECONDS;
        
        /**
         * 写入超时时间(秒)
         */
        @Builder.Default
        private Integer writeTimeoutSeconds = DEFAULT_WRITE_TIMEOUT_SECONDS;
        
        /**
         * 是否启用连接池
         */
        @Builder.Default
        private Boolean enableConnectionPool = true;
        
        /**
         * 连接重试次数
         */
        @Builder.Default
        private Integer maxRetries = DEFAULT_MAX_RETRIES;
        
        /**
         * 重试延迟时间(毫秒)
         */
        @Builder.Default
        private Long retryDelayMs = DEFAULT_RETRY_DELAY_MS;
    }

    /**
     * 索引配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IndexConfig {
        /**
         * 索引后端类型：elasticsearch, solr, lucene
         */
        @Builder.Default
        private String indexBackend = "elasticsearch";
        
        /**
         * 索引后端主机名
         */
        private String indexHostname;
        
        /**
         * 索引后端端口
         */
        private Integer indexPort;
        
        /**
         * 是否仅作为Elasticsearch客户端
         */
        @Builder.Default
        private Boolean elasticsearchClientOnly = true;
        
        /**
         * 索引名称
         */
        @Builder.Default
        private String indexName = "janusgraph_vectors";
        
        /**
         * 向量索引名称
         */
        @Builder.Default
        private String vectorIndexName = "vector_index";
        
        /**
         * 是否启用混合索引
         */
        @Builder.Default
        private Boolean enableMixedIndex = true;
        
        /**
         * 索引配置属性
         */
        @Builder.Default
        private Map<String, Object> indexProperties = new HashMap<>();
    }

    /**
     * 批量操作配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchConfig {
        /**
         * 批量大小
         */
        @Builder.Default
        private Integer batchSize = DEFAULT_BATCH_SIZE;
        
        /**
         * 是否启用批量提交
         */
        @Builder.Default
        private Boolean enableBatchCommit = true;
        
        /**
         * 批量提交间隔时间(毫秒)
         */
        @Builder.Default
        private Long batchCommitInterval = 5000L;
        
        /**
         * 并行处理线程数
         */
        @Builder.Default
        private Integer parallelThreads = Runtime.getRuntime().availableProcessors();
    }

    /**
     * 初始化参数配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InitParam {
        /**
         * 是否在初始化时创建Schema
         */
        @Builder.Default
        private Boolean createSchemaOnInit = true;
        
        /**
         * 是否在初始化时创建索引
         */
        @Builder.Default
        private Boolean createIndexOnInit = true;
        
        /**
         * 是否在初始化时验证Schema
         */
        @Builder.Default
        private Boolean validateSchemaOnInit = true;
        
        /**
         * 是否启用Schema约束
         */
        @Builder.Default
        private Boolean enableSchemaConstraints = true;
        
        /**
         * 是否允许所有属性
         */
        @Builder.Default
        private Boolean allowAllAttributes = false;
        
        /**
         * Schema默认值
         */
        @Builder.Default
        private String schemaDefault = "none";
    }

    /**
     * 获取默认配置实例
     * 
     * @return JanusGraphParam实例
     */
    public static JanusGraphParam getDefaultConfig() {
        return JanusGraphParam.builder()
                .graphConfig(GraphConfig.builder().build())
                .vectorConfig(VectorConfig.builder().build())
                .connectionConfig(ConnectionConfig.builder().build())
                .indexConfig(IndexConfig.builder().build())
                .batchConfig(BatchConfig.builder().build())
                .initParam(InitParam.builder().build())
                .build();
    }

    /**
     * 转换为JanusGraph配置Map
     * 
     * @return 配置Map
     */
    public Map<String, Object> toJanusGraphConfig() {
        Map<String, Object> config = new HashMap<>();
        
        if (graphConfig != null) {
            config.put(STORAGE_BACKEND, graphConfig.getStorageBackend());
            if (graphConfig.getStorageHostname() != null) {
                config.put(STORAGE_HOSTNAME, graphConfig.getStorageHostname());
            }
            if (graphConfig.getStoragePort() != null) {
                config.put(STORAGE_PORT, graphConfig.getStoragePort());
            }
            if ("cassandra".equals(graphConfig.getStorageBackend())) {
                config.put(STORAGE_CASSANDRA_KEYSPACE, graphConfig.getCassandraKeyspace());
            } else if ("hbase".equals(graphConfig.getStorageBackend())) {
                config.put(STORAGE_HBASE_TABLE, graphConfig.getHbaseTable());
            }
            config.putAll(graphConfig.getGraphProperties());
        }
        
        if (indexConfig != null) {
            config.put(INDEX_BACKEND, indexConfig.getIndexBackend());
            if (indexConfig.getIndexHostname() != null) {
                config.put(INDEX_HOSTNAME, indexConfig.getIndexHostname());
            }
            if (indexConfig.getIndexPort() != null) {
                config.put(INDEX_PORT, indexConfig.getIndexPort());
            }
            config.put(INDEX_ELASTICSEARCH_CLIENT_ONLY, indexConfig.getElasticsearchClientOnly());
            config.putAll(indexConfig.getIndexProperties());
        }
        
        if (initParam != null) {
            config.put(ATTRIBUTES_ALLOW_ALL, initParam.getAllowAllAttributes());
            config.put(SCHEMA_CONSTRAINTS, initParam.getEnableSchemaConstraints());
            config.put(SCHEMA_DEFAULT, initParam.getSchemaDefault());
        }
        
        return config;
    }
}
