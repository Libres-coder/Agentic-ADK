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
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.lancedb.LanceDbConfiguration;
import com.alibaba.langengine.lancedb.LanceDbException;
import com.alibaba.langengine.lancedb.client.LanceDbClient;
import com.alibaba.langengine.lancedb.model.LanceDbQueryRequest;
import com.alibaba.langengine.lancedb.model.LanceDbQueryResponse;
import com.alibaba.langengine.lancedb.model.LanceDbVector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
public class LanceDbVectorStore extends VectorStore {

    /**
     * LanceDB客户端
     */
    private final LanceDbClient lanceDbClient;

    /**
     * 配置信息
     */
    private final LanceDbConfiguration configuration;

    /**
     * Embeddings实例，用于生成文档嵌入
     */
    private final Embeddings embeddings;

    /**
     * 表名
     */
    private final String tableName;

    /**
     * 向量维度
     */
    private final int vectorDimension;

    /**
     * 相似度阈值
     */
    private final double similarityThreshold;

    /**
     * 最大缓存大小
     */
    private final int maxCacheSize;

    /**
     * 文档缓存
     */
    private final Map<String, Document> documentCache;

    /**
     * 嵌入缓存
     */
    private final Map<String, List<Double>> embeddingCache;

    /**
     * 构造函数
     *
     * @param embeddings           Embeddings实例
     * @param configuration        LanceDB配置
     * @param tableName            表名
     * @param vectorDimension      向量维度
     * @param maxCacheSize         最大缓存大小
     * @param similarityThreshold  相似度阈值
     */
    public LanceDbVectorStore(Embeddings embeddings, 
                             LanceDbConfiguration configuration,
                             String tableName,
                             int vectorDimension,
                             int maxCacheSize,
                             double similarityThreshold) {
        
        if (embeddings == null) {
            throw new IllegalArgumentException("Embeddings cannot be null");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("LanceDB configuration cannot be null");
        }
        if (StringUtils.isBlank(tableName)) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (vectorDimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive");
        }
        if (maxCacheSize <= 0) {
            throw new IllegalArgumentException("Max cache size must be positive");
        }
        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
        }

        this.embeddings = embeddings;
        this.configuration = configuration;
        this.tableName = tableName;
        this.vectorDimension = vectorDimension;
        this.maxCacheSize = maxCacheSize;
        this.similarityThreshold = similarityThreshold;
        this.lanceDbClient = new LanceDbClient(configuration);
        this.documentCache = new ConcurrentHashMap<>();
        this.embeddingCache = new ConcurrentHashMap<>();

        log.info("LanceDB vector store initialized for table: {}, dimension: {}, cache size: {}, similarity threshold: {}", 
                tableName, vectorDimension, maxCacheSize, similarityThreshold);

