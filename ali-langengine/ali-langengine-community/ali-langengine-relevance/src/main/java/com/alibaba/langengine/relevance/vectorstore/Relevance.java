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
package com.alibaba.langengine.relevance.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.relevance.exception.RelevanceException;
import com.alibaba.langengine.relevance.model.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Relevance extends VectorStore {

    /**
     * 向量化模型
     */
    private Embeddings embedding;

    /**
     * 数据集名称
     */
    private final String datasetName;

    /**
     * 配置参数
     */
    private final RelevanceParam param;

    /**
     * 服务层实例
     */
    private final RelevanceService relevanceService;

    /**
     * 使用默认配置构造
     *
     * @param embedding 向量化模型
     * @param datasetName 数据集名称
     */
    public Relevance(Embeddings embedding, String datasetName) {
        this.embedding = embedding;
        this.datasetName = datasetName != null ? datasetName : UUID.randomUUID().toString();
        this.param = new RelevanceParam.Builder()
                .datasetName(this.datasetName)
                .build();
        this.relevanceService = new RelevanceService(param);

        initializeDataset();
    }

    /**
     * 使用自定义参数构造（测试友好）
     *
     * @param embedding 向量化模型
     * @param param 配置参数
     * @param relevanceService 预配置的服务实例（用于测试）
     */
    Relevance(Embeddings embedding, RelevanceParam param, RelevanceService relevanceService) {
        this.embedding = embedding;
        this.param = param;
        this.datasetName = param.getDatasetName();
        this.relevanceService = relevanceService;
        // 跳过网络初始化，用于测试环境
    }

    /**
     * 使用自定义参数构造
     *
     * @param embedding 向量化模型
     * @param param 配置参数
     */
    public Relevance(Embeddings embedding, RelevanceParam param) {
        this.embedding = embedding;
        this.param = param;
        this.datasetName = param.getDatasetName();
        this.relevanceService = new RelevanceService(param);

        initializeDataset();
    }

    /**
     * 初始化数据集
     */
    private void initializeDataset() {
        try {
            if (!relevanceService.datasetExists()) {
                boolean created = relevanceService.createDataset();
                if (created) {
                    log.info("Dataset '{}' created successfully", datasetName);
                } else {
                    log.warn("Failed to create dataset '{}'", datasetName);
                }
            } else {
                log.debug("Dataset '{}' already exists", datasetName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize dataset '{}': {}", datasetName, e.getMessage(), e);
        }
    }

    /**
     * 添加文档到向量存储
     *
     * @param documents 文档列表
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.debug("No documents to add");
            return;
        }

        try {
            List<RelevanceDocument> relevanceDocs = new ArrayList<>();

            for (Document doc : documents) {
                RelevanceDocument relevanceDoc = new RelevanceDocument();
                relevanceDoc.setId(doc.getPageContent().hashCode() + "_" + System.currentTimeMillis());
                relevanceDoc.setText(doc.getPageContent());

                // 生成向量
                if (embedding != null) {
                    List<Document> embeddedDocs = embedding.embedTexts(Collections.singletonList(doc.getPageContent()));
                    if (!embeddedDocs.isEmpty() && embeddedDocs.get(0).getEmbedding() != null) {
                        List<Double> vector = embeddedDocs.get(0).getEmbedding();
                        relevanceDoc.setVector(vector);
                    }
                }

                // 设置元数据
                if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
                    relevanceDoc.setMetadata(new HashMap<>(doc.getMetadata()));
                }

                relevanceDocs.add(relevanceDoc);
            }

            boolean success = relevanceService.insertDocuments(relevanceDocs);
            if (success) {
                log.debug("Successfully added {} documents", documents.size());
            } else {
                log.error("Failed to add documents");
            }

        } catch (Exception e) {
            log.error("Error adding documents: {}", e.getMessage(), e);
            throw new RelevanceException("Failed to add documents", e);
        }
    }

    /**
     * 相似度搜索
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @return 相似文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int k) {
        return similaritySearch(query, k, null, null);
    }

    /**
     * 返回与查询最相似的文档（实现VectorStore抽象方法）
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @param maxDistanceValue 最大距离值
     * @param type 类型参数
     * @return 相似文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (query == null || query.trim().isEmpty()) {
            log.debug("Empty query, returning empty results");
            return Collections.emptyList();
        }

        try {
            // 生成查询向量
            List<Double> queryVector = null;
            if (embedding != null) {
                List<Document> embeddedDocs = embedding.embedTexts(Collections.singletonList(query));
                if (!embeddedDocs.isEmpty() && embeddedDocs.get(0).getEmbedding() != null) {
                    queryVector = embeddedDocs.get(0).getEmbedding();
                }
            } else {
                throw new RelevanceException("Embedding model is required for similarity search");
            }

            // 构建查询请求
            RelevanceQueryRequest queryRequest = new RelevanceQueryRequest(queryVector, k);
            queryRequest.setIncludeMetadata(true);
            if (maxDistanceValue != null) {
                queryRequest.setScoreThreshold(maxDistanceValue);
            }

            // 执行查询
            RelevanceQueryResponse response = relevanceService.queryDocuments(queryRequest);

            // 转换结果
            return response.getDocuments().stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error during similarity search: {}", e.getMessage(), e);
            throw new RelevanceException("Failed to perform similarity search", e);
        }
    }

    /**
     * 相似度搜索（带过滤条件）
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @param filter 过滤条件
     * @return 相似文档列表
     */
    public List<Document> similaritySearchWithFilter(String query, int k, Map<String, Object> filter) {
        if (query == null || query.trim().isEmpty()) {
            log.debug("Empty query, returning empty results");
            return Collections.emptyList();
        }

        try {
            // 生成查询向量
            List<Double> queryVector = null;
            if (embedding != null) {
                List<Document> embeddedDocs = embedding.embedTexts(Collections.singletonList(query));
                if (!embeddedDocs.isEmpty() && embeddedDocs.get(0).getEmbedding() != null) {
                    queryVector = embeddedDocs.get(0).getEmbedding();
                }
            } else {
                throw new RelevanceException("Embedding model is required for similarity search");
            }

            // 构建查询请求
            RelevanceQueryRequest queryRequest = new RelevanceQueryRequest(queryVector, k);
            queryRequest.setFilter(filter);
            queryRequest.setIncludeMetadata(true);

            // 执行查询
            RelevanceQueryResponse response = relevanceService.queryDocuments(queryRequest);

            // 转换结果
            return response.getDocuments().stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error during similarity search: {}", e.getMessage(), e);
            throw new RelevanceException("Failed to perform similarity search", e);
        }
    }

    /**
     * 相似度搜索（带分数）
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @return 相似文档列表（包含分数）
     */
    public List<Document> similaritySearchWithScore(String query, int k) {
        return similaritySearchWithScore(query, k, null);
    }

    /**
     * 相似度搜索（带分数和过滤条件）
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @param filter 过滤条件
     * @return 相似文档列表（包含分数）
     */
    public List<Document> similaritySearchWithScore(String query, int k, Map<String, Object> filter) {
        if (query == null || query.trim().isEmpty()) {
            log.debug("Empty query, returning empty results");
            return Collections.emptyList();
        }

        try {
            // 生成查询向量
            List<Double> queryVector = null;
            if (embedding != null) {
                List<Document> embeddedDocs = embedding.embedTexts(Collections.singletonList(query));
                if (!embeddedDocs.isEmpty() && embeddedDocs.get(0).getEmbedding() != null) {
                    queryVector = embeddedDocs.get(0).getEmbedding();
                }
            } else {
                throw new RelevanceException("Embedding model is required for similarity search");
            }

            // 构建查询请求
            RelevanceQueryRequest queryRequest = new RelevanceQueryRequest(queryVector, k);
            queryRequest.setFilter(filter);
            queryRequest.setIncludeMetadata(true);

            // 执行查询
            RelevanceQueryResponse response = relevanceService.queryDocuments(queryRequest);

            // 转换结果（包含分数）
            return response.getDocuments().stream()
                    .map(this::convertToDocumentWithScore)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error during similarity search with score: {}", e.getMessage(), e);
            throw new RelevanceException("Failed to perform similarity search with score", e);
        }
    }

    /**
     * 删除文档
     *
     * @param ids 文档ID列表
     * @return 是否成功删除
     */
    public Boolean delete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            log.debug("No document IDs provided for deletion");
            return true;
        }

        try {
            boolean success = relevanceService.deleteDocuments(ids);
            if (success) {
                log.debug("Successfully deleted {} documents", ids.size());
            } else {
                log.error("Failed to delete documents");
            }
            return success;

        } catch (Exception e) {
            log.error("Error deleting documents: {}", e.getMessage(), e);
            throw new RelevanceException("Failed to delete documents", e);
        }
    }

    /**
     * 转换为 Document 对象
     */
    private Document convertToDocument(RelevanceDocument relevanceDoc) {
        Document doc = new Document();
        doc.setPageContent(relevanceDoc.getText());

        Map<String, Object> metadata = new HashMap<>();
        if (relevanceDoc.getMetadata() != null) {
            metadata.putAll(relevanceDoc.getMetadata());
        }
        metadata.put("id", relevanceDoc.getId());
        doc.setMetadata(metadata);

        return doc;
    }

    /**
     * 转换为带分数的 Document 对象
     */
    private Document convertToDocumentWithScore(RelevanceDocument relevanceDoc) {
        Document doc = convertToDocument(relevanceDoc);

        // 添加相似度分数到元数据
        if (relevanceDoc.getScore() != null) {
            doc.getMetadata().put("score", relevanceDoc.getScore());
        }

        return doc;
    }

    /**
     * 获取数据集名称
     */
    public String getDatasetName() {
        return datasetName;
    }

    /**
     * 获取配置参数
     */
    public RelevanceParam getParam() {
        return param;
    }

    /**
     * 关闭资源
     */
    public void close() {
        if (relevanceService != null) {
            relevanceService.close();
        }
    }

    @Override
    public String toString() {
        return "Relevance{" +
                "datasetName='" + datasetName + '\'' +
                ", embedding=" + (embedding != null ? embedding.getClass().getSimpleName() : "null") +
                ", apiUrl='" + param.getApiUrl() + '\'' +
                ", projectId='" + param.getProjectId() + '\'' +
                '}';
    }
}