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
package com.alibaba.langengine.nebulagraph;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class NebulaGraphConfiguration {

    /**
     * NebulaGraph Meta 服务地址
     */
    public static String NEBULAGRAPH_META_HOST = WorkPropertiesUtils.get("nebulagraph_meta_host", "127.0.0.1");

    /**
     * NebulaGraph Meta 服务端口
     */
    public static int NEBULAGRAPH_META_PORT = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_meta_port", "9559"));

    /**
     * NebulaGraph Graph 服务地址
     */
    public static String NEBULAGRAPH_GRAPH_HOST = WorkPropertiesUtils.get("nebulagraph_graph_host", "127.0.0.1");

    /**
     * NebulaGraph Graph 服务端口
     */
    public static int NEBULAGRAPH_GRAPH_PORT = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_graph_port", "9669"));

    /**
     * NebulaGraph Storage 服务地址
     */
    public static String NEBULAGRAPH_STORAGE_HOST = WorkPropertiesUtils.get("nebulagraph_storage_host", "127.0.0.1");

    /**
     * NebulaGraph Storage 服务端口
     */
    public static int NEBULAGRAPH_STORAGE_PORT = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_storage_port", "9779"));

    /**
     * NebulaGraph 用户名
     */
    public static String NEBULAGRAPH_USERNAME = WorkPropertiesUtils.get("nebulagraph_username", "root");

    /**
     * NebulaGraph 密码
     */
    public static String NEBULAGRAPH_PASSWORD = WorkPropertiesUtils.get("nebulagraph_password", "nebula");

    /**
     * 默认图空间名称
     */
    public static String DEFAULT_SPACE_NAME = WorkPropertiesUtils.get("nebulagraph_default_space", "langengine");

    /**
     * 默认标签名称
     */
    public static String DEFAULT_TAG_NAME = WorkPropertiesUtils.get("nebulagraph_default_tag", "Document");

    /**
     * 默认向量维度
     */
    public static int DEFAULT_VECTOR_DIMENSION = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_default_dimension", "1536"));

    /**
     * 默认距离函数
     */
    public static String DEFAULT_DISTANCE_FUNCTION = WorkPropertiesUtils.get("nebulagraph_default_distance_function", "cosine");

    /**
     * 默认相似度阈值
     */
    public static double DEFAULT_SIMILARITY_THRESHOLD = Double.parseDouble(WorkPropertiesUtils.get("nebulagraph_default_similarity_threshold", "0.7"));

    /**
     * 默认批处理大小
     */
    public static int DEFAULT_BATCH_SIZE = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_default_batch_size", "100"));

    /**
     * 默认最大缓存大小
     */
    public static int DEFAULT_MAX_CACHE_SIZE = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_default_max_cache_size", "10000"));

    /**
     * 默认连接超时时间（毫秒）
     */
    public static int DEFAULT_TIMEOUT_MS = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_default_timeout_ms", "30000"));

    /**
     * 默认连接池大小
     */
    public static int DEFAULT_CONNECTION_POOL_SIZE = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_default_connection_pool_size", "10"));

    /**
     * 默认连接重试次数
     */
    public static int DEFAULT_RETRY_COUNT = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_default_retry_count", "3"));

    /**
     * 默认重试间隔时间（毫秒）
     */
    public static int DEFAULT_RETRY_INTERVAL_MS = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_default_retry_interval_ms", "1000"));

    /**
     * 默认会话超时时间（毫秒）
     */
    public static int DEFAULT_SESSION_TIMEOUT_MS = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_default_session_timeout_ms", "60000"));

    /**
     * 默认空闲时间（毫秒）
     */
    public static int DEFAULT_IDLE_TIME_MS = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_default_idle_time_ms", "180000"));

    /**
     * 默认连接健康检查间隔（毫秒）
     */
    public static int DEFAULT_HEALTH_CHECK_INTERVAL_MS = Integer.parseInt(WorkPropertiesUtils.get("nebulagraph_default_health_check_interval_ms", "60000"));

    /**
     * 默认是否启用SSL
     */
    public static boolean DEFAULT_ENABLE_SSL = Boolean.parseBoolean(WorkPropertiesUtils.get("nebulagraph_default_enable_ssl", "false"));

    /**
     * 默认是否启用压缩
     */
    public static boolean DEFAULT_ENABLE_COMPRESSION = Boolean.parseBoolean(WorkPropertiesUtils.get("nebulagraph_default_enable_compression", "false"));

    /**
     * 默认日志级别
     */
    public static String DEFAULT_LOG_LEVEL = WorkPropertiesUtils.get("nebulagraph_default_log_level", "INFO");

    /**
     * 默认是否启用性能监控
     */
    public static boolean DEFAULT_ENABLE_METRICS = Boolean.parseBoolean(WorkPropertiesUtils.get("nebulagraph_default_enable_metrics", "true"));

    /**
     * 验证配置参数
     */
    public static void validateConfiguration() {
        if (NEBULAGRAPH_META_HOST == null || NEBULAGRAPH_META_HOST.trim().isEmpty()) {
            throw new IllegalArgumentException("NebulaGraph meta host cannot be null or empty");
        }
        
        if (NEBULAGRAPH_GRAPH_HOST == null || NEBULAGRAPH_GRAPH_HOST.trim().isEmpty()) {
            throw new IllegalArgumentException("NebulaGraph graph host cannot be null or empty");
        }
        
        if (NEBULAGRAPH_STORAGE_HOST == null || NEBULAGRAPH_STORAGE_HOST.trim().isEmpty()) {
            throw new IllegalArgumentException("NebulaGraph storage host cannot be null or empty");
        }
        
        if (NEBULAGRAPH_USERNAME == null || NEBULAGRAPH_USERNAME.trim().isEmpty()) {
            throw new IllegalArgumentException("NebulaGraph username cannot be null or empty");
        }
        
        if (NEBULAGRAPH_PASSWORD == null) {
            throw new IllegalArgumentException("NebulaGraph password cannot be null");
        }
        
        if (NEBULAGRAPH_META_PORT <= 0 || NEBULAGRAPH_META_PORT > 65535) {
            throw new IllegalArgumentException("NebulaGraph meta port must be between 1 and 65535");
        }
        
        if (NEBULAGRAPH_GRAPH_PORT <= 0 || NEBULAGRAPH_GRAPH_PORT > 65535) {
            throw new IllegalArgumentException("NebulaGraph graph port must be between 1 and 65535");
        }
        
        if (NEBULAGRAPH_STORAGE_PORT <= 0 || NEBULAGRAPH_STORAGE_PORT > 65535) {
            throw new IllegalArgumentException("NebulaGraph storage port must be between 1 and 65535");
        }
        
        if (DEFAULT_SPACE_NAME == null || DEFAULT_SPACE_NAME.trim().isEmpty()) {
            throw new IllegalArgumentException("Default space name cannot be null or empty");
        }
        
        if (DEFAULT_TAG_NAME == null || DEFAULT_TAG_NAME.trim().isEmpty()) {
            throw new IllegalArgumentException("Default tag name cannot be null or empty");
        }
        
        if (DEFAULT_VECTOR_DIMENSION <= 0) {
            throw new IllegalArgumentException("Default vector dimension must be positive");
        }
        
        if (DEFAULT_SIMILARITY_THRESHOLD < 0.0 || DEFAULT_SIMILARITY_THRESHOLD > 1.0) {
            throw new IllegalArgumentException("Default similarity threshold must be between 0.0 and 1.0");
        }
        
        if (DEFAULT_BATCH_SIZE <= 0) {
            throw new IllegalArgumentException("Default batch size must be positive");
        }
        
        if (DEFAULT_MAX_CACHE_SIZE <= 0) {
            throw new IllegalArgumentException("Default max cache size must be positive");
        }
        
        if (DEFAULT_TIMEOUT_MS <= 0) {
            throw new IllegalArgumentException("Default timeout must be positive");
        }
        
        if (DEFAULT_CONNECTION_POOL_SIZE <= 0) {
            throw new IllegalArgumentException("Default connection pool size must be positive");
        }
        
        if (DEFAULT_RETRY_COUNT < 0) {
            throw new IllegalArgumentException("Default retry count must be non-negative");
        }
        
        if (DEFAULT_RETRY_INTERVAL_MS < 0) {
            throw new IllegalArgumentException("Default retry interval must be non-negative");
        }
    }

}