        // 尝试创建表（如果不存在）
        try {
            lanceDbClient.createTable(tableName, vectorDimension);
            log.info("Table {} created or already exists", tableName);
        } catch (LanceDbException e) {
            log.warn("Failed to create table {}: {}", tableName, e.getMessage());
        }
    }

    /**
     * 便利构造函数，使用默认配置
     *
     * @param embeddings    Embeddings实例
     * @param configuration LanceDB配置
     * @param tableName     表名
     */
    public LanceDbVectorStore(Embeddings embeddings, LanceDbConfiguration configuration, String tableName) {
        this(embeddings, configuration, tableName, 
             configuration.getDefaultVectorDimension(),
             configuration.getCacheSize(),
             configuration.getDefaultSimilarityThreshold());
    }

    @Override
    public void addDocuments(List<Document> documents) {
        try {
            addDocumentsInternal(documents);
        } catch (LanceDbException e) {
            throw new RuntimeException("Failed to add documents to LanceDB", e);
        }
    }
    
    private void addDocumentsInternal(List<Document> documents) throws LanceDbException {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        log.info("Adding {} documents to LanceDB vector store", documents.size());

        List<LanceDbVector> vectors = convertDocumentsToVectors(documents);
        
        // 批量插入到LanceDB
        if (!vectors.isEmpty()) {
            lanceDbClient.insert(tableName, vectors);
            log.info("Successfully inserted {} vectors into LanceDB table: {}", vectors.size(), tableName);
        }
    }

    /**
     * 将文档列表转换为LanceDB向量列表
     *
     * @param documents 文档列表
     * @return 向量列表
     * @throws LanceDbException 转换异常
     */
    private List<LanceDbVector> convertDocumentsToVectors(List<Document> documents) throws LanceDbException {
        List<LanceDbVector> vectors = new ArrayList<>();
        
        for (Document document : documents) {
            Optional<LanceDbVector> vectorOpt = convertDocumentToLanceDbVector(document);
            vectorOpt.ifPresent(vectors::add);
        }
        
        return vectors;
    }

    /**
     * 将单个文档转换为LanceDB向量
     *
     * @param document 文档
     * @return 向量对象的Optional
     */
    private Optional<LanceDbVector> convertDocumentToLanceDbVector(Document document) {
        try {
            // 生成文档嵌入
            List<Double> embedding = generateDocumentEmbedding(document);
            if (embedding == null) {
                return Optional.empty();
            }
            
            // 验证向量维度
            if (!isValidVectorDimension(embedding)) {
                log.warn("Document embedding dimension {} does not match expected dimension {}", 
                        embedding.size(), vectorDimension);
                return Optional.empty();
            }
            
            // 生成文档ID
            String docId = generateDocumentId(document);
            
            // 创建LanceDB向量记录
            LanceDbVector vector = LanceDbVector.of(docId, embedding, document.getPageContent(), 
                                                  document.getMetadata());
            
            // 更新缓存
            updateDocumentCache(docId, document, embedding);
            
            return Optional.of(vector);
            
        } catch (Exception e) {
            log.error("Failed to convert document to vector: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 生成文档嵌入
     *
     * @param document 文档
     * @return 嵌入向量，失败时返回null
     */
    private List<Double> generateDocumentEmbedding(Document document) {
        try {
            List<Document> embeddedDocs = embeddings.embedDocument(Arrays.asList(document));
            if (embeddedDocs.isEmpty() || embeddedDocs.get(0).getEmbedding() == null) {
                log.warn("Failed to generate embedding for document: {}", document.getPageContent());
                return null;
            }
            return embeddedDocs.get(0).getEmbedding();
        } catch (Exception e) {
            log.error("Error generating embedding for document: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 验证向量维度是否正确
     *
     * @param embedding 嵌入向量
     * @return 是否有效
     */
    private boolean isValidVectorDimension(List<Double> embedding) {
        return embedding != null && embedding.size() == vectorDimension;
    }

    /**
     * 更新文档缓存
     *
     * @param docId     文档ID
     * @param document  文档
     * @param embedding 嵌入向量
     */
    private void updateDocumentCache(String docId, Document document, List<Double> embedding) {
        manageCacheSize();
        documentCache.put(docId, document);
        embeddingCache.put(docId, embedding);
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        try {
            return similaritySearchInternal(query, k, maxDistanceValue, type);
        } catch (LanceDbException e) {
            throw new RuntimeException("Similarity search failed in LanceDB", e);
        }
    }
    
    private List<Document> similaritySearchInternal(String query, int k, Double maxDistanceValue, Integer type) throws LanceDbException {
        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }

        log.info("Performing similarity search in LanceDB for query: '{}', k: {}", query, k);

        // 生成查询嵌入
        List<Double> queryEmbedding = generateQueryEmbedding(query);
        
        // 构建查询请求
        LanceDbQueryRequest request = buildLanceDbQueryRequest(queryEmbedding, k, maxDistanceValue);
        
        // 执行查询
        LanceDbQueryResponse response = performLanceDbQuery(request);
        
        // 处理查询结果
        return processQueryResponse(response, query);
    }

    /**
     * 生成查询嵌入
     *
     * @param query 查询文本
     * @return 查询嵌入向量
     * @throws LanceDbException 生成异常
     */
    private List<Double> generateQueryEmbedding(String query) throws LanceDbException {
        try {
            List<Document> queryDocs = embeddings.embedTexts(Arrays.asList(query));
            if (queryDocs.isEmpty() || queryDocs.get(0).getEmbedding() == null) {
                throw new LanceDbException("Failed to generate embedding for query: " + query);
            }
            
            List<Double> queryEmbedding = queryDocs.get(0).getEmbedding();
            
            // 验证查询向量维度
            if (!isValidVectorDimension(queryEmbedding)) {
                throw new LanceDbException("Query embedding dimension " + queryEmbedding.size() + 
                                         " does not match expected dimension " + vectorDimension);
            }
            
            return queryEmbedding;
        } catch (Exception e) {
            throw new LanceDbException("Failed to generate query embedding", e);
        }
    }

    /**
     * 构建LanceDB查询请求
     *
     * @param queryEmbedding   查询嵌入向量
     * @param k               返回结果数量
     * @param maxDistanceValue 最大距离值
     * @return 查询请求对象
     */
    private LanceDbQueryRequest buildLanceDbQueryRequest(List<Double> queryEmbedding, int k, Double maxDistanceValue) {
        LanceDbQueryRequest.LanceDbQueryRequestBuilder requestBuilder = LanceDbQueryRequest.builder()
                .vector(queryEmbedding)
                .limit(k)
                .metric("cosine")
                .includeMetadata(true);

        // 如果指定了距离阈值，添加过滤条件
        if (maxDistanceValue != null) {
            requestBuilder.distanceThreshold(maxDistanceValue);
        } else {
            // 使用相似度阈值转换为距离阈值（余弦距离 = 1 - 余弦相似度）
            requestBuilder.distanceThreshold(1.0 - similarityThreshold);
        }

        return requestBuilder.build();
    }

    /**
     * 执行LanceDB查询
     *
     * @param request 查询请求
     * @return 查询响应
     * @throws LanceDbException 查询异常
     */
    private LanceDbQueryResponse performLanceDbQuery(LanceDbQueryRequest request) throws LanceDbException {
        LanceDbQueryResponse response = lanceDbClient.query(tableName, request);

        if (!response.isSuccessful()) {
            throw new LanceDbException("LanceDB query failed: " + response.getErrorMessage());
        }

        return response;
    }

    /**
     * 处理查询响应，转换为文档列表
     *
     * @param response 查询响应
     * @param query    原始查询文本，用于日志
     * @return 文档列表
     */
    private List<Document> processQueryResponse(LanceDbQueryResponse response, String query) {
        if (!response.hasResults()) {
            log.info("No results found for query: '{}'", query);
            return new ArrayList<>();
        }

        // 转换结果为Document列表
        List<Document> results = response.getResults().stream()
                .map(this::convertVectorToDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Found {} similar documents for query: '{}'", results.size(), query);
        return results;
    }

    /**
     * 搜索LanceDB向量
     *
     * @param request 查询请求
     * @return 文档列表
     * @throws LanceDbException 查询异常
     */
    public List<Document> searchLanceDb(LanceDbQueryRequest request) throws LanceDbException {
        if (request == null) {
            throw new IllegalArgumentException("Query request cannot be null");
        }

        try {
            log.info("Searching LanceDB with custom request");
            
            LanceDbQueryResponse response = lanceDbClient.query(tableName, request);
            
            if (!response.isSuccessful()) {
                log.warn("LanceDB search failed: {}", response.getErrorMessage());
                throw new LanceDbException("LanceDB search failed: " + response.getErrorMessage());
            }
            
            if (!response.hasResults()) {
                log.info("No results found for LanceDB search");
                return new ArrayList<>();
            }
            
            List<Document> documents = response.getResults().stream()
                    .map(this::convertVectorToDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            log.info("LanceDB search completed, found {} documents", documents.size());
            return documents;
            
        } catch (Exception e) {
            log.error("LanceDB search failed: {}", e.getMessage(), e);
            throw new LanceDbException("LanceDB search failed", e);
        }
    }

    /**
     * 将LanceDbVector转换为Document
     *
     * @param vector LanceDB向量
     * @return Document对象
     */
    private Document convertVectorToDocument(LanceDbVector vector) {
        if (vector == null || StringUtils.isBlank(vector.getText())) {
            return null;
        }

        try {
            Map<String, Object> metadata = vector.getMetadata() != null ? 
                    new HashMap<>(vector.getMetadata()) : new HashMap<>();
            
            // 添加相似度信息
            if (vector.getScore() != null) {
                metadata.put("similarity_score", vector.getScore());
            }
            if (vector.getDistance() != null) {
                metadata.put("distance", vector.getDistance());
            }
            
            // 添加LanceDB特定信息
            metadata.put("lancedb_id", vector.getId());
            metadata.put("vector_dimension", vector.getVectorDimension());
            
            return new Document(vector.getText(), metadata);
            
        } catch (Exception e) {
            log.error("Failed to convert LanceDbVector to Document: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成文档ID
     *
     * @param document 文档
     * @return 文档ID
     */
    private String generateDocumentId(Document document) {
        // 优先使用文档元数据中的ID
        if (document.getMetadata() != null && document.getMetadata().containsKey("id")) {
            Object id = document.getMetadata().get("id");
            if (id != null) {
                return id.toString();
            }
        }
        
        // 使用内容hash作为ID
        return UUID.nameUUIDFromBytes(document.getPageContent().getBytes()).toString();
    }

    /**
     * 管理缓存大小
     */
    private void manageCacheSize() {
        if (documentCache.size() >= maxCacheSize) {
            // 简单的LRU策略：移除最早的条目
            Iterator<Map.Entry<String, Document>> iterator = documentCache.entrySet().iterator();
            if (iterator.hasNext()) {
                String keyToRemove = iterator.next().getKey();
                documentCache.remove(keyToRemove);
                embeddingCache.remove(keyToRemove);
            }
        }
    }

    /**
     * 获取表名
     *
     * @return 表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 获取向量维度
     *
     * @return 向量维度
     */
    public int getVectorDimension() {
        return vectorDimension;
    }

    /**
     * 获取相似度阈值
     *
     * @return 相似度阈值
     */
    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存大小
     */
    public int getCacheSize() {
        return documentCache.size();
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        documentCache.clear();
        embeddingCache.clear();
        log.info("Cache cleared for LanceDB vector store");
    }

    /**
     * 删除表
     *
     * @throws LanceDbException 删除异常
     */
    public void dropTable() throws LanceDbException {
        try {
            lanceDbClient.dropTable(tableName);
            clearCache();
            log.info("Table {} dropped successfully", tableName);
        } catch (LanceDbException e) {
            log.error("Failed to drop table {}: {}", tableName, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取配置信息
     *
     * @return 配置对象
     */
    public LanceDbConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 关闭向量存储
     */
    public void close() {
        clearCache();
        if (lanceDbClient != null) {
            lanceDbClient.close();
        }
        log.info("LanceDB vector store closed");
    }
}
