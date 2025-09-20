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
package com.alibaba.langengine.lancedb.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.lancedb.LanceDbConfiguration;
import com.alibaba.langengine.lancedb.LanceDbException;
import com.alibaba.langengine.lancedb.admin.LanceDbAdminService;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LanceDbService {

    /**
     * 验证输入参数
     *
     * @param embeddings 嵌入模型
     * @param param      参数配置
     * @throws IllegalArgumentException 参数验证失败
     */
    private static void validateInputs(Embeddings embeddings, LanceDbParam param) {
        if (embeddings == null) {
            throw new IllegalArgumentException("Embeddings cannot be null");
        }
        if (param == null) {
            throw new IllegalArgumentException("LanceDbParam cannot be null");
        }
        if (!param.isValid()) {
            throw new IllegalArgumentException("Invalid LanceDbParam configuration");
        }
    }

    /**
     * 创建LanceDB向量存储实例
     *
     * @param embeddings Embeddings实例
     * @param param      参数配置
     * @return LanceDB向量存储实例
     * @throws LanceDbException 创建异常
     */
    public static LanceDbVectorStore createVectorStore(Embeddings embeddings, LanceDbParam param) throws LanceDbException {
        validateInputs(embeddings, param);

        try {
            // 创建配置对象
            LanceDbConfiguration configuration = createConfiguration(param);
            
            // 创建向量存储实例
            LanceDbVectorStore vectorStore = new LanceDbVectorStore(
                    embeddings,
                    configuration,
                    param.getEffectiveTableName(),
                    param.getEffectiveVectorDimension(),
                    param.getEffectiveCacheSize(),
                    param.getEffectiveSimilarityThreshold()
            );

            log.info("Successfully created LanceDB vector store for table: {}", param.getEffectiveTableName());
            return vectorStore;

        } catch (Exception e) {
            log.error("Failed to create LanceDB vector store: {}", e.getMessage(), e);
            throw new LanceDbException("Failed to create LanceDB vector store", e);
        }
    }

    /**
     * 创建简单的LanceDB向量存储实例
     *
     * @param embeddings Embeddings实例
     * @param tableName  表名
     * @return LanceDB向量存储实例
     * @throws LanceDbException 创建异常
     */
    public static LanceDbVectorStore createSimpleVectorStore(Embeddings embeddings, String tableName) throws LanceDbException {
        LanceDbParam param = LanceDbParam.localParams(tableName);
        return createVectorStore(embeddings, param);
    }

    /**
     * 创建生产环境的LanceDB向量存储实例
     *
     * @param embeddings Embeddings实例
     * @param uri        服务器URI
     * @param apiKey     API密钥
     * @param tableName  表名
     * @return LanceDB向量存储实例
     * @throws LanceDbException 创建异常
     */
    public static LanceDbVectorStore createProductionVectorStore(Embeddings embeddings, 
                                                                String uri, 
                                                                String apiKey, 
                                                                String tableName) throws LanceDbException {
        LanceDbParam param = LanceDbParam.productionParams(uri, apiKey, tableName);
        return createVectorStore(embeddings, param);
    }

    /**
     * 测试连接
     * 
     * @param param 参数配置
     * @return 是否连接成功
     * @deprecated 使用 {@link LanceDbAdminService#testConnection(LanceDbParam)} 替代
     */
    @Deprecated
    public static boolean testConnection(LanceDbParam param) {
        return LanceDbAdminService.testConnection(param);
    }

    /**
     * 创建表格（如果不存在）
     *
     * @param param 参数配置
     * @throws LanceDbException 创建异常
     * @deprecated 使用 {@link LanceDbAdminService#createTableIfNotExists(LanceDbParam)} 替代
     */
    @Deprecated
    public static void createTableIfNotExists(LanceDbParam param) throws LanceDbException {
        LanceDbAdminService.createTableIfNotExists(param);
    }

    /**
     * 删除表格
     *
     * @param param 参数配置
     * @throws LanceDbException 删除异常
     * @deprecated 使用 {@link LanceDbAdminService#dropTable(LanceDbParam)} 替代
     */
    @Deprecated
    public static void dropTable(LanceDbParam param) throws LanceDbException {
        LanceDbAdminService.dropTable(param);
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
