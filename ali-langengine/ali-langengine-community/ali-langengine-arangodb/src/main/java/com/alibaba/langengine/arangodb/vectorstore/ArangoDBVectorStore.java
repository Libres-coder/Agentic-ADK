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
package com.alibaba.langengine.arangodb.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.arangodb.ArangoDBConfiguration;
import com.alibaba.langengine.arangodb.exception.ArangoDBVectorStoreException;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class ArangoDBVectorStore extends VectorStore implements AutoCloseable {
    
    /**
     * 嵌入模型
     */
    private Embeddings embedding;
    
    /**
     * 集合名称
     */
    private final String collectionName;
    
    /**
     * ArangoDB服务
     */
    private final ArangoDBService arangoDBService;
    
    /**
     * 配置参数
     */
    private final ArangoDBParam param;
    
    /**
     * 文档缓存
     */
    private final Map<String, Document> documentCache;
    
    /**
     * 嵌入向量缓存
     */
    private final Map<String, List<Double>> embeddingCache;
    
    /**
     * 构造函数 - 使用默认配置
     */
    public ArangoDBVectorStore(String collectionName) {
        this(collectionName, ArangoDBParam.createDefault());
    }
    
    /**
     * 构造函数 - 使用指定参数
     */
    public ArangoDBVectorStore(String collectionName, ArangoDBParam param) {
        this(new ArangoDBConfiguration(), collectionName, param);
    }
    
    /**
     * 构造函数 - 使用完整配置
     */
    public ArangoDBVectorStore(ArangoDBConfiguration configuration, String collectionName, ArangoDBParam param) {
        if (StringUtils.isBlank(collectionName)) {
            throw new IllegalArgumentException("Collection name cannot be null or empty");
        }
        
        this.collectionName = collectionName;
        this.param = param != null ? param : ArangoDBParam.createDefault();
        
        // 创建服务
        this.arangoDBService = new ArangoDBService(configuration, collectionName, this.param);
        
        // 初始化缓存
        int maxCacheSize = this.param.getInitParam().getMaxCacheSize();
        this.documentCache = new ConcurrentHashMap<>(maxCacheSize / 4);
        this.embeddingCache = new ConcurrentHashMap<>(maxCacheSize / 4);
        
        log.info("ArangoDB vector store created: collection={}, dimension={}", 
                collectionName, this.param.getInitParam().getDimension());
    }
    
    /**
     * 构造函数 - 使用现有服务
     */
    public ArangoDBVectorStore(String collectionName, ArangoDBService service, ArangoDBParam param) {
        if (StringUtils.isBlank(collectionName)) {
            throw new IllegalArgumentException("Collection name cannot be null or empty");
        }
        if (service == null) {
            throw new IllegalArgumentException("ArangoDB service cannot be null");
        }
        
        this.collectionName = collectionName;
        this.arangoDBService = service;
        this.param = param != null ? param : ArangoDBParam.createDefault();
        
        // 初始化缓存
        int maxCacheSize = this.param.getInitParam().getMaxCacheSize();
        this.documentCache = new ConcurrentHashMap<>(maxCacheSize / 4);
        this.embeddingCache = new ConcurrentHashMap<>(maxCacheSize / 4);
        
        log.info("ArangoDB vector store created with existing service: collection={}", collectionName);
    }
    
    /**
     * 初始化向量存储
     */
    public void init() {
        try {
            arangoDBService.init();
            log.info("ArangoDB vector store initialized successfully");
        } catch (Exception e) {
            throw ArangoDBVectorStoreException.connectionError("Failed to initialize ArangoDB vector store", e);
        }
    }
    
    /**
     * 添加文档
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("No documents to add");
            return;
        }
        
        try {
            log.info("Adding {} documents to ArangoDB vector store", documents.size());
            
            // 生成嵌入向量
            List<Document> embeddedDocuments = generateEmbeddings(documents);
            
            // 添加到存储
            arangoDBService.addDocuments(embeddedDocuments);
            
            // 更新缓存
            updateDocumentCache(embeddedDocuments);
            
            log.info("Successfully added {} documents", embeddedDocuments.size());
            
        } catch (Exception e) {
            log.error("Failed to add documents to ArangoDB vector store", e);
            throw ArangoDBVectorStoreException.insertError("Failed to add documents", e);
        }
    }
    
    /**
     * 相似度搜索
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isBlank(query)) {
            log.warn("Query is null or empty");
            return new ArrayList<>();
        }
        
        if (k <= 0) {
            log.warn("Invalid k value: {}", k);
            return new ArrayList<>();
        }
        
        try {
            log.debug("Performing similarity search: query={}, k={}, maxDistance={}, type={}", 
                     query, k, maxDistanceValue, type);
            
            // 生成查询向量
            List<Double> queryVector = generateQueryEmbedding(query);
            if (queryVector == null || queryVector.isEmpty()) {
                log.warn("Failed to generate query embedding");
                return new ArrayList<>();
            }
            
            // 验证向量维度
            validateVectorDimension(queryVector);
            
            // 执行相似度搜索
            List<Document> results = arangoDBService.similaritySearch(queryVector, k, maxDistanceValue, type);
            
            // 过滤结果
            if (maxDistanceValue != null) {
                results = filterByMaxDistance(results, maxDistanceValue);
            }
            
            log.info("Found {} similar documents", results.size());
            
            return results;
            
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 基于向量的相似度搜索
     */
    public List<Document> similaritySearchByVector(List<Double> queryVector, int k, Double maxDistanceValue, Integer type) {
        if (queryVector == null || queryVector.isEmpty()) {
            log.warn("Query vector is null or empty");
            return new ArrayList<>();
        }
        
        if (k <= 0) {
            log.warn("Invalid k value: {}", k);
            return new ArrayList<>();
        }
        
        try {
            log.debug("Performing similarity search by vector: dimension={}, k={}, maxDistance={}, type={}", 
                     queryVector.size(), k, maxDistanceValue, type);
            
            // 验证向量维度
            validateVectorDimension(queryVector);
            
            // 执行搜索
            List<Document> results = arangoDBService.similaritySearch(queryVector, k, maxDistanceValue, type);
            
            // 过滤结果
            if (maxDistanceValue != null) {
                results = filterByMaxDistance(results, maxDistanceValue);
            }
            
            log.info("Found {} similar documents", results.size());
            
            return results;
            
        } catch (Exception e) {
            log.error("Failed to perform similarity search by vector", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 带元数据过滤的相似度搜索
     */
    public List<Document> similaritySearchWithMetadata(String query, int k, Map<String, Object> metadataFilter) {
        List<Document> results = similaritySearch(query, k, null, null);
        
        if (metadataFilter == null || metadataFilter.isEmpty()) {
            return results;
        }
        
        // 在结果中应用元数据过滤
        return results.stream()
                .filter(doc -> matchesMetadataFilter(doc, metadataFilter))
                .collect(Collectors.toList());
    }
    
    /**
     * 生成嵌入向量
     */
    private List<Document> generateEmbeddings(List<Document> documents) {
        if (embedding == null) {
            throw ArangoDBVectorStoreException.configurationError("Embeddings model not configured");
        }
        
        try {
            return embedding.embedDocument(documents);
        } catch (Exception e) {
            throw ArangoDBVectorStoreException.configurationError("Failed to generate embeddings: " + e.getMessage());
        }
    }
    
    /**
     * 生成查询嵌入向量
     */
    private List<Double> generateQueryEmbedding(String query) {
        if (embedding == null) {
            throw ArangoDBVectorStoreException.configurationError("Embeddings model not configured");
        }
        
        try {
            List<String> embeddingStrings = embedding.embedQuery(query, 1);
            if (embeddingStrings.isEmpty()) {
                return null;
            }
            
            String embeddingString = embeddingStrings.get(0);
            return parseEmbeddingString(embeddingString);
            
        } catch (Exception e) {
            log.error("Failed to generate query embedding", e);
            return null;
        }
    }
    
    /**
     * 解析嵌入向量字符串
     */
    private List<Double> parseEmbeddingString(String embeddingString) {
        try {
            if (embeddingString.startsWith("[") && embeddingString.endsWith("]")) {
                // JSON格式解析
                String cleaned = embeddingString.substring(1, embeddingString.length() - 1);
                String[] parts = cleaned.split(",");
                
                List<Double> result = new ArrayList<>(parts.length);
                for (String part : parts) {
                    result.add(Double.parseDouble(part.trim()));
                }
                return result;
            } else {
                // 尝试直接JSON解析
                return JSON.parseArray(embeddingString, Double.class);
            }
        } catch (Exception e) {
            log.error("Failed to parse embedding string: {}", embeddingString, e);
            return null;
        }
    }
    
    /**
     * 验证向量维度
     */
    private void validateVectorDimension(List<Double> vector) {
        int expectedDimension = param.getInitParam().getDimension();
        if (vector.size() != expectedDimension) {
            throw ArangoDBVectorStoreException.vectorDimensionError(
                    String.format("Vector dimension mismatch: expected %d, actual %d", 
                                expectedDimension, vector.size()));
        }
    }
    
    /**
     * 按最大距离过滤结果
     */
    private List<Document> filterByMaxDistance(List<Document> documents, double maxDistance) {
        return documents.stream()
                .filter(doc -> {
                    Object distanceObj = doc.getMetadata().get("distance");
                    if (distanceObj instanceof Number) {
                        double distance = ((Number) distanceObj).doubleValue();
                        return distance <= maxDistance;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 元数据过滤匹配
     */
    private boolean matchesMetadataFilter(Document document, Map<String, Object> metadataFilter) {
        Map<String, Object> docMetadata = document.getMetadata();
        if (docMetadata == null) {
            return metadataFilter.isEmpty();
        }
        
        for (Map.Entry<String, Object> entry : metadataFilter.entrySet()) {
            String key = entry.getKey();
            Object expectedValue = entry.getValue();
            Object actualValue = docMetadata.get(key);
            
            if (!Objects.equals(expectedValue, actualValue)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 更新文档缓存
     */
    private void updateDocumentCache(List<Document> documents) {
        if (documents == null) {
            return;
        }
        
        int maxCacheSize = param.getInitParam().getMaxCacheSize();
        
        for (Document document : documents) {
            String uniqueId = document.getUniqueId();
            if (uniqueId != null && documentCache.size() < maxCacheSize) {
                documentCache.put(uniqueId, document);
                
                if (document.getEmbedding() != null && embeddingCache.size() < maxCacheSize) {
                    embeddingCache.put(uniqueId, document.getEmbedding());
                }
            }
        }
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = arangoDBService.getStats();
        stats.put("documentCacheSize", documentCache.size());
        stats.put("embeddingCacheSize", embeddingCache.size());
        stats.put("maxCacheSize", param.getInitParam().getMaxCacheSize());
        return stats;
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        documentCache.clear();
        embeddingCache.clear();
        arangoDBService.clearCache();
        log.info("All caches cleared");
    }
    
    /**
     * 健康检查
     */
    public boolean isHealthy() {
        return arangoDBService.isHealthy();
    }
    
    /**
     * 获取集合名称
     */
    public String getCollectionName() {
        return collectionName;
    }
    
    /**
     * 获取向量维度
     */
    public int getVectorDimension() {
        return param.getInitParam().getDimension();
    }
    
    /**
     * 获取相似度阈值
     */
    public double getSimilarityThreshold() {
        return param.getInitParam().getSimilarityThreshold();
    }
    
    /**
     * 设置相似度阈值
     */
    public void setSimilarityThreshold(double threshold) {
        if (threshold < 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
        }
        param.getInitParam().setSimilarityThreshold(threshold);
    }
    
    @Override
    public void close() {
        try {
            clearCache();
            
            if (arangoDBService != null) {
                arangoDBService.close();
            }
            
            log.info("ArangoDB vector store closed");
            
        } catch (Exception e) {
            log.error("Error closing ArangoDB vector store", e);
        }
    }
}
