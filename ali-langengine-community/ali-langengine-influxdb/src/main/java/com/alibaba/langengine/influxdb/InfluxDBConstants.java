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
     * InfluxDB服务器URL
     */
    public static final String INFLUXDB_URL = System.getProperty("influxdb.url", 
            System.getenv("INFLUXDB_URL") != null ? System.getenv("INFLUXDB_URL") : "http://localhost:8086");

    /**
     * InfluxDB访问令牌
     */
    public static final String INFLUXDB_TOKEN = System.getProperty("influxdb.token", 
            System.getenv("INFLUXDB_TOKEN"));

    /**
     * InfluxDB组织名称
     */
    public static final String INFLUXDB_ORG = System.getProperty("influxdb.org", 
            System.getenv("INFLUXDB_ORG") != null ? System.getenv("INFLUXDB_ORG") : "my-org");

    /**
     * InfluxDB存储桶名称
     */
    public static final String INFLUXDB_BUCKET = System.getProperty("influxdb.bucket", 
            System.getenv("INFLUXDB_BUCKET") != null ? System.getenv("INFLUXDB_BUCKET") : "vectors");

    /**
     * 批量写入大小
     */
    public static final int BATCH_SIZE = Integer.parseInt(
            System.getProperty("influxdb.batch.size", "1000"));

    /**
     * 默认向量维度
     */
    public static final int DEFAULT_VECTOR_DIMENSION = Integer.parseInt(
            System.getProperty("influxdb.vector.dimension", "1536"));

    /**
     * 默认相似度阈值
     */
    public static final double DEFAULT_SIMILARITY_THRESHOLD = Double.parseDouble(
            System.getProperty("influxdb.similarity.threshold", "0.7"));

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

    /**
     * 默认精度
     */
    public static final String DEFAULT_PRECISION = "ns";

    /**
     * 刷新间隔（毫秒）
     */
    public static final int FLUSH_INTERVAL = Integer.parseInt(
            System.getProperty("influxdb.flush.interval", "1000"));

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
    public static final int RETRY_INTERVAL = 1000;
}
