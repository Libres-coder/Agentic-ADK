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
package com.alibaba.langengine.neo4j.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

import static com.alibaba.langengine.neo4j.Neo4jConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Neo4j extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * Neo4j服务实例
     */
    private final Neo4jService neo4jService;

    /**
     * 参数配置
     */
    private final Neo4jParam neo4jParam;

    /**
     * 构造函数 - 使用默认参数
     */
    public Neo4j() {
        this(null, null, null, null, null);
    }

    /**
     * 构造函数 - 指定基本连接信息
     */
    public Neo4j(String uri, String username, String password) {
        this(uri, username, password, null, null);
    }

    /**
     * 构造函数 - 指定连接信息和数据库
     */
    public Neo4j(String uri, String username, String password, String database) {
        this(uri, username, password, database, null);
    }

    /**
     * 构造函数 - 完整参数
     *
     * 注意：硬编码的默认值（如密码"password"）仅用于本地开发和测试环境。
     * 生产环境中必须通过环境变量或配置文件提供正确的连接参数。
     *
     * @param uri Neo4j连接URI，如果为空则使用配置文件中的值或默认值
     * @param username 用户名，如果为空则使用配置文件中的值或默认值
     * @param password 密码，如果为空则使用配置文件中的值。生产环境中不应使用默认密码
     * @param database 数据库名称，如果为空则使用配置文件中的值或默认值
     * @param neo4jParam Neo4j参数配置，如果为空则使用默认配置
     */
    public Neo4j(String uri, String username, String password, String database, Neo4jParam neo4jParam) {
        // 使用配置文件中的默认值
        String effectiveUri = StringUtils.defaultIfEmpty(uri, NEO4J_URI);
        String effectiveUsername = StringUtils.defaultIfEmpty(username, NEO4J_USERNAME);
        String effectivePassword = StringUtils.defaultIfEmpty(password, NEO4J_PASSWORD);
        String effectiveDatabase = StringUtils.defaultIfEmpty(database, NEO4J_DATABASE);

        // 设置默认值
        if (StringUtils.isEmpty(effectiveUri)) {
            effectiveUri = "bolt://localhost:7687";
        }
        if (StringUtils.isEmpty(effectiveUsername)) {
            effectiveUsername = "neo4j";
        }
        if (StringUtils.isEmpty(effectivePassword)) {
            // 在生产环境中，应该通过环境变量或配置文件提供密码
            log.warn("Using default password for Neo4j connection. This should only be used for local development/testing. " +
                    "Please set neo4j_password in configuration or environment variables for production use.");
            effectivePassword = "password";
        }
        if (StringUtils.isEmpty(effectiveDatabase)) {
            effectiveDatabase = "neo4j";
        }

        this.neo4jParam = neo4jParam != null ? neo4jParam : new Neo4jParam();
        this.neo4jService = new Neo4jService(effectiveUri, effectiveUsername, effectivePassword, effectiveDatabase, this.neo4jParam);

        log.info("Neo4j Vector Store initialized with URI: {}, Database: {}", effectiveUri, effectiveDatabase);
    }

    /**
     * 初始化向量存储
     * 创建必要的索引和约束
     */
    public void init() {
        try {
            neo4jService.init(embedding);
            log.info("Neo4j vector store initialization completed");
        } catch (Exception e) {
            log.error("Failed to initialize Neo4j vector store", e);
            throw new RuntimeException("Failed to initialize Neo4j vector store", e);
        }
    }

    /**
     * 添加文档到向量存储
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            // 为文档生成向量嵌入
            documents = embedding.embedDocument(documents);
            
            // 为没有唯一ID的文档生成ID
            for (Document document : documents) {
                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }
            }
            
            // 添加到Neo4j
            neo4jService.addDocuments(documents);
            
            log.info("Successfully added {} documents to Neo4j vector store", documents.size());
        } catch (Exception e) {
            log.error("Failed to add documents to Neo4j vector store", e);
            throw new RuntimeException("Failed to add documents to Neo4j vector store", e);
        }
    }

    /**
     * 相似性搜索
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        try {
            // 生成查询向量
            List<String> embeddingStrings = embedding.embedQuery(query, k);
            if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
                log.warn("Failed to generate embedding for query: {}", query);
                return Lists.newArrayList();
            }

            // 解析向量
            List<Float> queryEmbedding = JSON.parseArray(embeddingStrings.get(0), Float.class);
            
            // 执行相似性搜索
            List<Document> results = neo4jService.similaritySearch(queryEmbedding, k);
            
            // 应用距离过滤
            if (maxDistanceValue != null) {
                results = results.stream()
                        .filter(doc -> doc.getScore() != null && doc.getScore() <= maxDistanceValue)
                        .collect(Lists::newArrayList, List::add, List::addAll);
            }
            
            log.info("Similarity search completed. Query: {}, Results: {}", query, results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Failed to perform similarity search for query: {}", query, e);
            throw new RuntimeException("Failed to perform similarity search", e);
        }
    }

    /**
     * 获取文档数量
     */
    public long getDocumentCount() {
        return neo4jService.getDocumentCount();
    }

    /**
     * 清空所有文档
     */
    public void clearDocuments() {
        neo4jService.clearDocuments();
    }

    /**
     * 检查连接健康状态
     */
    public boolean isHealthy() {
        return neo4jService.isHealthy();
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (neo4jService != null) {
            neo4jService.close();
        }
    }
}
