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
package com.alibaba.langengine.janusgraph;


public class JanusGraphConfiguration {

    /**
     * JanusGraph连接相关配置
     */
    public static final String JANUSGRAPH_URL = "JANUSGRAPH_URL";
    public static final String JANUSGRAPH_USERNAME = "JANUSGRAPH_USERNAME";
    public static final String JANUSGRAPH_PASSWORD = "JANUSGRAPH_PASSWORD";
    public static final String JANUSGRAPH_DATABASE = "JANUSGRAPH_DATABASE";
    
    /**
     * 存储后端配置
     */
    public static final String STORAGE_BACKEND = "storage.backend";
    public static final String STORAGE_HOSTNAME = "storage.hostname";
    public static final String STORAGE_PORT = "storage.port";
    public static final String STORAGE_CASSANDRA_KEYSPACE = "storage.cassandra.keyspace";
    public static final String STORAGE_HBASE_TABLE = "storage.hbase.table";
    
    /**
     * 索引后端配置
     */
    public static final String INDEX_BACKEND = "index.search.backend";
    public static final String INDEX_HOSTNAME = "index.search.hostname";
    public static final String INDEX_PORT = "index.search.port";
    public static final String INDEX_ELASTICSEARCH_CLIENT_ONLY = "index.search.elasticsearch.client-only";
    
    /**
     * 图配置
     */
    public static final String GRAPH_TRAVERSAL_SOURCE = "g";
    public static final String GRAPH_MAX_VERTEX_LABEL_COUNT = "graph.max-vertex-label-count";
    public static final String GRAPH_MAX_EDGE_LABEL_COUNT = "graph.max-edge-label-count";
    
    /**
     * 向量存储相关常量
     */
    public static final String DEFAULT_VECTOR_DIMENSION = "768";
    public static final String DEFAULT_VERTEX_LABEL = "Document";
    public static final String DEFAULT_EDGE_LABEL = "similar_to";
    public static final String DEFAULT_VECTOR_PROPERTY = "embedding";
    public static final String DEFAULT_CONTENT_PROPERTY = "content";
    public static final String DEFAULT_ID_PROPERTY = "docId";
    public static final String DEFAULT_METADATA_PROPERTY = "metadata";
    public static final String DEFAULT_SCORE_PROPERTY = "score";
    
    /**
     * 连接池配置
     */
    public static final int DEFAULT_MAX_CONNECTION_POOL_SIZE = 10;
    public static final int DEFAULT_MIN_CONNECTION_POOL_SIZE = 1;
    public static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 30;
    public static final int DEFAULT_READ_TIMEOUT_SECONDS = 60;
    public static final int DEFAULT_WRITE_TIMEOUT_SECONDS = 60;
    
    /**
     * 批量操作配置
     */
    public static final int DEFAULT_BATCH_SIZE = 100;
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final long DEFAULT_RETRY_DELAY_MS = 1000L;
    
    /**
     * 默认向量相似度阈值
     */
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;
    
    /**
     * 缓存配置
     */
    public static final String CACHE_DB_CACHE = "cache.db-cache";
    public static final String CACHE_DB_CACHE_CLEAN_WAIT = "cache.db-cache-clean-wait";
    public static final String CACHE_DB_CACHE_SIZE = "cache.db-cache-size";
    
    /**
     * 序列化配置
     */
    public static final String ATTRIBUTES_ALLOW_ALL = "attributes.allow-all";
    public static final String SCHEMA_CONSTRAINTS = "schema.constraints";
    public static final String SCHEMA_DEFAULT = "schema.default";
}
