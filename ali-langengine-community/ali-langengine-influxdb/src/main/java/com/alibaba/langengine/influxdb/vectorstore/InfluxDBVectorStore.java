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
package com.alibaba.langengine.influxdb.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.influxdb.InfluxDBConfiguration;
import com.alibaba.langengine.influxdb.InfluxDBConstants;
import com.alibaba.langengine.influxdb.client.InfluxDBVectorClient;
import com.alibaba.langengine.influxdb.exception.InfluxDBVectorStoreException;
import com.alibaba.langengine.influxdb.model.InfluxDBQueryRequest;
import com.alibaba.langengine.influxdb.model.InfluxDBQueryResponse;
import com.alibaba.langengine.influxdb.model.InfluxDBVector;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Data
@EqualsAndHashCode(callSuper = false)
public class InfluxDBVectorStore extends VectorStore implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(InfluxDBVectorStore.class);

    /**
     * InfluxDB客户端
     */
    private InfluxDBVectorClient client;

    /**
     * 嵌入模型
     */
    private Embeddings embedding;

    /**
     * 测量名称
     */
    private String measurement;

    /**
     * 向量维度
     */
    private int vectorDimension;

    /**
     * 相似度阈值
     */
    private double similarityThreshold;

    /**
     * 相似度计算方法
     */
    private InfluxDBQueryRequest.SimilarityMetric similarityMetric;

    /**
     * 最大缓存大小
     */
    private int maxCacheSize;

    /**
     * 文档缓存
     */
    private final Map<String, Document> documentCache;

    /**
     * 向量缓存
     */
    private final Map<String, List<Double>> embeddingCache;

    /**
     * 批量写入大小
     */
    private int batchSize;

    /**
     * 自动刷新间隔（毫秒）
     */
    private long flushInterval;

    /**
     * 默认构造函数
     */
    public InfluxDBVectorStore() {
        this(InfluxDBConstants.INFLUXDB_URL,
             InfluxDBConstants.INFLUXDB_TOKEN,
             InfluxDBConstants.INFLUXDB_ORG,
             InfluxDBConstants.INFLUXDB_BUCKET);
    }

    /**
     * 构造函数
     *
     * @param url    InfluxDB服务器URL
     * @param token  访问令牌
     * @param org    组织名称
     * @param bucket 存储桶名称
     */
    public InfluxDBVectorStore(String url, String token, String org, String bucket) {
        this(url, token, org, bucket, InfluxDBConstants.DEFAULT_MEASUREMENT);
    }

    /**
     * 构造函数
     *
     * @param url         InfluxDB服务器URL
     * @param token       访问令牌
     * @param org         组织名称
     * @param bucket      存储桶名称
     * @param measurement 测量名称
     */
    public InfluxDBVectorStore(String url, String token, String org, String bucket, String measurement) {
        this(url, token, org, bucket, measurement, 
             InfluxDBConstants.DEFAULT_VECTOR_DIMENSION,
             InfluxDBConstants.DEFAULT_SIMILARITY_THRESHOLD);
    }

    /**
     * 完整构造函数
     *
     * @param url                 InfluxDB服务器URL
     * @param token               访问令牌
     * @param org                 组织名称
     * @param bucket              存储桶名称
     * @param measurement         测量名称
     * @param vectorDimension     向量维度
     * @param similarityThreshold 相似度阈值
     */
    public InfluxDBVectorStore(String url, String token, String org, String bucket, 
                              String measurement, int vectorDimension, double similarityThreshold) {
        this.client = new InfluxDBVectorClient(url, token, org, bucket, 1000);
        this.measurement = StringUtils.isNotBlank(measurement) ? measurement : InfluxDBConstants.DEFAULT_MEASUREMENT;
        this.vectorDimension = vectorDimension > 0 ? vectorDimension : InfluxDBConstants.DEFAULT_VECTOR_DIMENSION;
        this.similarityThreshold = similarityThreshold;
        this.similarityMetric = InfluxDBQueryRequest.SimilarityMetric.COSINE;
        this.maxCacheSize = 1000;
        this.batchSize = InfluxDBConstants.BATCH_SIZE;
        this.flushInterval = InfluxDBConstants.FLUSH_INTERVAL;
        this.documentCache = new ConcurrentHashMap<>();
        this.embeddingCache = new ConcurrentHashMap<>();

        log.info("InfluxDB vector store initialized: measurement={}, dimension={}, threshold={}", 
                measurement, vectorDimension, similarityThreshold);
    }

    /**
     * 从配置创建向量存储
     *
     * @param config InfluxDB配置
     */
    public InfluxDBVectorStore(InfluxDBConfiguration config) {
        this(config.getUrl(), config.getToken(), config.getOrg(), config.getBucket(),
             InfluxDBConstants.DEFAULT_MEASUREMENT, config.getDefaultVectorDimension(), 
             config.getDefaultSimilarityThreshold());
    }

    /**
     * 初始化向量存储
     */
    public void init() {
        try {
            if (!client.ping()) {
                throw InfluxDBVectorStoreException.connectionError("Failed to connect to InfluxDB", null);
            }
            log.info("InfluxDB vector store initialization completed successfully");
        } catch (Exception e) {
            throw InfluxDBVectorStoreException.connectionError("Failed to initialize InfluxDB vector store", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        try {
            log.info("Adding {} documents to InfluxDB vector store", documents.size());

            // 生成嵌入向量
            List<Document> embeddedDocuments = generateEmbeddings(documents);

            // 转换为InfluxDB向量格式
            List<InfluxDBVector> vectors = embeddedDocuments.stream()
                    .map(this::convertDocumentToVector)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 批量插入
            insertVectorsBatch(vectors);

            // 更新缓存
            for (Document document : embeddedDocuments) {
                updateDocumentCache(document.getUniqueId(), document, document.getEmbedding());
            }

            log.info("Successfully added {} documents to InfluxDB vector store", vectors.size());

        } catch (Exception e) {
            log.error("Failed to add documents to InfluxDB vector store", e);
            throw InfluxDBVectorStoreException.writeError("Failed to insert documents", e);
        }
    }

    /**
     * 添加文档和向量（测试兼容方法）
     *
     * @param documents 文档列表
     * @param embeddings 向量列表
     */
    public void addDocuments(List<Document> documents, List<List<Double>> embeddings) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        try {
            log.info("Adding {} documents with predefined embeddings to InfluxDB vector store", documents.size());

            // 验证文档和向量数量一致
            if (embeddings != null && documents.size() != embeddings.size()) {
                throw new IllegalArgumentException("Documents and embeddings size mismatch");
            }

            // 设置预定义的嵌入向量
            List<Document> documentsWithEmbeddings = new ArrayList<>();
            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);
                if (embeddings != null && i < embeddings.size()) {
                    doc.setEmbedding(embeddings.get(i));
                }
                documentsWithEmbeddings.add(doc);
            }

            // 转换为InfluxDB向量格式
            List<InfluxDBVector> vectors = documentsWithEmbeddings.stream()
                    .map(this::convertDocumentToVector)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 批量插入
            insertVectorsBatch(vectors);

            // 更新缓存
            for (int i = 0; i < documentsWithEmbeddings.size(); i++) {
                Document doc = documentsWithEmbeddings.get(i);
                List<Double> embedding = (embeddings != null && i < embeddings.size()) ? embeddings.get(i) : doc.getEmbedding();
                updateDocumentCache(doc.getUniqueId(), doc, embedding);
            }

            log.info("Successfully added {} documents with predefined embeddings to InfluxDB vector store", vectors.size());
        } catch (Exception e) {
            log.error("Failed to add documents with embeddings to InfluxDB vector store", e);
            throw InfluxDBVectorStoreException.writeError("Failed to insert documents with embeddings", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isBlank(query)) {
            return new ArrayList<>();
        }

        try {
            // 生成查询向量
            List<Double> queryVector = generateQueryEmbedding(query);
            if (queryVector == null || queryVector.isEmpty()) {
                log.warn("Failed to generate query embedding for: {}", query);
                return new ArrayList<>();
            }

            // 验证向量维度
            if (queryVector.size() != vectorDimension) {
                throw InfluxDBVectorStoreException.vectorDimensionError(
                        String.format("Query vector dimension %d does not match expected dimension %d", 
                                queryVector.size(), vectorDimension));
            }

            // 构建查询请求
            InfluxDBQueryRequest.InfluxDBQueryRequestBuilder requestBuilder = InfluxDBQueryRequest.builder()
                    .queryVector(queryVector)
                    .limit(k)
                    .measurement(measurement)
                    .similarityMetric(similarityMetric)
                    .includeMetadata(true);

            if (maxDistanceValue != null) {
                requestBuilder.similarityThreshold(1.0 - maxDistanceValue); // 距离转换为相似度
            } else {
                requestBuilder.similarityThreshold(similarityThreshold);
            }

            InfluxDBQueryRequest request = requestBuilder.build();

            // 执行查询
            InfluxDBQueryResponse response = client.querySimilarVectors(request);

            if (!response.isSuccess()) {
                throw InfluxDBVectorStoreException.queryError("Query failed: " + response.getErrorMessage(), null);
            }

            // 转换结果
            List<Document> results = response.getResults().stream()
                    .map(this::convertVectorToDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.debug("Similarity search returned {} results for query: {}", results.size(), query);
            return results;

        } catch (Exception e) {
            throw InfluxDBVectorStoreException.queryError("Failed to perform similarity search", e);
        }
    }

    /**
     * 相似度搜索（使用向量）
     *
     * @param queryVector 查询向量
     * @param k 返回结果数量
     * @return 相似文档列表
     */
    public List<Document> similaritySearch(List<Double> queryVector, int k) {
        if (queryVector == null || queryVector.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 验证向量维度
            if (queryVector.size() != vectorDimension) {
                throw InfluxDBVectorStoreException.vectorDimensionError(
                        String.format("Query vector dimension %d does not match expected dimension %d", 
                                queryVector.size(), vectorDimension));
            }

            // 构建查询请求
            InfluxDBQueryRequest request = InfluxDBQueryRequest.builder()
                    .queryVector(queryVector)
                    .limit(k)
                    .measurement(measurement)
                    .similarityMetric(similarityMetric)
                    .includeMetadata(true)
                    .similarityThreshold(similarityThreshold)
                    .build();

            // 执行查询
            InfluxDBQueryResponse response = client.querySimilarVectors(request);

            if (!response.isSuccess()) {
                throw InfluxDBVectorStoreException.queryError("Query failed: " + response.getErrorMessage(), null);
            }

            // 转换结果
            List<Document> results = response.getResults().stream()
                    .map(this::convertVectorToDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.debug("Similarity search returned {} results", results.size());
            return results;

        } catch (Exception e) {
            throw InfluxDBVectorStoreException.queryError("Failed to perform similarity search", e);
        }
    }

    /**
     * 带元数据过滤的相似度搜索
     *
     * @param queryVector 查询向量
     * @param k 返回结果数量
     * @param metadata 元数据过滤条件
     * @return 相似文档列表
     */
    public List<Document> similaritySearchWithMetadata(List<Double> queryVector, int k, Map<String, Object> metadata) {
        if (queryVector == null || queryVector.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 验证向量维度
            if (queryVector.size() != vectorDimension) {
                throw InfluxDBVectorStoreException.vectorDimensionError(
                        String.format("Query vector dimension %d does not match expected dimension %d", 
                                queryVector.size(), vectorDimension));
            }

            // 构建查询请求
            InfluxDBQueryRequest.InfluxDBQueryRequestBuilder requestBuilder = InfluxDBQueryRequest.builder()
                    .queryVector(queryVector)
                    .limit(k)
                    .measurement(measurement)
                    .similarityMetric(similarityMetric)
                    .includeMetadata(true)
                    .similarityThreshold(similarityThreshold);

            // 添加元数据过滤
            if (metadata != null && !metadata.isEmpty()) {
                requestBuilder.fieldFilters(metadata);
            }

            InfluxDBQueryRequest request = requestBuilder.build();

            // 执行查询
            InfluxDBQueryResponse response = client.querySimilarVectors(request);

            if (!response.isSuccess()) {
                throw InfluxDBVectorStoreException.queryError("Query failed: " + response.getErrorMessage(), null);
            }

            // 转换结果
            List<Document> results = response.getResults().stream()
                    .map(this::convertVectorToDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.debug("Similarity search with metadata returned {} results", results.size());
            return results;

        } catch (Exception e) {
            throw InfluxDBVectorStoreException.queryError("Failed to perform similarity search with metadata", e);
        }
    }

    /**
     * 带分数的相似度搜索
     *
     * @param queryVector 查询向量
     * @param k 返回结果数量
     * @param scoreThreshold 分数阈值
     * @return 相似文档列表
     */
    public List<Document> similaritySearchWithScore(List<Double> queryVector, int k, double scoreThreshold) {
        if (queryVector == null || queryVector.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 验证向量维度
            if (queryVector.size() != vectorDimension) {
                throw InfluxDBVectorStoreException.vectorDimensionError(
                        String.format("Query vector dimension %d does not match expected dimension %d", 
                                queryVector.size(), vectorDimension));
            }

            // 构建查询请求
            InfluxDBQueryRequest request = InfluxDBQueryRequest.builder()
                    .queryVector(queryVector)
                    .limit(k)
                    .measurement(measurement)
                    .similarityMetric(similarityMetric)
                    .includeMetadata(true)
                    .similarityThreshold(scoreThreshold)
                    .build();

            // 执行查询
            InfluxDBQueryResponse response = client.querySimilarVectors(request);

            if (!response.isSuccess()) {
                throw InfluxDBVectorStoreException.queryError("Query failed: " + response.getErrorMessage(), null);
            }

            // 转换结果并设置分数
            List<Document> results = response.getResults().stream()
                    .map(vector -> {
                        Document doc = convertVectorToDocument(vector);
                        if (doc != null && vector.getScore() != null) {
                            // 将分数设置到文档的元数据中
                            Map<String, Object> docMetadata = doc.getMetadata();
                            if (docMetadata == null) {
                                docMetadata = new HashMap<>();
                                doc.setMetadata(docMetadata);
                            }
                            docMetadata.put("score", vector.getScore());
                        }
                        return doc;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.debug("Similarity search with score returned {} results", results.size());
            return results;

        } catch (Exception e) {
            throw InfluxDBVectorStoreException.queryError("Failed to perform similarity search with score", e);
        }
    }

    /**
     * 删除文档
     *
     * @param docId 文档ID
     */
    public void deleteDocument(String docId) {
        try {
            client.deleteVector(docId, measurement);
            documentCache.remove(docId);
            embeddingCache.remove(docId);
            log.debug("Successfully deleted document: {}", docId);
        } catch (Exception e) {
            throw InfluxDBVectorStoreException.writeError("Failed to delete document", e);
        }
    }

    /**
     * 获取文档数量
     *
     * @return 文档数量
     */
    public int getDocumentCount() {
        return documentCache.size();
    }

    /**
     * 清空所有数据
     */
    public void clear() {
        try {
            documentCache.clear();
            embeddingCache.clear();
            log.info("InfluxDB vector store cleared");
        } catch (Exception e) {
            log.error("Failed to clear vector store", e);
        }
    }

    /**
     * 生成文档嵌入向量
     *
     * @param documents 文档列表
     * @return 包含嵌入向量的文档列表
     */
    private List<Document> generateEmbeddings(List<Document> documents) {
        if (embedding == null) {
            throw InfluxDBVectorStoreException.configurationError("Embeddings model not configured");
        }

        try {
            return embedding.embedDocument(documents);
        } catch (Exception e) {
            throw InfluxDBVectorStoreException.dataFormatError("Failed to generate embeddings", e);
        }
    }

    /**
     * 生成查询嵌入向量
     *
     * @param query 查询文本
     * @return 嵌入向量
     */
    private List<Double> generateQueryEmbedding(String query) {
        if (embedding == null) {
            throw InfluxDBVectorStoreException.configurationError("Embeddings model not configured");
        }

        try {
            List<String> embeddingStrings = embedding.embedQuery(query, 1);
            if (embeddingStrings.isEmpty()) {
                return null;
            }

            String embeddingString = embeddingStrings.get(0);
            if (embeddingString.startsWith("[") && embeddingString.endsWith("]")) {
                // JSON格式解析
                String cleaned = embeddingString.substring(1, embeddingString.length() - 1);
                String[] parts = cleaned.split(",");
                List<Double> embedding = new ArrayList<>();
                for (String part : parts) {
                    embedding.add(Double.parseDouble(part.trim()));
                }
                return embedding;
            } else {
                // 空格分隔格式解析
                String[] parts = embeddingString.trim().split("\\s+");
                List<Double> embedding = new ArrayList<>();
                for (String part : parts) {
                    embedding.add(Double.parseDouble(part));
                }
                return embedding;
            }
        } catch (Exception e) {
            log.error("Failed to generate query embedding", e);
            return null;
        }
    }

    /**
     * 将文档转换为InfluxDB向量
     *
     * @param document 文档
     * @return InfluxDB向量
     */
    private InfluxDBVector convertDocumentToVector(Document document) {
        try {
            if (document.getEmbedding() == null || document.getEmbedding().isEmpty()) {
                log.warn("Document {} has no embedding", document.getUniqueId());
                return null;
            }

            return InfluxDBVector.builder()
                    .id(document.getUniqueId())
                    .vector(document.getEmbedding())
                    .content(document.getPageContent())
                    .metadata(document.getMetadata())
                    .timestamp(Instant.now())
                    .measurement(measurement)
                    .build();
        } catch (Exception e) {
            log.error("Failed to convert document to vector: {}", document.getUniqueId(), e);
            return null;
        }
    }

    /**
     * 将InfluxDB向量转换为文档
     *
     * @param vector InfluxDB向量
     * @return 文档
     */
    private Document convertVectorToDocument(InfluxDBVector vector) {
        try {
            Document document = new Document();
            document.setUniqueId(vector.getId());
            document.setPageContent(vector.getContent());
            document.setMetadata(vector.getMetadata());
            document.setScore(vector.getScore());
            document.setEmbedding(vector.getVector());
            return document;
        } catch (Exception e) {
            log.error("Failed to convert vector to document: {}", vector.getId(), e);
            return null;
        }
    }

    /**
     * 批量插入向量
     *
     * @param vectors 向量列表
     */
    private void insertVectorsBatch(List<InfluxDBVector> vectors) {
        if (vectors.isEmpty()) {
            return;
        }

        try {
            // 按批次大小分组插入
            for (int i = 0; i < vectors.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, vectors.size());
                List<InfluxDBVector> batch = vectors.subList(i, endIndex);
                client.insertVectors(batch);
                
                log.debug("Inserted batch {}/{} ({} vectors)", 
                        i / batchSize + 1, 
                        (vectors.size() + batchSize - 1) / batchSize, 
                        batch.size());
            }
        } catch (Exception e) {
            throw InfluxDBVectorStoreException.writeError("Failed to insert vectors in batch", e);
        }
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
        if (embedding != null) {
            embeddingCache.put(docId, embedding);
        }
    }

    /**
     * 管理缓存大小
     */
    private void manageCacheSize() {
        if (documentCache.size() >= maxCacheSize) {
            // 简单的LRU策略：清除最早的一半条目
            Iterator<Map.Entry<String, Document>> iterator = documentCache.entrySet().iterator();
            int removeCount = maxCacheSize / 2;
            while (iterator.hasNext() && removeCount-- > 0) {
                String key = iterator.next().getKey();
                iterator.remove();
                embeddingCache.remove(key);
            }
        }
    }

    /**
     * 验证向量维度
     *
     * @param vector 向量
     * @return 是否有效
     */
    private boolean isValidVectorDimension(List<Double> vector) {
        return vector != null && vector.size() == vectorDimension;
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public String getCacheStatistics() {
        return String.format("Document cache: %d/%d, Embedding cache: %d/%d",
                documentCache.size(), maxCacheSize,
                embeddingCache.size(), maxCacheSize);
    }

    /**
     * 关闭资源
     */
    @Override
    public void close() {
        try {
            if (client != null) {
                client.close();
            }
            documentCache.clear();
            embeddingCache.clear();
            log.info("InfluxDB vector store closed successfully");
        } catch (Exception e) {
            log.error("Error closing InfluxDB vector store", e);
        }
    }
}
