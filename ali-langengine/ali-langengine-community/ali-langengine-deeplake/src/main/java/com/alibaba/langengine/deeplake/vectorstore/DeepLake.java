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
package com.alibaba.langengine.deeplake.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.deeplake.DeepLakeConfiguration;
import com.alibaba.langengine.deeplake.vectorstore.service.DeepLakeService;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.langengine.deeplake.DeepLakeConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class DeepLake extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * 数据集名称，标识一个唯一的向量数据集
     * 可以看做是某个业务或知识库的集合标识
     */
    private String datasetName;

    /**
     * Deep Lake API token，用于身份验证
     */
    private String apiToken;

    /**
     * Deep Lake 服务实例
     */
    private final DeepLakeService deepLakeService;

    /**
     * Deep Lake 参数配置
     */
    private DeepLakeParam deepLakeParam;

    /**
     * 构造函数 - 使用默认配置
     * 
     * @param embedding 嵌入模型
     * @param datasetName 数据集名称
     */
    public DeepLake(Embeddings embedding, String datasetName) {
        this(DEEPLAKE_SERVER_URL, DEEPLAKE_API_TOKEN, embedding, datasetName, null);
    }

    /**
     * 构造函数 - 使用自定义参数
     * 
     * @param embedding 嵌入模型
     * @param datasetName 数据集名称
     * @param deepLakeParam Deep Lake参数配置
     */
    public DeepLake(Embeddings embedding, String datasetName, DeepLakeParam deepLakeParam) {
        this(DEEPLAKE_SERVER_URL, DEEPLAKE_API_TOKEN, embedding, datasetName, deepLakeParam);
    }

    /**
     * 构造函数 - 完整配置
     * 
     * @param serverUrl Deep Lake服务URL
     * @param apiToken API令牌
     * @param embedding 嵌入模型
     * @param datasetName 数据集名称
     * @param deepLakeParam 参数配置
     */
    public DeepLake(String serverUrl, String apiToken, Embeddings embedding, 
                   String datasetName, DeepLakeParam deepLakeParam) {
        if (StringUtils.isEmpty(serverUrl)) {
            throw DeepLakeException.invalidParameter("Deep Lake server URL cannot be empty");
        }
        if (StringUtils.isEmpty(datasetName)) {
            throw DeepLakeException.invalidParameter("Dataset name cannot be empty");
        }

        this.embedding = embedding;
        this.datasetName = datasetName;
        this.apiToken = apiToken;
        this.deepLakeParam = deepLakeParam != null ? deepLakeParam : new DeepLakeParam();

        // 初始化服务
        Duration timeout = Duration.ofSeconds(60);
        this.deepLakeService = new DeepLakeService(serverUrl, apiToken, datasetName, timeout);
        
        // 初始化数据集
        init();
    }

    /**
     * 初始化数据集
     */
    public void init() {
        try {
            log.info("Initializing Deep Lake dataset: {}", datasetName);
            
            Map<String, Object> datasetConfig = createDatasetConfig();
            deepLakeService.initializeDataset(datasetConfig);
            
            log.info("Deep Lake dataset initialization completed: {}", datasetName);
        } catch (Exception e) {
            log.error("Failed to initialize Deep Lake dataset: {}", datasetName, e);
            throw DeepLakeException.operationFailed("Failed to initialize dataset: " + datasetName, e);
        }
    }

    /**
     * 创建数据集配置
     */
    private Map<String, Object> createDatasetConfig() {
        Map<String, Object> config = new HashMap<>();
        DeepLakeParam.InitParam initParam = deepLakeParam.getInitParam();
        
        config.put("description", initParam.getDescription());
        config.put("is_public", initParam.getIsPublic());
        
        // 向量配置
        Map<String, Object> vectorConfig = new HashMap<>();
        vectorConfig.put("dimension", initParam.getDimension());
        vectorConfig.put("distance_metric", initParam.getVectorDistance());
        
        // 索引配置
        Map<String, Object> indexConfig = new HashMap<>();
        indexConfig.put("type", initParam.getIndexType());
        indexConfig.put("max_connections", initParam.getMaxConnections());
        indexConfig.put("ef_construction", initParam.getEfConstruction());
        indexConfig.put("ef_search", initParam.getEfSearch());
        
        vectorConfig.put("index", indexConfig);
        config.put("vector_config", vectorConfig);
        
        return config;
    }

    /**
     * 添加文档到向量存储
     * 
     * @param documents 要添加的文档列表
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("No documents to add");
            return;
        }

        try {
            log.info("Adding {} documents to Deep Lake dataset: {}", documents.size(), datasetName);
            
            // 确保文档有唯一ID
            documents.forEach(document -> {
                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }
            });

            // 如果文档没有向量，使用embedding生成
            List<Document> documentsWithEmbeddings = ensureDocumentsHaveEmbeddings(documents);
            
            // 验证向量维度
            validateVectorDimensions(documentsWithEmbeddings);
            
            deepLakeService.addDocuments(documentsWithEmbeddings);
            
            log.info("Successfully added {} documents to Deep Lake", documentsWithEmbeddings.size());
        } catch (Exception e) {
            log.error("Failed to add documents to Deep Lake", e);
            throw DeepLakeException.operationFailed("Failed to add documents", e);
        }
    }

    /**
     * 相似度搜索
     * 
     * @param query 查询文本
     * @param k 返回结果数量
     * @param maxDistanceValue 最大距离阈值
     * @param type 搜索类型（保留参数，兼容基类）
     * @return 相似文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isEmpty(query)) {
            throw DeepLakeException.invalidParameter("Query cannot be empty");
        }
        if (k <= 0) {
            throw DeepLakeException.invalidParameter("k must be positive");
        }

        try {
            log.info("Performing similarity search for query: '{}', k={}, maxDistance={}", 
                query, k, maxDistanceValue);
            
            // 将查询文本转换为向量
            List<Float> queryVector = embedQuery(query);
            
            // 执行搜索
            List<Document> results = deepLakeService.similaritySearch(queryVector, k, maxDistanceValue);
            
            log.info("Found {} similar documents", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw DeepLakeException.operationFailed("Failed to perform similarity search", e);
        }
    }

    /**
     * 添加文本并生成向量
     * 
     * @param texts 文本列表
     * @param metadatas 元数据列表
     * @param ids ID列表
     * @return 添加的文档ID列表
     */
    public List<String> addTexts(Iterable<String> texts, 
                                List<Map<String, Object>> metadatas, 
                                List<String> ids) {
        if (texts == null) {
            throw DeepLakeException.invalidParameter("Texts cannot be null");
        }

        List<String> textList = new ArrayList<>();
        texts.forEach(textList::add);
        
        if (textList.isEmpty()) {
            log.warn("No texts to add");
            return new ArrayList<>();
        }

        // 生成ID（如果未提供）
        List<String> finalIds = ids;
        if (CollectionUtils.isEmpty(finalIds)) {
            finalIds = textList.stream()
                .map(text -> UUID.randomUUID().toString())
                .collect(Collectors.toList());
        }

        // 创建文档
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < textList.size(); i++) {
            Document document = new Document();
            document.setUniqueId(finalIds.get(i));
            document.setPageContent(textList.get(i));
            
            if (metadatas != null && i < metadatas.size()) {
                document.setMetadata(metadatas.get(i));
            }
            
            documents.add(document);
        }

        addDocuments(documents);
        return finalIds;
    }

    /**
     * 删除向量
     * 
     * @param ids 要删除的向量ID列表
     */
    public void deleteVectors(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            log.warn("No IDs provided for deletion");
            return;
        }

        try {
            log.info("Deleting {} vectors from Deep Lake", ids.size());
            deepLakeService.deleteVectors(ids);
            log.info("Successfully deleted vectors");
        } catch (Exception e) {
            log.error("Failed to delete vectors", e);
            throw DeepLakeException.operationFailed("Failed to delete vectors", e);
        }
    }

    /**
     * 获取数据集统计信息
     * 
     * @return 统计信息
     */
    public Map<String, Object> getDatasetStats() {
        try {
            return deepLakeService.getDatasetStats();
        } catch (Exception e) {
            log.error("Failed to get dataset statistics", e);
            throw DeepLakeException.operationFailed("Failed to get dataset statistics", e);
        }
    }

    /**
     * 关闭连接和资源
     */
    public void close() {
        try {
            log.info("Closing Deep Lake connection");
            // Deep Lake服务通常不需要显式关闭，但可以在这里处理清理逻辑
        } catch (Exception e) {
            log.warn("Error while closing Deep Lake connection", e);
        }
    }

    /**
     * 确保文档有嵌入向量
     */
    private List<Document> ensureDocumentsHaveEmbeddings(List<Document> documents) {
        if (embedding == null) {
            log.warn("No embedding model provided, documents must have pre-computed embeddings");
            return documents;
        }

        List<Document> documentsNeedingEmbeddings = documents.stream()
            .filter(doc -> CollectionUtils.isEmpty(doc.getEmbedding()))
            .collect(Collectors.toList());

        if (documentsNeedingEmbeddings.isEmpty()) {
            return documents;
        }

        log.info("Generating embeddings for {} documents", documentsNeedingEmbeddings.size());
        List<Document> embeddedDocuments = embedding.embedDocument(documentsNeedingEmbeddings);
        
        // 合并嵌入向量回原文档
        Map<String, List<Double>> embeddingMap = embeddedDocuments.stream()
            .collect(Collectors.toMap(Document::getUniqueId, Document::getEmbedding));

        documentsNeedingEmbeddings.forEach(doc -> {
            List<Double> docEmbedding = embeddingMap.get(doc.getUniqueId());
            if (docEmbedding != null) {
                doc.setEmbedding(docEmbedding);
            }
        });

        return documents;
    }

    /**
     * 验证向量维度
     */
    private void validateVectorDimensions(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        Integer expectedDimension = deepLakeParam.getInitParam().getDimension();
        
        for (Document document : documents) {
            if (CollectionUtils.isNotEmpty(document.getEmbedding())) {
                int actualDimension = document.getEmbedding().size();
                if (actualDimension != expectedDimension) {
                    throw DeepLakeException.vectorDimensionError(
                        String.format("Vector dimension mismatch for document %s: expected %d, actual %d",
                            document.getUniqueId(), expectedDimension, actualDimension));
                }
            }
        }
    }

    /**
     * 将查询文本转换为向量
     */
    private List<Float> embedQuery(String query) {
        if (embedding == null) {
            throw DeepLakeException.invalidParameter("Embedding model is required for text search");
        }

        try {
            List<String> embeddingStrings = embedding.embedQuery(query, 1);
            if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
                return Lists.newArrayList();
            }
            
            return JSON.parseArray(embeddingStrings.get(0), Float.class);
        } catch (Exception e) {
            log.error("Failed to embed query: {}", query, e);
            throw DeepLakeException.operationFailed("Failed to embed query", e);
        }
    }
}
