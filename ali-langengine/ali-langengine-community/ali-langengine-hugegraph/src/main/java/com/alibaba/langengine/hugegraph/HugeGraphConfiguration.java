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
package com.alibaba.langengine.hugegraph;


public class HugeGraphConfiguration {

    /**
     * HugeGraph连接相关配置
     */
    public static final String HUGEGRAPH_URL = "HUGEGRAPH_URL";
    public static final String HUGEGRAPH_USERNAME = "HUGEGRAPH_USERNAME";
    public static final String HUGEGRAPH_PASSWORD = "HUGEGRAPH_PASSWORD";
    public static final String HUGEGRAPH_DATABASE = "HUGEGRAPH_DATABASE";
    public static final String HUGEGRAPH_GRAPH = "HUGEGRAPH_GRAPH";
    public static final String HUGEGRAPH_TIMEOUT = "HUGEGRAPH_TIMEOUT";
    
    /**
     * 服务器配置
     */
    public static final String SERVER_HOST = "server.host";
    public static final String SERVER_PORT = "server.port";
    public static final String SERVER_USERNAME = "server.username";
    public static final String SERVER_PASSWORD = "server.password";
    public static final String SERVER_PROTOCOL = "server.protocol";
    public static final String SERVER_TRUST_STORE_FILE = "server.trust-store-file";
    public static final String SERVER_TRUST_STORE_PASSWORD = "server.trust-store-password";
    
    /**
     * 图配置
     */
    public static final String GRAPH_TRAVERSAL_SOURCE = "g";
    public static final String GRAPH_MAX_VERTEX_LABEL_COUNT = "graph.max-vertex-label-count";
    public static final String GRAPH_MAX_EDGE_LABEL_COUNT = "graph.max-edge-label-count";
    
    /**
     * 向量配置
     */
    public static final String DEFAULT_VERTEX_LABEL = "Document";
    public static final String DEFAULT_CONTENT_PROPERTY = "content";
    public static final String DEFAULT_VECTOR_PROPERTY = "vector";
    public static final String DEFAULT_METADATA_PROPERTY = "metadata";
    public static final String DEFAULT_ID_PROPERTY = "doc_id";
    public static final int DEFAULT_VECTOR_DIMENSION = 1536;
    public static final int DEFAULT_BATCH_SIZE = 100;
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;
    public static final int DEFAULT_CACHE_SIZE = 1000;
    
    /**
     * 连接配置
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000; // 30 seconds
    public static final int DEFAULT_READ_TIMEOUT = 60000; // 60 seconds
    public static final int DEFAULT_CONNECTION_POOL_SIZE = 10;
    public static final int DEFAULT_MAX_IDLE_CONNECTIONS = 5;
    public static final int DEFAULT_KEEP_ALIVE_DURATION = 300000; // 5 minutes
    
    /**
     * 搜索配置
     */
    public static final int DEFAULT_MAX_SEARCH_RESULTS = 1000;
    public static final boolean DEFAULT_ENABLE_RESULT_CACHE = true;
    public static final int DEFAULT_CACHE_TTL = 300; // 5 minutes
    
    /**
     * 性能配置
     */
    public static final int DEFAULT_THREAD_POOL_SIZE = 10;
    public static final boolean DEFAULT_ENABLE_METRICS = true;
    public static final boolean DEFAULT_ENABLE_BATCH_INSERT = true;
    public static final int DEFAULT_BATCH_INSERT_SIZE = 50;
    
    /**
     * 获取环境变量或系统属性，如果都没有则返回默认值
     */
    public static String getConfigValue(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key, defaultValue);
        }
        return value;
    }
    
    /**
     * 获取整数配置值
     */
    public static int getIntConfigValue(String key, int defaultValue) {
        String value = getConfigValue(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔配置值
     */
    public static boolean getBooleanConfigValue(String key, boolean defaultValue) {
        String value = getConfigValue(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
    
    /**
     * 获取双精度配置值
     */
    public static double getDoubleConfigValue(String key, double defaultValue) {
        String value = getConfigValue(key, String.valueOf(defaultValue));
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
