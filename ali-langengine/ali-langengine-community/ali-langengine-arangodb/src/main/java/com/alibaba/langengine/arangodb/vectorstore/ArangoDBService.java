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

import com.alibaba.langengine.arangodb.ArangoDBConfiguration;
import com.alibaba.langengine.arangodb.client.ArangoDBVectorClient;
import com.alibaba.langengine.arangodb.exception.ArangoDBVectorStoreException;
import com.alibaba.langengine.arangodb.model.ArangoDBQueryRequest;
import com.alibaba.langengine.arangodb.model.ArangoDBQueryResponse;
import com.alibaba.langengine.arangodb.model.ArangoDBVector;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
public class ArangoDBService implements AutoCloseable {
    
    private final ArangoDBVectorClient client;
    private final ArangoDBConfiguration configuration;
    private final ArangoDBParam param;
    private final String collectionName;
    
    /**
     * 构造函数
     */
    public ArangoDBService(ArangoDBConfiguration configuration, String collectionName, ArangoDBParam param) {
        this.configuration = configuration;
        this.collectionName = collectionName;
        this.param = param != null ? param : ArangoDBParam.createDefault();
        
        // 验证参数
        validateInputs();
        
        // 创建客户端
        this.client = new ArangoDBVectorClient(configuration);
        
        log.info("ArangoDB service initialized: collection={}, dimension={}", 
                collectionName, this.param.getInitParam().getDimension());
    }
    
    /**
     * 简化构造函数
     */
    public ArangoDBService(String collectionName, ArangoDBParam param) {
        this(createDefaultConfiguration(), collectionName, param);
    }
    
    /**
     * 默认构造函数
     */
    public ArangoDBService(String collectionName) {
        this(collectionName, ArangoDBParam.createDefault());
    }
    
    /**
     * 创建默认配置
     */
    private static ArangoDBConfiguration createDefaultConfiguration() {
        return new ArangoDBConfiguration();
    }
    
    /**
     * 验证输入参数
     */
    private void validateInputs() {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        
        if (StringUtils.isBlank(collectionName)) {
            throw new IllegalArgumentException("Collection name cannot be null or empty");
        }
        
        if (param != null) {
            param.validate();
        }
    }
    
    /**
     * 初始化服务
     */
    public void init() {
        try {
            // 测试连接
            if (!client.ping()) {
                throw ArangoDBVectorStoreException.connectionError("Failed to ping ArangoDB", null);
            }
            
            // 创建集合
            client.getOrCreateCollection(collectionName);
            
            log.info("ArangoDB service initialized successfully");
            
        } catch (Exception e) {
            throw ArangoDBVectorStoreException.connectionError("Failed to initialize ArangoDB service", e);
        }
    }
    
