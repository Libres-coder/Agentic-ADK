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
package com.alibaba.langengine.influxdb;



public final class InfluxDBConstants {

    private InfluxDBConstants() {
        // 工具类，禁止实例化
    }

    
    /**
     * 默认InfluxDB服务器URL
     */
    public static final String DEFAULT_URL = "http://localhost:8086";

    /**
     * 默认组织名称
     */
    public static final String DEFAULT_ORG = "default-org";

    /**
     * 默认存储桶名称
     */
    public static final String DEFAULT_BUCKET = "default-bucket";

    /**
     * 默认批量写入大小
     */
    public static final int DEFAULT_BATCH_SIZE = 100;

    /**
     * 默认向量维度
     */
    public static final int DEFAULT_VECTOR_DIMENSION = 1536;

    /**
     * 默认相似度阈值
     */
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;

    /**
     * 默认TopK值
     */
    public static final int DEFAULT_TOP_K = 10;

    /**
     * 默认缓存大小
     */
    public static final int DEFAULT_CACHE_SIZE = 1000;

    /**
     * 默认缓存TTL（毫秒）
     */
    public static final int DEFAULT_CACHE_TTL_MS = 300000; // 5分钟

    /**
     * 默认连接超时（毫秒）
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 60000;

    /**
     * 默认读取超时（毫秒）
     */
    public static final int DEFAULT_READ_TIMEOUT_MS = 60000;

    /**
     * 默认写入超时（毫秒）
     */
    public static final int DEFAULT_WRITE_TIMEOUT_MS = 60000;

    // =========================== 字段名常量 ===========================

    // =========================== 字段名常量 ===========================

    /**
     * 默认测量名称
     */
    public static final String DEFAULT_MEASUREMENT = "vector_data";

    /**
     * 默认标签字段名
     */
    public static final String DEFAULT_TAG_FIELD = "doc_id";

    /**
     * 向量字段名
     */
    public static final String VECTOR_FIELD = "vector";

    /**
     * 内容字段名
     */
    public static final String CONTENT_FIELD = "content";

    /**
     * 元数据字段名前缀
     */
    public static final String METADATA_FIELD_PREFIX = "meta_";

    // =========================== 查询相关常量 ===========================

    /**
     * 查询结果限制倍数
     * 在查询时使用limit * QUERY_LIMIT_MULTIPLIER以获取更多候选结果进行后处理
     */
    public static final int QUERY_LIMIT_MULTIPLIER = 2;

    // =========================== 性能相关常量 ===========================

    /**
     * 默认精度
     */
    public static final String DEFAULT_PRECISION = "ns";

    /**
     * 刷新间隔（毫秒）
     */
    public static final int DEFAULT_FLUSH_INTERVAL_MS = 1000;

    /**
     * 是否启用压缩
     */
    public static final boolean ENABLE_GZIP = true;

    /**
     * 最大重试次数
     */
    public static final int MAX_RETRIES = 3;

    /**
     * 重试间隔（毫秒）
     */
    public static final int RETRY_INTERVAL_MS = 1000;

    // =========================== 配置键名常量 ===========================

    /**
     * 系统属性和环境变量键名
     */
    public static final class ConfigKeys {
        public static final String URL_PROPERTY = "influxdb.url";
        public static final String URL_ENV = "INFLUXDB_URL";
        
        public static final String TOKEN_PROPERTY = "influxdb.token";
        public static final String TOKEN_ENV = "INFLUXDB_TOKEN";
        
        public static final String ORG_PROPERTY = "influxdb.org";
        public static final String ORG_ENV = "INFLUXDB_ORG";
        
        public static final String BUCKET_PROPERTY = "influxdb.bucket";
        public static final String BUCKET_ENV = "INFLUXDB_BUCKET";
        
        public static final String VECTOR_DIMENSION_PROPERTY = "influxdb.vector.dimension";
        public static final String VECTOR_DIMENSION_ENV = "INFLUXDB_VECTOR_DIMENSION";
        
        public static final String CACHE_SIZE_PROPERTY = "influxdb.cache.size";
        public static final String CACHE_SIZE_ENV = "INFLUXDB_CACHE_SIZE";
        
        private ConfigKeys() {
            // 禁止实例化
        }
    }
}
