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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("test")
public class TimescaleDBTestConfiguration {
    
    /**
     * 测试数据库URL
     */
    public static final String TEST_DATABASE_URL = "localhost:5432";
    
    /**
     * 测试数据库名
     */
    public static final String TEST_DATABASE_NAME = "test_timescaledb";
    
    /**
     * 测试用户名
     */
    public static final String TEST_USERNAME = "test_user";
    
    /**
     * 测试密码
     */
    public static final String TEST_PASSWORD = "test_password";
    
    /**
     * 测试表名
     */
    public static final String TEST_TABLE_NAME = "test_vector_store";
    
    /**
     * 测试向量维度
     */
    public static final int TEST_VECTOR_DIMENSION = 1536;
    
    /**
     * 测试批次大小
     */
    public static final int TEST_BATCH_SIZE = 50;
    
    /**
     * 测试相似度阈值
     */
    public static final double TEST_SIMILARITY_THRESHOLD = 0.8;
    
    /**
     * 测试缓存大小
     */
    public static final int TEST_CACHE_SIZE = 100;
    
    /**
     * 测试连接超时时间
     */
    public static final int TEST_CONNECTION_TIMEOUT = 10000;
    
    /**
     * 测试查询超时时间
     */
    public static final int TEST_QUERY_TIMEOUT = 30000;
    
    /**
     * 测试最大连接数
     */
    public static final int TEST_MAX_CONNECTIONS = 10;
    
    /**
     * 测试初始连接数
     */
    public static final int TEST_INITIAL_CONNECTIONS = 2;
    
    /**
     * 测试分块时间间隔（天）
     */
    public static final int TEST_CHUNK_TIME_INTERVAL = 7;
    
    @Bean
    public TimescaleDBConfiguration timescaleDBConfiguration() {
        return new TimescaleDBConfiguration();
    }
}