    /**
     * 添加文档
     */
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("No documents to add");
            return;
        }
        
        try {
            log.info("Adding {} documents to ArangoDB", documents.size());
            
            // 转换文档为向量格式
            List<ArangoDBVector> vectors = documents.stream()
                    .map(this::convertDocumentToVector)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            if (vectors.isEmpty()) {
                log.warn("No valid vectors converted from documents");
                return;
            }
            
            // 批量插入
            insertVectorsBatch(vectors);
            
            log.info("Successfully added {} documents", vectors.size());
            
        } catch (Exception e) {
            log.error("Failed to add documents", e);
            throw ArangoDBVectorStoreException.insertError("Failed to add documents", e);
        }
    }
    
    /**
     * 相似度搜索
     */
    public List<Document> similaritySearch(List<Double> queryVector, int k, Double maxDistanceValue, Integer type) {
        if (queryVector == null || queryVector.isEmpty()) {
            log.warn("Query vector is null or empty");
            return new ArrayList<>();
        }
        
        if (k <= 0) {
            log.warn("Invalid k value: {}", k);
            return new ArrayList<>();
        }
        
        try {
            log.debug("Performing similarity search with k={}, maxDistance={}", k, maxDistanceValue);
            
            // 构建查询请求
            ArangoDBQueryRequest.ArangoDBQueryRequestBuilder requestBuilder = ArangoDBQueryRequest.builder()
                    .queryVector(queryVector)
                    .topK(Math.min(k, param.getInitParam().getMaxTopK()))
                    .similarityThreshold(param.getInitParam().getSimilarityThreshold())
                    .distanceFunction(param.getInitParam().getDistanceFunctionEnum())
                    .includeVector(param.getInitParam().isIncludeVector())
                    .includeMetadata(param.getInitParam().isIncludeMetadata());
            
            // 设置最大距离
            if (maxDistanceValue != null && maxDistanceValue > 0) {
                requestBuilder.maxDistance(maxDistanceValue);
            }
            
            // 设置类型过滤
            if (type != null) {
                requestBuilder.docTypeFilter(String.valueOf(type));
            }
            
            ArangoDBQueryRequest request = requestBuilder.build();
            
            // 执行查询
            ArangoDBQueryResponse response = client.querySimilarVectors(collectionName, request);
            
            if (!response.isSuccess()) {
                log.error("Query failed: {}", response.getErrorMessage());
                return new ArrayList<>();
            }
            
            // 转换结果
            List<Document> results = response.getResults().stream()
                    .map(this::convertVectorToDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            log.info("Found {} similar documents in {}ms", results.size(), response.getExecutionTimeMs());
            
            return results;
            
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 文本相似度搜索
     */
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        // 这个方法需要嵌入模型的支持，通常在 VectorStore 层实现
        throw new UnsupportedOperationException("Text similarity search should be implemented in VectorStore layer with embeddings");
    }
    
    /**
     * 批量插入向量
     */
    private void insertVectorsBatch(List<ArangoDBVector> vectors) {
        int batchSize = param.getInitParam().getBatchSize();
        
        for (int i = 0; i < vectors.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, vectors.size());
            List<ArangoDBVector> batch = vectors.subList(i, endIndex);
            
            try {
                client.insertVectors(collectionName, batch);
                log.debug("Inserted batch {}-{} of {} vectors", i + 1, endIndex, vectors.size());
                
            } catch (Exception e) {
                log.error("Failed to insert batch {}-{}", i + 1, endIndex, e);
                throw ArangoDBVectorStoreException.insertError("Failed to insert vector batch", e);
            }
        }
    }
    
    /**
     * 转换文档为向量格式
     */
    private ArangoDBVector convertDocumentToVector(Document document) {
        try {
            if (document == null) {
                return null;
            }
            
            // 检查必需字段
            if (StringUtils.isBlank(document.getPageContent())) {
                log.warn("Document has empty content, skipping");
                return null;
            }
            
            if (document.getEmbedding() == null || document.getEmbedding().isEmpty()) {
                log.warn("Document has no embedding, skipping");
                return null;
            }
            
            // 构建向量对象
            ArangoDBVector.ArangoDBVectorBuilder builder = ArangoDBVector.builder()
                    .content(document.getPageContent())
                    .vector(document.getEmbedding())
                    .dimension(document.getEmbedding().size())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now());
            
            // 设置唯一ID
            String uniqueId = document.getUniqueId();
            if (StringUtils.isBlank(uniqueId)) {
                uniqueId = UUID.randomUUID().toString();
            }
            builder.uniqueId(uniqueId);
            
            // 设置可选字段
            // Document类没有title字段，使用uniqueId作为title
            if (StringUtils.isNotBlank(document.getUniqueId())) {
                builder.title(document.getUniqueId());
            }
            
            if (document.getIndex() != null) {
                builder.docIndex(document.getIndex().toString());
            }
            
            // 设置元数据
            if (document.getMetadata() != null && !document.getMetadata().isEmpty()) {
                builder.metadata(new HashMap<>(document.getMetadata()));
            }
            
            // 构建并验证
            ArangoDBVector vector = builder.build();
            vector.validate();
            
            return vector;
            
        } catch (Exception e) {
            log.error("Failed to convert document to vector: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 转换向量为文档格式
     */
    private Document convertVectorToDocument(ArangoDBVector vector) {
        try {
            if (vector == null) {
                return null;
            }
            
            Document document = new Document();
            
            // 设置基础字段
            document.setPageContent(vector.getContent());
            document.setUniqueId(vector.getUniqueId());
            
            // 设置向量数据
            if (vector.getVector() != null && param.getInitParam().isIncludeVector()) {
                document.setEmbedding(new ArrayList<>(vector.getVector()));
            }
            
            // 设置可选字段
            // Document类没有setTitle方法，title信息放入metadata中
            
            if (StringUtils.isNotBlank(vector.getDocIndex())) {
                try {
                    document.setIndex(Integer.parseInt(vector.getDocIndex()));
                } catch (NumberFormatException e) {
                    // 如果无法解析为Integer，忽略
                }
            }
            
            // 设置元数据
            Map<String, Object> metadata = new HashMap<>();
            
            if (vector.getMetadata() != null) {
                metadata.putAll(vector.getMetadata());
            }
            
            // 添加向量存储特有的元数据
            if (vector.getScore() != null) {
                metadata.put("similarity_score", vector.getScore());
            }
            
            if (vector.getDistance() != null) {
                metadata.put("distance", vector.getDistance());
            }
            
            if (vector.getDimension() != null) {
                metadata.put("vector_dimension", vector.getDimension());
            }
            
            if (vector.getCreatedAt() != null) {
                metadata.put("created_at", vector.getCreatedAt().toString());
            }
            
            if (vector.getUpdatedAt() != null) {
                metadata.put("updated_at", vector.getUpdatedAt().toString());
            }
            
            if (vector.getDocType() != null) {
                metadata.put("doc_type", vector.getDocType());
            }
            
            if (vector.getTags() != null && !vector.getTags().isEmpty()) {
                metadata.put("tags", vector.getTags());
            }
            
            if (vector.getCustomFields() != null && !vector.getCustomFields().isEmpty()) {
                metadata.put("custom_fields", vector.getCustomFields());
            }
            
            document.setMetadata(metadata);
            
            return document;
            
        } catch (Exception e) {
            log.error("Failed to convert vector to document: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取集合统计信息
     */
    public Map<String, Object> getStats() {
        try {
            return client.getCollectionStats(collectionName);
        } catch (Exception e) {
            log.error("Failed to get collection stats", e);
            return Map.of("error", e.getMessage());
        }
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        try {
            client.clearCache();
            log.info("Cache cleared successfully");
        } catch (Exception e) {
            log.error("Failed to clear cache", e);
        }
    }
    
    /**
     * 测试连接
     */
    public boolean isHealthy() {
        try {
            return client.ping();
        } catch (Exception e) {
            log.error("Health check failed", e);
            return false;
        }
    }
    
    @Override
    public void close() {
        try {
            if (client != null) {
                client.close();
            }
            log.info("ArangoDB service closed");
        } catch (Exception e) {
            log.error("Error closing ArangoDB service", e);
        }
    }
}
