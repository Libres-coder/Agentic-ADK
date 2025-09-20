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
package com.alibaba.langengine.timescaledb.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.timescaledb.TimescaleDBConfiguration;
import com.alibaba.langengine.timescaledb.client.TimescaleDBClient;
import com.alibaba.langengine.timescaledb.exception.TimescaleDBException;
import com.alibaba.langengine.timescaledb.model.TimescaleDBQueryRequest;
import com.alibaba.langengine.timescaledb.model.TimescaleDBQueryResponse;
import com.alibaba.langengine.timescaledb.model.TimescaleDBVector;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class TimescaleDBVectorStore extends VectorStore implements AutoCloseable {

    /**
     * TimescaleDB客户端
     */
    private TimescaleDBClient client;

    /**
     * 嵌入模型
     */
    private Embeddings embedding;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 向量维度
     */
    private int vectorDimension;

    /**
     * 批次大小
     */
    private int batchSize;

    /**
     * 相似度阈值
     */
    private double similarityThreshold;

    /**
     * 最大缓存大小
     */
    private int maxCacheSize;

    /**
     * 文档缓存
     */
    private Map<String, Document> documentCache;

    /**
     * 嵌入缓存
     */
    private Map<String, List<Double>> embeddingCache;

    /**
     * 构造函数 - 使用默认配置
     */
    public TimescaleDBVectorStore(Embeddings embedding) {
        this(embedding, TimescaleDBConfiguration.DEFAULT_TABLE_NAME,
             TimescaleDBConfiguration.DEFAULT_VECTOR_DIMENSION);
    }

    /**
     * 构造函数 - 指定表名和维度
     */
    public TimescaleDBVectorStore(Embeddings embedding, String tableName, int vectorDimension) {
        this(embedding, tableName, vectorDimension,
             TimescaleDBConfiguration.DEFAULT_BATCH_SIZE,
             TimescaleDBConfiguration.DEFAULT_SIMILARITY_THRESHOLD,
             TimescaleDBConfiguration.DEFAULT_CACHE_SIZE);
    }

    /**
     * 构造函数 - 完整参数
     */
    public TimescaleDBVectorStore(Embeddings embedding, String tableName, int vectorDimension,
                                int batchSize, double similarityThreshold, int maxCacheSize) {
        if (embedding == null) {
            throw new IllegalArgumentException("Embeddings cannot be null");
        }
        if (StringUtils.isBlank(tableName)) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (vectorDimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive");
        }

        // 验证配置
        TimescaleDBConfiguration.validateConfiguration();

        this.embedding = embedding;
        this.tableName = tableName;
        this.vectorDimension = vectorDimension;
        this.batchSize = batchSize > 0 ? batchSize : TimescaleDBConfiguration.DEFAULT_BATCH_SIZE;
        this.similarityThreshold = similarityThreshold;
        this.maxCacheSize = maxCacheSize > 0 ? maxCacheSize : TimescaleDBConfiguration.DEFAULT_CACHE_SIZE;

        // 初始化缓存 - 使用LRU策略
        this.documentCache = Collections.synchronizedMap(new LinkedHashMap<String, Document>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Document> eldest) {
                return size() > maxCacheSize;
            }
        });
        
        this.embeddingCache = Collections.synchronizedMap(new LinkedHashMap<String, List<Double>>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, List<Double>> eldest) {
                return size() > maxCacheSize;
            }
        });

        // 初始化数据源
        DataSource dataSource = createDataSource();

        // 初始化客户端
        this.client = new TimescaleDBClient(dataSource, tableName, vectorDimension, batchSize,
                                          TimescaleDBConfiguration.DEFAULT_CONNECTION_TIMEOUT,
                                          TimescaleDBConfiguration.DEFAULT_QUERY_TIMEOUT);

        log.info("TimescaleDB vector store initialized: table={}, dimension={}, batchSize={}, cacheSize={}",
                tableName, vectorDimension, batchSize, maxCacheSize);
    }

    /**
     * 创建数据源
     */
    private DataSource createDataSource() {
        DruidDataSource dataSource = new DruidDataSource();

        String url = TimescaleDBConfiguration.getJdbcUrl();
        dataSource.setUrl(url);
        dataSource.setUsername(TimescaleDBConfiguration.getUsername());
        dataSource.setPassword(TimescaleDBConfiguration.getPassword());

        // 连接池配置
        dataSource.setInitialSize(TimescaleDBConfiguration.DEFAULT_INITIAL_CONNECTIONS);
        dataSource.setMaxActive(TimescaleDBConfiguration.DEFAULT_MAX_CONNECTIONS);
        dataSource.setMaxWait(TimescaleDBConfiguration.DEFAULT_CONNECTION_TIMEOUT);

        // 连接验证
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);

        try {
            dataSource.init();
        } catch (SQLException e) {
            throw new TimescaleDBException("Failed to initialize data source", e);
        }

        return dataSource;
    }

    /**
     * 初始化向量存储
     */
    public void init() {
        try {
            client.initialize();
            log.info("TimescaleDB vector store initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize TimescaleDB vector store", e);
            throw TimescaleDBException.sqlExecutionError("Failed to initialize vector store", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        try {
            log.info("Adding {} documents to TimescaleDB vector store", documents.size());

            // 生成嵌入向量
            List<Document> embeddedDocuments = generateEmbeddings(documents);

            // 转换为TimescaleDB向量格式
            List<TimescaleDBVector> vectors = embeddedDocuments.stream()
                    .map(this::convertDocumentToVector)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 批量插入
            if (!vectors.isEmpty()) {
                client.insertVectors(vectors);

                // 更新缓存
                for (Document document : embeddedDocuments) {
                    updateDocumentCache(document.getUniqueId(), document, document.getEmbedding());
                }

                log.info("Successfully added {} documents to TimescaleDB vector store", vectors.size());
            }

        } catch (Exception e) {
            log.error("Failed to add documents to TimescaleDB vector store", e);
            throw TimescaleDBException.writeError("Failed to insert documents", e);
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
                throw TimescaleDBException.vectorDimensionError(
                        String.format("Query vector dimension %d does not match expected dimension %d",
                                queryVector.size(), vectorDimension));
            }

            // 构建查询请求
            TimescaleDBQueryRequest request = TimescaleDBQueryRequest.builder()
                    .queryVector(queryVector)
                    .limit(k)
                    .similarityThreshold(maxDistanceValue != null ? maxDistanceValue : similarityThreshold)
                    .includeVectors(true)
                    .includeMetadata(true)
                    .build();

            // 执行查询
            TimescaleDBQueryResponse response = client.similaritySearch(request);

            if (!response.isSuccess() || response.isEmpty()) {
                log.warn("Similarity search returned no results for query: {}", query);
                return new ArrayList<>();
            }

            // 转换为Document格式
            return response.getVectors().stream()
                    .map(this::convertVectorToDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Similarity search failed for query: {}", query, e);
            throw TimescaleDBException.queryError("Similarity search failed", e);
        }
    }

    /**
     * 时序相似性搜索
     */
    public List<Document> similaritySearchWithTimeFilter(String query, int k,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        if (StringUtils.isBlank(query)) {
            return new ArrayList<>();
        }

        try {
            List<Double> queryVector = generateQueryEmbedding(query);
            if (queryVector == null || queryVector.isEmpty()) {
                return new ArrayList<>();
            }

            TimescaleDBQueryRequest request = TimescaleDBQueryRequest.builder()
                    .queryVector(queryVector)
                    .limit(k)
                    .timeRangeStart(startTime)
                    .timeRangeEnd(endTime)
                    .includeVectors(true)
                    .includeMetadata(true)
                    .build();

            TimescaleDBQueryResponse response = client.similaritySearch(request);

            return response.getVectors().stream()
                    .map(this::convertVectorToDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Time-filtered similarity search failed", e);
            throw TimescaleDBException.queryError("Time-filtered search failed", e);
        }
    }

    /**
     * 根据ID删除文档
     */
    public boolean deleteDocument(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }

        try {
            boolean deleted = client.deleteVector(id);
            if (deleted) {
                // 从缓存中移除
                documentCache.remove(id);
                embeddingCache.remove(id);
                log.info("Deleted document with id: {}", id);
            }
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete document: {}", id, e);
            throw TimescaleDBException.writeError("Failed to delete document", e);
        }
    }

    /**
     * 批量删除文档
     */
    public int deleteDocuments(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        try {
            int deletedCount = client.deleteVectors(ids);

            // 从缓存中移除
            for (String id : ids) {
                documentCache.remove(id);
                embeddingCache.remove(id);
            }

            log.info("Deleted {} documents", deletedCount);
            return deletedCount;

        } catch (Exception e) {
            log.error("Failed to delete documents", e);
            throw TimescaleDBException.writeError("Failed to delete documents", e);
        }
    }

    /**
     * 获取文档总数
     */
    public long countDocuments() {
        try {
            return client.countVectors();
        } catch (Exception e) {
            log.error("Failed to count documents", e);
            throw TimescaleDBException.queryError("Failed to count documents", e);
        }
    }

    /**
     * 清空所有数据
     */
    public void clear() {
        try {
            client.clearTable();
            documentCache.clear();
            embeddingCache.clear();
            log.info("TimescaleDB vector store cleared");
        } catch (Exception e) {
            log.error("Failed to clear vector store", e);
            throw TimescaleDBException.writeError("Failed to clear vector store", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStatistics() {
        return String.format("Document cache: %d/%d, Embedding cache: %d/%d",
                documentCache.size(), maxCacheSize,
                embeddingCache.size(), maxCacheSize);
    }

    /**
     * 生成文档嵌入向量
     */
    private List<Document> generateEmbeddings(List<Document> documents) {
        if (embedding == null) {
            throw TimescaleDBException.configurationError("Embeddings model not configured");
        }

        try {
            return embedding.embedDocument(documents);
        } catch (Exception e) {
            throw TimescaleDBException.dataFormatError("Failed to generate embeddings", e);
        }
    }

    /**
     * 生成查询嵌入向量
     */
    private List<Double> generateQueryEmbedding(String query) {
        if (embedding == null) {
            throw TimescaleDBException.configurationError("Embeddings model not configured");
        }

        try {
            List<String> embeddingStrings = embedding.embedQuery(query, 1);
            if (embeddingStrings.isEmpty()) {
                return null;
            }

            String embeddingString = embeddingStrings.get(0);
            if (embeddingString.startsWith("[") && embeddingString.endsWith("]")) {
                String cleaned = embeddingString.substring(1, embeddingString.length() - 1);
                String[] parts = cleaned.split(",");
                return Arrays.stream(parts)
                        .map(String::trim)
                        .map(Double::parseDouble)
                        .collect(Collectors.toList());
            }

            return null;
        } catch (Exception e) {
            log.error("Failed to generate query embedding", e);
            return null;
        }
    }

    /**
     * 将Document转换为TimescaleDBVector
     */
    private TimescaleDBVector convertDocumentToVector(Document document) {
        try {
            TimescaleDBVector vector = new TimescaleDBVector();
            vector.setId(document.getUniqueId());
            vector.setContent(document.getPageContent());
            vector.setVector(document.getEmbedding());
            vector.setVectorDimension(vectorDimension);
            vector.setMetadata(document.getMetadata());
            vector.setDocIndex(document.getIndex());
            vector.setDefaultTimestamp();

            return vector;
        } catch (Exception e) {
            log.error("Failed to convert document to vector: {}", document.getUniqueId(), e);
            return null;
        }
    }

    /**
     * 将TimescaleDBVector转换为Document
     */
    private Document convertVectorToDocument(TimescaleDBVector vector) {
        try {
            Document document = new Document();
            document.setUniqueId(vector.getId());
            document.setPageContent(vector.getContent());
            document.setEmbedding(vector.getVector());
            document.setMetadata(vector.getSafeMetadata());
            document.setIndex(vector.getDocIndex());
            document.setScore(vector.getScore());

            return document;
        } catch (Exception e) {
            log.error("Failed to convert vector to document: {}", vector.getId(), e);
            return null;
        }
    }

    /**
     * 更新文档缓存
     */
    private void updateDocumentCache(String docId, Document document, List<Double> embedding) {
        // LRU策略由LinkedHashMap自动处理
        documentCache.put(docId, document);

        if (embedding != null) {
            embeddingCache.put(docId, embedding);
        }
    }

    /**
     * 验证向量维度
     */
    private boolean isValidVectorDimension(List<Double> vector) {
        return vector != null && vector.size() == vectorDimension;
    }

    @Override
    public void close() {
        try {
            if (client != null) {
                client.close();
            }

            // 清理缓存
            documentCache.clear();
            embeddingCache.clear();

            log.info("TimescaleDB vector store closed successfully");
        } catch (Exception e) {
            log.error("Error closing TimescaleDB vector store", e);
        }
    }
}
