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
package com.alibaba.langengine.clickhouse.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.clickhouse.ClickHouseConfiguration;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

import static com.alibaba.langengine.clickhouse.ClickHouseConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class ClickHouse extends VectorStore {

    private Embeddings embedding;

    private final ClickHouseService clickHouseService;
    private final ClickHouseParam clickHouseParam;

    /**
     * 构造函数 - 使用默认参数
     */
    public ClickHouse() {
        this(null, null, null, null, null);
    }

    /**
     * 构造函数 - 指定数据库
     */
    public ClickHouse(String database) {
        this(null, null, null, database, null);
    }

    /**
     * 构造函数 - 指定数据库和参数
     */
    public ClickHouse(String database, ClickHouseParam clickHouseParam) {
        this(null, null, null, database, clickHouseParam);
    }

    /**
     * 构造函数 - 完整参数
     * 
     * 注意：硬编码的默认值（如密码"password"）仅用于本地开发和测试环境。
     * 生产环境中必须通过环境变量或配置文件提供正确的连接参数。
     * 
     * @param url ClickHouse连接URL，如果为空则使用配置文件中的值或默认值
     * @param username 用户名，如果为空则使用配置文件中的值或默认值
     * @param password 密码，如果为空则使用配置文件中的值。生产环境中不应使用默认密码
     * @param database 数据库名称，如果为空则使用配置文件中的值或默认值
     * @param clickHouseParam ClickHouse参数配置，如果为空则使用默认配置
     */
    public ClickHouse(String url, String username, String password, String database, ClickHouseParam clickHouseParam) {
        // 使用配置文件中的默认值
        String effectiveUrl = StringUtils.defaultIfEmpty(url, CLICKHOUSE_URL);
        String effectiveUsername = StringUtils.defaultIfEmpty(username, CLICKHOUSE_USERNAME);
        String effectivePassword = StringUtils.defaultIfEmpty(password, CLICKHOUSE_PASSWORD);
        String effectiveDatabase = StringUtils.defaultIfEmpty(database, CLICKHOUSE_DATABASE);
        
        // 设置默认值
        if (StringUtils.isEmpty(effectiveUrl)) {
            effectiveUrl = "jdbc:clickhouse://localhost:8123";
        }
        if (StringUtils.isEmpty(effectiveUsername)) {
            effectiveUsername = "default";
        }
        if (StringUtils.isEmpty(effectivePassword)) {
            // 在生产环境中，应该通过环境变量或配置文件提供密码
            log.warn("Using default password for ClickHouse connection. This should only be used for local development/testing. " +
                    "Please set clickhouse_password in configuration or environment variables for production use.");
            effectivePassword = "";
        }
        if (StringUtils.isEmpty(effectiveDatabase)) {
            effectiveDatabase = "default";
        }

        this.clickHouseParam = clickHouseParam != null ? clickHouseParam : new ClickHouseParam();
        this.clickHouseService = new ClickHouseService(effectiveUrl, effectiveUsername, effectivePassword, effectiveDatabase, this.clickHouseParam);
        
        log.info("ClickHouse Vector Store initialized with URL: {}, Database: {}", effectiveUrl, effectiveDatabase);
    }

    /**
     * 初始化向量存储
     * 
     * 此方法会：
     * 1. 创建数据库（如果不存在）
     * 2. 根据embedding模型结果维度创建向量表
     * 3. 创建必要的索引以优化查询性能
     */
    public void init() {
        try {
            clickHouseService.init(embedding);
            log.info("ClickHouse Vector Store initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize ClickHouse Vector Store", e);
            throw new RuntimeException("Failed to initialize ClickHouse Vector Store", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        
        // 为没有uniqueId的文档生成UUID
        for (Document document : documents) {
            if (StringUtils.isEmpty(document.getUniqueId())) {
                document.setUniqueId(UUID.randomUUID().toString());
            }
        }
        
        // 使用embedding模型生成向量
        documents = embedding.embedDocument(documents);
        
        // 添加到ClickHouse
        clickHouseService.addDocuments(documents);
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isEmpty(query)) {
            return Lists.newArrayList();
        }
        
        // 使用embedding模型生成查询向量
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
            return Lists.newArrayList();
        }
        
        // 解析向量
        List<Float> queryVector = JSON.parseArray(embeddingStrings.get(0), Float.class);
        
        // 执行相似性搜索
        return clickHouseService.similaritySearch(queryVector, k, maxDistanceValue, type);
    }

    /**
     * 获取文档数量
     */
    public long getDocumentCount() {
        return clickHouseService.getDocumentCount();
    }

    /**
     * 清空所有文档
     */
    public void clearDocuments() {
        clickHouseService.clearDocuments();
    }

    /**
     * 删除表
     */
    public void dropTable() {
        clickHouseService.dropTable();
    }

    /**
     * 检查健康状态
     */
    public boolean isHealthy() {
        return clickHouseService.isHealthy();
    }

    /**
     * 关闭资源
     */
    public void close() {
        if (clickHouseService != null) {
            clickHouseService.close();
        }
    }
}
