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
package com.alibaba.langengine.janusgraph.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.VertexLabel;
import org.janusgraph.core.attribute.Geoshape;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.graphdb.database.management.ManagementSystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Slf4j
@Data
public class JanusGraphService implements AutoCloseable {

    private JanusGraph graph;
    private Client gremlinClient;
    private Cluster gremlinCluster;
    private JanusGraphParam janusGraphParam;
    private ExecutorService executorService;
    private volatile boolean initialized = false;

    public JanusGraphService(JanusGraphParam janusGraphParam) {
        this.janusGraphParam = janusGraphParam != null ? janusGraphParam : JanusGraphParam.getDefaultConfig();
        
        // 确保批处理配置不为空
        JanusGraphParam.BatchConfig batchConfig = this.janusGraphParam.getBatchConfig();
        if (batchConfig == null) {
            batchConfig = new JanusGraphParam.BatchConfig();
        }
        
        this.executorService = Executors.newFixedThreadPool(batchConfig.getParallelThreads());
        
        try {
            initializeGraph();
            log.info("JanusGraph Service initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize JanusGraph Service", e);
            throw new RuntimeException("Failed to initialize JanusGraph", e);
        }
    }

    /**
     * 初始化图数据库连接
     */
    private void initializeGraph() throws Exception {
        Map<String, Object> configMap = janusGraphParam.toJanusGraphConfig();
        
        // 将Map转换为Configuration
        Configuration config = new BaseConfiguration();
        configMap.forEach((key, value) -> config.setProperty(key, value));
        this.graph = JanusGraphFactory.open(config);
        
        // 验证连接
        verifyConnection();
        
        // 初始化Gremlin客户端（如果需要远程连接）
        initializeGremlinClient();
        
        this.initialized = true;
        log.info("JanusGraph initialized with config: {}", JSON.toJSONString(configMap));
    }

    /**
     * 初始化Gremlin客户端
     */
    private void initializeGremlinClient() {
        try {
            JanusGraphParam.ConnectionConfig connConfig = janusGraphParam.getConnectionConfig();
            if (connConfig != null && connConfig.getEnableConnectionPool()) {
                // 创建Gremlin集群配置
                Cluster.Builder clusterBuilder = Cluster.build()
                        .maxConnectionPoolSize(connConfig.getMaxConnectionPoolSize())
                        .minConnectionPoolSize(connConfig.getMinConnectionPoolSize())
                        .maxWaitForConnection(connConfig.getConnectionTimeoutSeconds() * 1000)
                        .keepAliveInterval(60000);

                this.gremlinCluster = clusterBuilder.create();
                this.gremlinClient = gremlinCluster.connect();
                
                log.info("Gremlin client initialized with connection pool");
            }
        } catch (Exception e) {
            log.warn("Failed to initialize Gremlin client, using direct graph access: {}", e.getMessage());
        }
    }

