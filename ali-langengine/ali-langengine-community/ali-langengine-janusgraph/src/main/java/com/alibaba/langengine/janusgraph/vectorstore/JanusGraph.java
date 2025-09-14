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
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.alibaba.langengine.janusgraph.JanusGraphConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class JanusGraph extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * JanusGraph服务实例
     */
    private final JanusGraphService janusGraphService;

    /**
     * JanusGraph参数配置
     */
    private JanusGraphParam janusGraphParam;

    /**
     * 相似度计算函数
     */
    private JanusGraphSimilarityFunction similarityFunction;

    /**
     * 构造函数 - 使用默认配置
     * 
     * @param embedding 嵌入模型
     */
    public JanusGraph(Embeddings embedding) {
        this(embedding, JanusGraphParam.getDefaultConfig(), JanusGraphSimilarityFunction.COSINE);
    }

    /**
     * 构造函数 - 自定义配置
     * 
     * @param embedding 嵌入模型
     * @param janusGraphParam JanusGraph配置参数
     */
    public JanusGraph(Embeddings embedding, JanusGraphParam janusGraphParam) {
        this(embedding, janusGraphParam, JanusGraphSimilarityFunction.COSINE);
    }

    /**
     * 构造函数 - 完整配置
     * 
     * @param embedding 嵌入模型
     * @param janusGraphParam JanusGraph配置参数
     * @param similarityFunction 相似度计算函数
     */
    public JanusGraph(Embeddings embedding, JanusGraphParam janusGraphParam, 
                     JanusGraphSimilarityFunction similarityFunction) {
        this.embedding = embedding;
        this.janusGraphParam = janusGraphParam != null ? janusGraphParam : JanusGraphParam.getDefaultConfig();
        this.similarityFunction = similarityFunction != null ? similarityFunction : JanusGraphSimilarityFunction.COSINE;
        
        try {
            this.janusGraphService = new JanusGraphService(this.janusGraphParam);
            initializeVectorStore();
            log.info("JanusGraph VectorStore initialized successfully");
        } catch (JanusGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to initialize JanusGraph VectorStore", e);
            throw JanusGraphVectorStoreException.configurationError("Failed to initialize JanusGraph VectorStore: " + e.getMessage());
        }
    }

    /**
     * 静态工厂方法 - 创建基于BerkeleyDB的本地JanusGraph实例
     * 
     * @param embedding 嵌入模型
     * @param storagePath 存储路径
     * @return JanusGraph实例
     */
    public static JanusGraph createLocalInstance(Embeddings embedding, String storagePath) {
        JanusGraphParam.GraphConfig graphConfig = JanusGraphParam.GraphConfig.builder()
            .storageBackend("berkeleyje")
            .build();
        graphConfig.getGraphProperties().put("storage.directory", storagePath);

        JanusGraphParam param = JanusGraphParam.builder()
            .graphConfig(graphConfig)
            .vectorConfig(JanusGraphParam.VectorConfig.builder().build())
            .connectionConfig(JanusGraphParam.ConnectionConfig.builder().build())
            .indexConfig(JanusGraphParam.IndexConfig.builder()
                .indexBackend("lucene")
                .build())
            .batchConfig(JanusGraphParam.BatchConfig.builder().build())
            .initParam(JanusGraphParam.InitParam.builder().build())
            .build();

        return new JanusGraph(embedding, param);
    }

    /**
     * 静态工厂方法 - 创建基于Cassandra的分布式JanusGraph实例
     * 
     * @param embedding 嵌入模型
     * @param cassandraHost Cassandra主机地址
     * @param cassandraPort Cassandra端口
     * @param keyspace Cassandra keyspace
     * @return JanusGraph实例
     */
    public static JanusGraph createCassandraInstance(Embeddings embedding, String cassandraHost, 
                                                   Integer cassandraPort, String keyspace) {
        JanusGraphParam.GraphConfig graphConfig = JanusGraphParam.GraphConfig.builder()
            .storageBackend("cassandra")
            .storageHostname(cassandraHost)
            .storagePort(cassandraPort != null ? cassandraPort : 9042)
            .cassandraKeyspace(keyspace != null ? keyspace : "janusgraph")
            .build();

        JanusGraphParam.IndexConfig indexConfig = JanusGraphParam.IndexConfig.builder()
            .indexBackend("elasticsearch")
            .indexHostname(cassandraHost)
            .indexPort(9200)
            .build();

        JanusGraphParam param = JanusGraphParam.builder()
            .graphConfig(graphConfig)
            .vectorConfig(JanusGraphParam.VectorConfig.builder().build())
            .connectionConfig(JanusGraphParam.ConnectionConfig.builder().build())
            .indexConfig(indexConfig)
            .batchConfig(JanusGraphParam.BatchConfig.builder().build())
            .initParam(JanusGraphParam.InitParam.builder().build())
            .build();

        return new JanusGraph(embedding, param);
    }

    /**
     * 初始化向量存储
     */
    private void initializeVectorStore() {
        try {
            // 初始化Schema和索引
            janusGraphService.initializeSchema(embedding);
            
            // 验证配置
            validateConfiguration();
            
            log.info("Vector store initialization completed");
        } catch (JanusGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to initialize vector store", e);
            throw new JanusGraphVectorStoreException(JanusGraphVectorStoreException.ErrorType.CONFIGURATION_ERROR, 
                "Vector store initialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * 验证配置
     */
    private void validateConfiguration() {
        if (embedding == null) {
            throw new IllegalArgumentException("Embedding model cannot be null");
        }

        JanusGraphParam.VectorConfig vectorConfig = janusGraphParam.getVectorConfig();
        if (vectorConfig.getVectorDimension() <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive");
        }

        if (StringUtils.isEmpty(vectorConfig.getVertexLabel())) {
            throw new IllegalArgumentException("Vertex label cannot be empty");
        }

        log.info("Configuration validation passed");
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
            log.info("Adding {} documents to JanusGraph", documents.size());
            
            // 预处理文档
            List<Document> processedDocuments = preprocessDocuments(documents);
            
            // 添加到图数据库
            List<String> documentIds = janusGraphService.addDocuments(processedDocuments, embedding);
            
            log.info("Successfully added {} documents with IDs: {}", 
                documentIds.size(), documentIds.size() <= 10 ? documentIds : 
                documentIds.subList(0, 10) + "...");
                
        } catch (JanusGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to add documents to JanusGraph", e);
            throw new JanusGraphVectorStoreException(JanusGraphVectorStoreException.ErrorType.DOCUMENT_ADD_FAILED, 
                "Failed to add documents to JanusGraph: " + e.getMessage(), e);
        }
    }

    /**
     * 预处理文档
     */
    private List<Document> preprocessDocuments(List<Document> documents) {
        return documents.stream()
            .peek(document -> {
                // 确保文档有ID
                if (document.getMetadata() == null || !document.getMetadata().containsKey("id")) {
                    if (document.getMetadata() == null) {
                        document.setMetadata(Map.of("id", UUID.randomUUID().toString()));
                    } else {
                        document.getMetadata().put("id", UUID.randomUUID().toString());
                    }
                }
                
                // 添加时间戳
                document.getMetadata().put("timestamp", System.currentTimeMillis());
                document.getMetadata().put("vector_store", "janusgraph");
            })
            .toList();
    }

    /**
     * 相似度搜索
     * 
     * @param query 查询文本
     * @param k 返回结果数量
     * @param maxDistanceValue 最大距离阈值
     * @param type 搜索类型
     * @return 相似文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isEmpty(query)) {
            log.warn("Query is empty, returning empty results");
            return Lists.newArrayList();
        }

        if (k <= 0) {
            log.warn("K must be positive, returning empty results");
            return Lists.newArrayList();
        }

        try {
            log.info("Performing similarity search with query: '{}', k: {}, maxDistance: {}, type: {}", 
                query, k, maxDistanceValue, type);
            
            List<Document> results = janusGraphService.similaritySearch(query, embedding, k, maxDistanceValue, type);
            
            log.info("Similarity search returned {} results", results.size());
            return results;
            
        } catch (JanusGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to perform similarity search for query: '{}'", query, e);
            throw new JanusGraphVectorStoreException(JanusGraphVectorStoreException.ErrorType.VECTOR_SEARCH_FAILED, 
                "Similarity search failed for query: '" + query + "': " + e.getMessage(), e);
        }
    }

    /**
     * 删除文档
     * 
     * @param documentId 文档ID
     * @return 是否成功删除
     */
    public boolean deleteDocument(String documentId) {
        if (StringUtils.isEmpty(documentId)) {
            log.warn("Document ID is empty");
            return false;
        }

        try {
            boolean deleted = janusGraphService.deleteDocument(documentId);
            if (deleted) {
                log.info("Successfully deleted document with ID: {}", documentId);
            } else {
                log.warn("Document not found with ID: {}", documentId);
            }
            return deleted;
        } catch (JanusGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete document with ID: {}", documentId, e);
            throw new JanusGraphVectorStoreException(JanusGraphVectorStoreException.ErrorType.DOCUMENT_DELETE_FAILED, 
                "Failed to delete document with ID: " + documentId + ": " + e.getMessage(), e);
        }
    }

    /**
     * 批量删除文档
     * 
     * @param documentIds 文档ID列表
     * @return 删除成功的文档数量
     */
    public int deleteDocuments(List<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            log.warn("No document IDs provided for deletion");
            return 0;
        }

        int deletedCount = 0;
        for (String documentId : documentIds) {
            try {
                if (deleteDocument(documentId)) {
                    deletedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to delete document with ID: {}", documentId, e);
            }
        }

        log.info("Deleted {} out of {} documents", deletedCount, documentIds.size());
        return deletedCount;
    }

    /**
     * 获取存储统计信息
     * 
     * @return 统计信息Map
     */
    public Map<String, Object> getStorageStats() {
        try {
            // 这里可以添加获取图数据库统计信息的逻辑
            return Map.of(
                "storage_backend", janusGraphParam.getGraphConfig().getStorageBackend(),
                "index_backend", janusGraphParam.getIndexConfig().getIndexBackend(),
                "vertex_label", janusGraphParam.getVectorConfig().getVertexLabel(),
                "vector_dimension", janusGraphParam.getVectorConfig().getVectorDimension(),
                "similarity_function", similarityFunction.getName(),
                "initialized", janusGraphService != null
            );
        } catch (Exception e) {
            log.error("Failed to get storage stats", e);
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * 更新相似度计算函数
     * 
     * @param newSimilarityFunction 新的相似度函数
     */
    public void updateSimilarityFunction(JanusGraphSimilarityFunction newSimilarityFunction) {
        if (newSimilarityFunction != null) {
            this.similarityFunction = newSimilarityFunction;
            log.info("Updated similarity function to: {}", newSimilarityFunction.getName());
        }
    }

    /**
     * 健康检查
     * 
     * @return 是否健康
     */
    public boolean healthCheck() {
        try {
            if (janusGraphService != null) {
                janusGraphService.verifyConnection();
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Health check failed", e);
            return false;
        }
    }

    /**
     * 关闭资源
     */
    public void close() {
        try {
            if (janusGraphService != null) {
                janusGraphService.close();
                log.info("JanusGraph VectorStore closed successfully");
            }
        } catch (Exception e) {
            log.error("Error closing JanusGraph VectorStore", e);
        }
    }

    /**
     * 获取配置信息的JSON表示
     * 
     * @return 配置JSON字符串
     */
    public String getConfigurationJson() {
        try {
            return JSON.toJSONString(janusGraphParam, true);
        } catch (Exception e) {
            log.error("Failed to serialize configuration", e);
            return "{}";
        }
    }
}
