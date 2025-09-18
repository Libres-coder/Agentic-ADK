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
package com.alibaba.langengine.nebulagraph.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.nebulagraph.client.NebulaGraphVectorClient;
import com.alibaba.langengine.nebulagraph.exception.NebulaGraphVectorStoreException;
import com.alibaba.langengine.nebulagraph.model.NebulaGraphQueryRequest;
import com.alibaba.langengine.nebulagraph.model.NebulaGraphQueryResponse;
import com.alibaba.langengine.nebulagraph.model.NebulaGraphVector;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * NebulaGraph向量库
 * NebulaGraph是一个开源的分布式图数据库，支持向量存储和相似度搜索
 * 官方文档：https://docs.nebula-graph.io/
 *
 * @author langengine
 */
@Slf4j
@Data
public class NebulaGraph extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * 图空间名称
     * 类似于数据库中的database概念，用于隔离不同的数据集
     */
    private String spaceName;

    /**
     * 标签名称
     * 定义节点的类型和属性结构，默认为"Document"
     */
    private String tagName;

    /**
     * NebulaGraph配置参数
     */
    private NebulaGraphParam param;

    /**
     * 内部使用的客户端，不希望对外暴露
     */
    private NebulaGraphVectorClient _client;

    /**
     * 内部使用的服务实例，不希望对外暴露
     */
    private NebulaGraphService _service;

    /**
     * 默认构造函数
     *
     * @param embedding 嵌入模型
     * @param spaceName 图空间名称
     */
    public NebulaGraph(Embeddings embedding, String spaceName) {
        this(embedding, spaceName, "Document", NebulaGraphParam.createDefault());
    }

    /**
     * 完整构造函数
     *
     * @param embedding 嵌入模型
     * @param spaceName 图空间名称
     * @param tagName 标签名称
     * @param param 配置参数
     */
    public NebulaGraph(Embeddings embedding, String spaceName, String tagName, NebulaGraphParam param) {
        this.embedding = embedding;
        this.spaceName = spaceName != null ? spaceName : "langengine_" + UUID.randomUUID().toString().replace("-", "");
        this.tagName = tagName != null ? tagName : "Document";
        this.param = param != null ? param : NebulaGraphParam.createDefault();

        try {
            // 初始化客户端
            this._client = new NebulaGraphVectorClient(this.param);

            // 初始化服务
            this._service = new NebulaGraphService(this._client, this.spaceName, this.tagName, this.param);

            // 初始化图空间和标签
            this._service.init();

            log.info("NebulaGraph vector store initialized: space={}, tag={}", this.spaceName, this.tagName);

        } catch (Exception e) {
            throw NebulaGraphVectorStoreException.initError(
                "Failed to initialize NebulaGraph vector store", e);
        }
    }

    /**
     * 添加文档向量，如果没有向量，系统会自动使用embedding生成向量
     *
     * @param documents 文档列表
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("No documents to add");
            return;
        }

        try {
            List<NebulaGraphVector> vectors = new ArrayList<>();

            for (Document document : documents) {
                // 确保文档有唯一ID
                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }

                // 跳过空内容
                if (StringUtils.isEmpty(document.getPageContent())) {
                    log.warn("Skipping document with empty content: {}", document.getUniqueId());
                    continue;
                }

                // 确保有元数据
                if (MapUtils.isEmpty(document.getMetadata())) {
                    document.setMetadata(new HashMap<>());
                }

                // 获取或生成向量
                List<Double> embedding = document.getEmbedding();
                if (CollectionUtils.isEmpty(embedding) && this.embedding != null) {
                    // 使用embedding模型生成向量
                    List<Document> embeddedDocs = this.embedding.embedTexts(List.of(document.getPageContent()));
                    if (CollectionUtils.isNotEmpty(embeddedDocs)) {
                        embedding = embeddedDocs.get(0).getEmbedding();
                        document.setEmbedding(embedding);
                    }
                }

                if (CollectionUtils.isEmpty(embedding)) {
                    log.warn("No embedding available for document: {}", document.getUniqueId());
                    continue;
                }

                // 转换为NebulaGraphVector
                NebulaGraphVector vector = convertDocumentToVector(document);
                vectors.add(vector);
            }

            if (CollectionUtils.isNotEmpty(vectors)) {
                _service.insertVectors(vectors);
                log.info("Added {} documents to NebulaGraph", vectors.size());
            }

        } catch (Exception e) {
            log.error("Failed to add documents to NebulaGraph", e);
            throw NebulaGraphVectorStoreException.insertError("Failed to add documents", e);
        }
    }

    /**
     * 相似度搜索
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @param maxDistanceValue 最大距离阈值
     * @param type 搜索类型（暂未使用）
     * @return 搜索结果文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isEmpty(query)) {
            log.warn("Empty query string");
            return new ArrayList<>();
        }

        try {
            // 生成查询向量
            List<Double> queryVector = null;
            if (this.embedding != null) {
                List<Document> embeddedDocs = this.embedding.embedTexts(List.of(query));
                if (CollectionUtils.isNotEmpty(embeddedDocs)) {
                    queryVector = embeddedDocs.get(0).getEmbedding();
                }
            }

            if (CollectionUtils.isEmpty(queryVector)) {
                throw NebulaGraphVectorStoreException.queryError("Failed to generate query vector", null);
            }

            // 构建查询请求
            NebulaGraphQueryRequest request = NebulaGraphQueryRequest.builder()
                .queryVector(queryVector)
                .topK(k)
                .similarityThreshold(maxDistanceValue != null ? maxDistanceValue : 0.0)
                .includeMetadata(true)
                .includeVector(false)
                .distanceFunction(NebulaGraphQueryRequest.DistanceFunction.COSINE)
                .build();

            // 执行查询
            NebulaGraphQueryResponse response = _service.querySimilarVectors(request);

            if (!response.isSuccess()) {
                throw NebulaGraphVectorStoreException.queryError(
                    "Query failed: " + response.getErrorMessage(), null);
            }

            // 转换结果
            List<Document> documents = response.getDocuments().stream()
                .map(this::convertVectorResultToDocument)
                .collect(Collectors.toList());

            log.info("Found {} similar documents for query", documents.size());
            return documents;

        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            if (e instanceof NebulaGraphVectorStoreException) {
                throw e;
            }
            throw NebulaGraphVectorStoreException.queryError("Similarity search failed", e);
        }
    }

    /**
     * 删除文档
     *
     * @param uniqueIds 要删除的文档唯一ID列表
     */
    public void deleteDocuments(List<String> uniqueIds) {
        if (CollectionUtils.isEmpty(uniqueIds)) {
            log.warn("No documents to delete");
            return;
        }

        try {
            _service.deleteVectors(uniqueIds);
            log.info("Deleted {} documents from NebulaGraph", uniqueIds.size());
        } catch (Exception e) {
            log.error("Failed to delete documents from NebulaGraph", e);
            throw NebulaGraphVectorStoreException.deleteError("Failed to delete documents", e);
        }
    }

    /**
     * 关闭连接和清理资源
     */
    public void close() {
        try {
            if (_client != null) {
                _client.close();
            }
            log.info("NebulaGraph vector store closed");
        } catch (Exception e) {
            log.error("Error closing NebulaGraph vector store", e);
        }
    }

    // 私有辅助方法

    /**
     * 将Document转换为NebulaGraphVector
     */
    private NebulaGraphVector convertDocumentToVector(Document document) {
        NebulaGraphVector vector = new NebulaGraphVector();
        vector.setUniqueId(document.getUniqueId());
        vector.setContent(document.getPageContent());
        vector.setVector(document.getEmbedding());
        vector.setMetadata(document.getMetadata());

        // 设置额外字段
        vector.setTitle(extractFromMetadata(document.getMetadata(), "title", ""));
        vector.setDocIndex(extractFromMetadata(document.getMetadata(), "doc_index", ""));
        vector.setDocType(extractFromMetadata(document.getMetadata(), "doc_type", ""));
        vector.setTags(extractTagsFromMetadata(document.getMetadata()));
        vector.setCustomFields(extractCustomFields(document.getMetadata()));
        vector.setCreatedAt(System.currentTimeMillis());
        vector.setUpdatedAt(System.currentTimeMillis());

        return vector;
    }

    /**
     * 将查询结果转换为Document
     */
    private Document convertVectorResultToDocument(NebulaGraphQueryResponse.DocumentResult result) {
        Document document = new Document();
        document.setUniqueId(result.getUniqueId());
        document.setPageContent(result.getContent());
        document.setScore(result.getScore());
        document.setMetadata(result.getMetadata() != null ? result.getMetadata() : new HashMap<>());

        if (CollectionUtils.isNotEmpty(result.getVector())) {
            document.setEmbedding(result.getVector());
        }

        return document;
    }

    /**
     * 从元数据中提取字符串值
     */
    private String extractFromMetadata(Map<String, Object> metadata, String key, String defaultValue) {
        if (MapUtils.isEmpty(metadata) || !metadata.containsKey(key)) {
            return defaultValue;
        }
        Object value = metadata.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * 从元数据中提取标签列表
     */
    @SuppressWarnings("unchecked")
    private List<String> extractTagsFromMetadata(Map<String, Object> metadata) {
        if (MapUtils.isEmpty(metadata) || !metadata.containsKey("tags")) {
            return new ArrayList<>();
        }

        Object tagsObj = metadata.get("tags");
        if (tagsObj instanceof List) {
            return ((List<?>) tagsObj).stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        } else if (tagsObj instanceof String) {
            String tagsStr = (String) tagsObj;
            if (StringUtils.isNotEmpty(tagsStr)) {
                return List.of(tagsStr.split(","));
            }
        }

        return new ArrayList<>();
    }

    /**
     * 提取自定义字段
     */
    private Map<String, Object> extractCustomFields(Map<String, Object> metadata) {
        if (MapUtils.isEmpty(metadata)) {
            return new HashMap<>();
        }

        Map<String, Object> customFields = new HashMap<>();
        // 排除已知的标准字段
        Set<String> standardFields = Set.of("title", "doc_index", "doc_type", "tags");

        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (!standardFields.contains(entry.getKey())) {
                customFields.put(entry.getKey(), entry.getValue());
            }
        }

        return customFields;
    }
}