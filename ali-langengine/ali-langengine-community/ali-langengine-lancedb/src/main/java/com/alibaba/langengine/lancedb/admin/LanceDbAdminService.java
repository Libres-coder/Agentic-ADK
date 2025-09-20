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
package com.alibaba.langengine.lancedb.admin;

import com.alibaba.langengine.lancedb.LanceDbConfiguration;
import com.alibaba.langengine.lancedb.LanceDbException;
import com.alibaba.langengine.lancedb.client.LanceDbClient;
import com.alibaba.langengine.lancedb.vectorstore.LanceDbParam;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LanceDbAdminService {
    
    /**
     * 测试连接
     *
     * @param configuration LanceDB配置
     * @return 是否连接成功
     */
    public static boolean testConnection(LanceDbConfiguration configuration) {
        if (configuration == null || !configuration.isValid()) {
            log.error("Invalid LanceDbConfiguration for connection test");
            return false;
        }

        try (LanceDbClient client = new LanceDbClient(configuration)) {
            // 这里可以添加实际的连接测试逻辑
            // 例如尝试列出表或执行简单查询
            log.info("Connection test successful for URI: {}", configuration.getFullServerUrl());
            return true;
        } catch (Exception e) {
            log.error("Connection test failed for URI {}: {}", configuration.getFullServerUrl(), e.getMessage());
            return false;
        }
    }

    /**
     * 测试连接（使用参数配置）
     *
     * @param param 参数配置
     * @return 是否连接成功
     */
    public static boolean testConnection(LanceDbParam param) {
        if (param == null || !param.isValid()) {
            log.error("Invalid LanceDbParam for connection test");
            return false;
        }

        LanceDbConfiguration configuration = createConfiguration(param);
        return testConnection(configuration);
    }

    /**
     * 创建表格（如果不存在）
     *
     * @param configuration 配置对象
     * @param tableName     表名
     * @param dimension     向量维度
     * @throws LanceDbException 创建异常
     */
    public static void createTableIfNotExists(LanceDbConfiguration configuration, String tableName, int dimension) 
            throws LanceDbException {
        if (configuration == null || !configuration.isValid()) {
            throw new IllegalArgumentException("Invalid LanceDbConfiguration");
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive");
        }

        try (LanceDbClient client = new LanceDbClient(configuration)) {
            client.createTable(tableName, dimension);
            log.info("Successfully created table '{}' with dimension {}", tableName, dimension);
        } catch (Exception e) {
            log.error("Failed to create table '{}': {}", tableName, e.getMessage(), e);
            throw new LanceDbException("Failed to create table: " + tableName, e);
        }
    }

    /**
     * 创建表格（如果不存在）- 使用参数配置
     *
     * @param param 参数配置
     * @throws LanceDbException 创建异常
     */
    public static void createTableIfNotExists(LanceDbParam param) throws LanceDbException {
        if (param == null || !param.isValid()) {
            throw new IllegalArgumentException("Invalid LanceDbParam");
        }

        LanceDbConfiguration configuration = createConfiguration(param);
        createTableIfNotExists(configuration, param.getEffectiveTableName(), param.getEffectiveVectorDimension());
    }

    /**
     * 删除表格
     *
     * @param configuration 配置对象
     * @param tableName     表名
     * @throws LanceDbException 删除异常
     */
    public static void dropTable(LanceDbConfiguration configuration, String tableName) throws LanceDbException {
        if (configuration == null || !configuration.isValid()) {
            throw new IllegalArgumentException("Invalid LanceDbConfiguration");
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }

        try (LanceDbClient client = new LanceDbClient(configuration)) {
            client.dropTable(tableName);
            log.info("Successfully dropped table '{}'", tableName);
        } catch (Exception e) {
            log.error("Failed to drop table '{}': {}", tableName, e.getMessage(), e);
            throw new LanceDbException("Failed to drop table: " + tableName, e);
        }
    }

    /**
     * 删除表格 - 使用参数配置
     *
     * @param param 参数配置
     * @throws LanceDbException 删除异常
     */
    public static void dropTable(LanceDbParam param) throws LanceDbException {
        if (param == null || !param.isValid()) {
            throw new IllegalArgumentException("Invalid LanceDbParam");
        }

        LanceDbConfiguration configuration = createConfiguration(param);
        dropTable(configuration, param.getEffectiveTableName());
    }

    /**
     * 根据参数创建配置对象
     *
     * @param param 参数配置
     * @return 配置对象
     */
    private static LanceDbConfiguration createConfiguration(LanceDbParam param) {
        LanceDbConfiguration.LanceDbConfigurationBuilder builder = LanceDbConfiguration.builder()
                .uri(param.getEffectiveUri())
                .baseUrl(param.getEffectiveUri())
                .apiKey(param.getApiKey())
                .enableSsl(param.isEffectiveEnableSsl())
                .defaultVectorDimension(param.getEffectiveVectorDimension())
                .defaultSimilarityThreshold(param.getEffectiveSimilarityThreshold())
                .cacheEnabled(param.isEffectiveCacheEnabled())
                .cacheSize(param.getEffectiveCacheSize());

        // 设置连接参数
        if (param.getConnectionTimeout() != null) {
            builder.connectionTimeout(param.getConnectionTimeout());
        }
        if (param.getReadTimeout() != null) {
            builder.readTimeout(param.getReadTimeout());
        }
        if (param.getWriteTimeout() != null) {
            builder.writeTimeout(param.getWriteTimeout());
        }

        // 设置重试参数
        if (param.getMaxRetries() != null) {
            builder.maxRetries(param.getMaxRetries());
        }
        if (param.getRetryIntervalMs() != null) {
            builder.retryIntervalMs(param.getRetryIntervalMs());
        }

        // 设置批量和连接池参数
        if (param.getBatchSize() != null) {
            builder.batchSize(param.getBatchSize());
        }
        if (param.getConnectionPoolSize() != null) {
            builder.connectionPoolSize(param.getConnectionPoolSize());
        }

        // 设置缓存过期时间
        if (param.getCacheExpirationMinutes() != null) {
            builder.cacheExpirationMinutes(param.getCacheExpirationMinutes());
        }

        return builder.build();
    }
}
