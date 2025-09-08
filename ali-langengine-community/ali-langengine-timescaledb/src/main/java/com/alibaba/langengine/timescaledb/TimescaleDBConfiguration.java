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
package com.alibaba.langengine.timescaledb;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class TimescaleDBConfiguration {
    
    /**
     * TimescaleDB连接URL
     */
    public static String TIMESCALEDB_URL = WorkPropertiesUtils.get("timescaledb_url");
    
    /**
     * TimescaleDB用户名
     */
    public static String TIMESCALEDB_USERNAME = WorkPropertiesUtils.get("timescaledb_username");
    
    /**
     * TimescaleDB密码
     */
    public static String TIMESCALEDB_PASSWORD = WorkPropertiesUtils.get("timescaledb_password");
    
    /**
     * TimescaleDB数据库名
     */
    public static String TIMESCALEDB_DATABASE = WorkPropertiesUtils.get("timescaledb_database");
    
    /**
     * 默认表名
     */
    public static final String DEFAULT_TABLE_NAME = "vector_store";
    
    /**
     * 默认向量维度
     */
    public static final int DEFAULT_VECTOR_DIMENSION = 1536;
    
    /**
     * 默认批次大小
     */
    public static final int DEFAULT_BATCH_SIZE = 100;
    
    /**
     * 默认相似度阈值
     */
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.8;
    
    /**
     * 默认最大连接数
     */
    public static final int DEFAULT_MAX_CONNECTIONS = 20;
    
    /**
     * 默认初始连接数
     */
    public static final int DEFAULT_INITIAL_CONNECTIONS = 5;
    
    /**
     * 默认连接超时时间(毫秒)
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    
    /**
     * 默认查询超时时间(毫秒)
     */
    public static final int DEFAULT_QUERY_TIMEOUT = 60000;
    
    /**
     * 默认缓存大小
     */
    public static final int DEFAULT_CACHE_SIZE = 1000;
    
    /**
     * 时间序列表名后缀
     */
    public static final String TIMESERIES_TABLE_SUFFIX = "_timeseries";
    
    /**
     * 超表分区间隔（天）
     */
    public static final int DEFAULT_CHUNK_TIME_INTERVAL = 7;
}