    /**
     * 验证数据库连接
     */
    public void verifyConnection() {
        try {
            // 执行简单查询验证连接
            JanusGraphTransaction tx = graph.newTransaction();
            try {
                tx.traversal().V().limit(1).hasNext();
                tx.commit();
                log.info("JanusGraph connection verified successfully");
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        } catch (Exception e) {
            log.error("JanusGraph connection verification failed", e);
            throw new RuntimeException("JanusGraph connection verification failed", e);
        }
    }

    /**
     * 初始化Schema和索引
     */
    public void initializeSchema(Embeddings embedding) {
        if (!janusGraphParam.getInitParam().getCreateSchemaOnInit()) {
            log.info("Schema creation is disabled, skipping schema initialization");
            return;
        }

        try {
            JanusGraphManagement mgmt = graph.openManagement();
            JanusGraphParam.VectorConfig vectorConfig = janusGraphParam.getVectorConfig();
            
            // 创建顶点标签
            VertexLabel documentLabel = createVertexLabelIfNotExists(mgmt, vectorConfig.getVertexLabel());
            
            // 创建属性键
            PropertyKey idKey = createPropertyKeyIfNotExists(mgmt, vectorConfig.getIdPropertyName(), String.class);
            PropertyKey contentKey = createPropertyKeyIfNotExists(mgmt, vectorConfig.getContentPropertyName(), String.class);
            PropertyKey vectorKey = createPropertyKeyIfNotExists(mgmt, vectorConfig.getVectorPropertyName(), String.class);
            PropertyKey metadataKey = createPropertyKeyIfNotExists(mgmt, vectorConfig.getMetadataPropertyName(), String.class);
            
            // 创建索引
            if (janusGraphParam.getInitParam().getCreateIndexOnInit()) {
                createIndexes(mgmt, vectorConfig, idKey, contentKey, vectorKey);
            }
            
            mgmt.commit();
            
            // 等待索引启用
            if (janusGraphParam.getInitParam().getCreateIndexOnInit()) {
                waitForIndexes();
            }
            
            log.info("Schema and indexes initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize schema", e);
            throw new RuntimeException("Failed to initialize schema", e);
        }
    }

    /**
     * 创建顶点标签
     */
    private VertexLabel createVertexLabelIfNotExists(JanusGraphManagement mgmt, String labelName) {
        VertexLabel label = mgmt.getVertexLabel(labelName);
        if (label == null) {
            label = mgmt.makeVertexLabel(labelName).make();
            log.info("Created vertex label: {}", labelName);
        }
        return label;
    }

    /**
     * 创建属性键
     */
    private PropertyKey createPropertyKeyIfNotExists(JanusGraphManagement mgmt, String keyName, Class<?> dataType) {
        PropertyKey key = mgmt.getPropertyKey(keyName);
        if (key == null) {
            key = mgmt.makePropertyKey(keyName).dataType(dataType).make();
            log.info("Created property key: {} with type: {}", keyName, dataType.getSimpleName());
        }
        return key;
    }

    /**
     * 创建索引
     */
    private void createIndexes(JanusGraphManagement mgmt, JanusGraphParam.VectorConfig vectorConfig,
                              PropertyKey idKey, PropertyKey contentKey, PropertyKey vectorKey) {
        
        JanusGraphParam.IndexConfig indexConfig = janusGraphParam.getIndexConfig();
        
        // 创建唯一ID索引
        String idIndexName = vectorConfig.getVertexLabel() + "_id_index";
        if (mgmt.getGraphIndex(idIndexName) == null) {
            mgmt.buildIndex(idIndexName, Vertex.class)
                .addKey(idKey)
                .unique()
                .buildCompositeIndex();
            log.info("Created unique ID index: {}", idIndexName);
        }

        // 创建混合索引（用于全文搜索和向量搜索）
        if (indexConfig.getEnableMixedIndex()) {
            String mixedIndexName = indexConfig.getVectorIndexName();
            if (mgmt.getGraphIndex(mixedIndexName) == null) {
                mgmt.buildIndex(mixedIndexName, Vertex.class)
                    .addKey(contentKey)
                    .addKey(vectorKey)
                    .buildMixedIndex(indexConfig.getIndexBackend());
                log.info("Created mixed index: {} on backend: {}", 
                    mixedIndexName, indexConfig.getIndexBackend());
            }
        }
    }

    /**
     * 等待索引启用
     */
    private void waitForIndexes() {
        try {
            JanusGraphParam.IndexConfig indexConfig = janusGraphParam.getIndexConfig();
            JanusGraphParam.VectorConfig vectorConfig = janusGraphParam.getVectorConfig();
            
            String idIndexName = vectorConfig.getVertexLabel() + "_id_index";
            String mixedIndexName = indexConfig.getVectorIndexName();
            
            // 等待索引启用
            ManagementSystem.awaitGraphIndexStatus(graph, idIndexName)
                .timeout(2, ChronoUnit.MINUTES)
                .call();
                
            if (indexConfig.getEnableMixedIndex()) {
                ManagementSystem.awaitGraphIndexStatus(graph, mixedIndexName)
                    .timeout(2, ChronoUnit.MINUTES)
                    .call();
            }
            
            log.info("All indexes are now enabled");
        } catch (Exception e) {
            log.error("Failed to wait for indexes", e);
            throw new RuntimeException("Failed to wait for indexes", e);
        }
    }

    /**
     * 添加文档到图数据库
     */
    public List<String> addDocuments(List<Document> documents, Embeddings embedding) {
        if (CollectionUtils.isEmpty(documents)) {
            return new ArrayList<>();
        }

        List<String> documentIds = new ArrayList<>();
        JanusGraphParam.BatchConfig batchConfig = janusGraphParam.getBatchConfig();
        
        if (batchConfig.getEnableBatchCommit() && documents.size() > batchConfig.getBatchSize()) {
            // 批量处理
            return addDocumentsInBatches(documents, embedding);
        } else {
            // 单事务处理
            return addDocumentsInSingleTransaction(documents, embedding);
        }
    }

    /**
     * 批量添加文档
     */
    private List<String> addDocumentsInBatches(List<Document> documents, Embeddings embedding) {
        List<String> allDocumentIds = new ArrayList<>();
        JanusGraphParam.BatchConfig batchConfig = janusGraphParam.getBatchConfig();
        int batchSize = batchConfig.getBatchSize();
        
        List<List<Document>> batches = partitionDocuments(documents, batchSize);
        List<Future<List<String>>> futures = new ArrayList<>();
        
        for (List<Document> batch : batches) {
            Future<List<String>> future = executorService.submit(() -> 
                addDocumentsInSingleTransaction(batch, embedding));
            futures.add(future);
        }
        
        // 收集结果
        for (Future<List<String>> future : futures) {
            try {
                allDocumentIds.addAll(future.get());
            } catch (Exception e) {
                log.error("Failed to process document batch", e);
                throw new RuntimeException("Failed to process document batch", e);
            }
        }
        
        return allDocumentIds;
    }

    /**
     * 单事务添加文档
     */
    private List<String> addDocumentsInSingleTransaction(List<Document> documents, Embeddings embedding) {
        List<String> documentIds = new ArrayList<>();
        JanusGraphTransaction tx = graph.newTransaction();
        
        try {
            JanusGraphParam.VectorConfig vectorConfig = janusGraphParam.getVectorConfig();
            
            for (Document document : documents) {
                String docId = generateDocumentId(document);
                
                // 生成向量
                List<String> vectorStrings = embedding.embedQuery(document.getPageContent(), 
                    janusGraphParam.getVectorConfig().getVectorDimension());
                List<Double> vector = vectorStrings.stream()
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
                
                // 创建顶点
                Vertex vertex = tx.addVertex(vectorConfig.getVertexLabel());
                vertex.property(vectorConfig.getIdPropertyName(), docId);
                vertex.property(vectorConfig.getContentPropertyName(), document.getPageContent());
                vertex.property(vectorConfig.getVectorPropertyName(), JSON.toJSONString(vector));
                
                if (document.getMetadata() != null && !document.getMetadata().isEmpty()) {
                    vertex.property(vectorConfig.getMetadataPropertyName(), JSON.toJSONString(document.getMetadata()));
                }
                
                documentIds.add(docId);
                
                log.debug("Added document vertex with ID: {}", docId);
            }
            
            tx.commit();
            log.info("Successfully added {} documents to JanusGraph", documents.size());
            
        } catch (Exception e) {
            tx.rollback();
            log.error("Failed to add documents to JanusGraph", e);
            throw new RuntimeException("Failed to add documents to JanusGraph", e);
        }
        
        return documentIds;
    }

    /**
     * 向量相似度搜索
     */
    public List<Document> similaritySearch(String query, Embeddings embedding, int k, 
                                         Double maxDistanceValue, Integer type) {
        try {
            // 生成查询向量
            List<String> queryVectorStrings = embedding.embedQuery(query, 
                janusGraphParam.getVectorConfig().getVectorDimension());
            List<Double> queryVector = queryVectorStrings.stream()
                .map(Double::parseDouble)
                .collect(Collectors.toList());
            
            // 执行相似度搜索
            return performVectorSearch(queryVector, k, maxDistanceValue);
            
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new RuntimeException("Failed to perform similarity search", e);
        }
    }

    /**
     * 执行向量搜索
     */
    private List<Document> performVectorSearch(List<Double> queryVector, int k, Double maxDistanceValue) {
        List<Document> results = new ArrayList<>();
        JanusGraphTransaction tx = graph.newTransaction();
        
        try {
            JanusGraphParam.VectorConfig vectorConfig = janusGraphParam.getVectorConfig();
            
            // 获取所有文档顶点
            List<Vertex> vertices = tx.traversal().V()
                .hasLabel(vectorConfig.getVertexLabel())
                .toList();
            
            // 计算相似度并排序
            List<DocumentSimilarity> similarities = new ArrayList<>();
            
            for (Vertex vertex : vertices) {
                String vectorStr = vertex.value(vectorConfig.getVectorPropertyName());
                List<Double> docVector = JSON.parseArray(vectorStr, Double.class);
                
                double similarity = JanusGraphSimilarityFunction.COSINE.calculateSimilarity(queryVector, docVector);
                double distance = JanusGraphSimilarityFunction.COSINE.calculateDistance(queryVector, docVector);
                
                // 应用距离阈值过滤
                if (maxDistanceValue != null && distance > maxDistanceValue) {
                    continue;
                }
                
                String docId = vertex.value(vectorConfig.getIdPropertyName());
                String content = vertex.value(vectorConfig.getContentPropertyName());
                String metadataStr = vertex.property(vectorConfig.getMetadataPropertyName()).isPresent() ? 
                    vertex.value(vectorConfig.getMetadataPropertyName()) : null;
                
                Map<String, Object> metadata = new HashMap<>();
                if (StringUtils.isNotEmpty(metadataStr)) {
                    metadata = JSON.parseObject(metadataStr, Map.class);
                }
                metadata.put("score", similarity);
                metadata.put("distance", distance);
                
                Document document = new Document();
                document.setPageContent(content);
                document.setMetadata(metadata);
                
                similarities.add(new DocumentSimilarity(document, similarity));
            }
            
            // 按相似度排序并取前k个
            results = similarities.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .limit(k)
                .map(DocumentSimilarity::getDocument)
                .collect(Collectors.toList());
            
            tx.commit();
            
        } catch (Exception e) {
            tx.rollback();
            log.error("Failed to perform vector search", e);
            throw new RuntimeException("Failed to perform vector search", e);
        }
        
        return results;
    }

    /**
     * 删除文档
     */
    public boolean deleteDocument(String documentId) {
        JanusGraphTransaction tx = graph.newTransaction();
        
        try {
            JanusGraphParam.VectorConfig vectorConfig = janusGraphParam.getVectorConfig();
            
            Vertex vertex = tx.traversal().V()
                .hasLabel(vectorConfig.getVertexLabel())
                .has(vectorConfig.getIdPropertyName(), documentId)
                .tryNext()
                .orElse(null);
            
            if (vertex != null) {
                vertex.remove();
                tx.commit();
                log.info("Deleted document with ID: {}", documentId);
                return true;
            } else {
                tx.rollback();
                log.warn("Document not found with ID: {}", documentId);
                return false;
            }
            
        } catch (Exception e) {
            tx.rollback();
            log.error("Failed to delete document with ID: {}", documentId, e);
            throw new RuntimeException("Failed to delete document", e);
        }
    }

    /**
     * 生成文档ID
     */
    private String generateDocumentId(Document document) {
        // 优先使用文档元数据中的ID
        if (document.getMetadata() != null && document.getMetadata().containsKey("id")) {
            return document.getMetadata().get("id").toString();
        }
        
        // 生成UUID作为文档ID
        return UUID.randomUUID().toString();
    }

    /**
     * 分割文档列表
     */
    private List<List<Document>> partitionDocuments(List<Document> documents, int batchSize) {
        List<List<Document>> batches = new ArrayList<>();
        for (int i = 0; i < documents.size(); i += batchSize) {
            batches.add(documents.subList(i, Math.min(i + batchSize, documents.size())));
        }
        return batches;
    }

    @Override
    public void close() {
        try {
            if (gremlinClient != null) {
                gremlinClient.close();
            }
            if (gremlinCluster != null) {
                gremlinCluster.close();
            }
            if (graph != null) {
                graph.close();
            }
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            log.info("JanusGraph Service closed successfully");
        } catch (Exception e) {
            log.error("Error closing JanusGraph Service", e);
        }
    }

    /**
     * 文档相似度辅助类
     */
    private static class DocumentSimilarity {
        private final Document document;
        private final double similarity;

        public DocumentSimilarity(Document document, double similarity) {
            this.document = document;
            this.similarity = similarity;
        }

        public Document getDocument() {
            return document;
        }

        public double getSimilarity() {
            return similarity;
        }
    }
}
