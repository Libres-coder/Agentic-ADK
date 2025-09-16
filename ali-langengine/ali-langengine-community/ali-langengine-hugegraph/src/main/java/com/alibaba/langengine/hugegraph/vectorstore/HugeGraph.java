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
package com.alibaba.langengine.hugegraph.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.hugegraph.exception.HugeGraphVectorStoreException;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.alibaba.langengine.hugegraph.HugeGraphConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class HugeGraph extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * HugeGraph服务实例
     */
    private final HugeGraphService hugeGraphService;

    /**
     * HugeGraph参数配置
     */
    private HugeGraphParam hugeGraphParam;

    /**
     * 相似度计算函数
     */
    private HugeGraphSimilarityFunction similarityFunction;

    /**
     * 构造函数 - 使用默认配置
     */
    public HugeGraph(Embeddings embedding) {
        this(embedding, HugeGraphParam.getDefaultConfig(), HugeGraphSimilarityFunction.COSINE);
    }

    /**
     * 构造函数 - 自定义配置
     */
    public HugeGraph(Embeddings embedding, HugeGraphParam hugeGraphParam) {
        this(embedding, hugeGraphParam, HugeGraphSimilarityFunction.COSINE);
    }

    /**
     * 构造函数 - 完整配置
     */
    public HugeGraph(Embeddings embedding, HugeGraphParam hugeGraphParam,
                     HugeGraphSimilarityFunction similarityFunction) {
        this.embedding = embedding;
        this.hugeGraphParam = hugeGraphParam != null ? hugeGraphParam : HugeGraphParam.getDefaultConfig();
        this.similarityFunction = similarityFunction != null ? similarityFunction : HugeGraphSimilarityFunction.COSINE;

        try {
            this.hugeGraphService = new HugeGraphService(this.hugeGraphParam);
            initializeVectorStore();
            log.info("HugeGraph VectorStore initialized successfully");
        } catch (HugeGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to initialize HugeGraph VectorStore", e);
            throw HugeGraphVectorStoreException.configurationError("Failed to initialize HugeGraph VectorStore: " + e.getMessage());
        }
    }

    /**
     * 初始化向量存储
     */
    private void initializeVectorStore() {
        try {
            // 初始化Schema和索引
            hugeGraphService.initializeSchema(embedding);
            // 验证配置
            validateConfiguration();
            log.info("Vector store initialization completed");
        } catch (HugeGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to initialize vector store", e);
            throw new HugeGraphVectorStoreException(HugeGraphVectorStoreException.ErrorType.CONFIGURATION_ERROR,
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

        HugeGraphParam.VectorConfig vectorConfig = hugeGraphParam.getVectorConfig();
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
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("Documents list is empty, nothing to add.");
            return;
        }

        try {
            List<Document> processedDocs = preprocessDocuments(documents);
            hugeGraphService.addDocuments(processedDocs, embedding);
        } catch (HugeGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to add documents to HugeGraph", e);
            throw HugeGraphVectorStoreException.vectorInsertFailed("Failed to add documents: " + e.getMessage(), e);
        }
    }

    /**
     * 预处理文档
     */
    private List<Document> preprocessDocuments(List<Document> documents) {
        for (Document doc : documents) {
            if (StringUtils.isEmpty(doc.getUniqueId())) {
                doc.setUniqueId(UUID.randomUUID().toString());
            }
            if (doc.getMetadata() == null) {
                doc.setMetadata(new java.util.HashMap<>());
            }
            doc.getMetadata().putIfAbsent("created_at", System.currentTimeMillis());
        }
        return documents;
    }

    /**
     * 相似度搜索
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

            List<Document> results = hugeGraphService.similaritySearch(query, embedding, k, maxDistanceValue, type);

            log.info("Similarity search returned {} results", results.size());
            return results;
        } catch (HugeGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to perform similarity search for query: '{}'", query, e);
            throw new HugeGraphVectorStoreException(HugeGraphVectorStoreException.ErrorType.VECTOR_SEARCH_FAILED,
                "Similarity search failed for query: '" + query + "': " + e.getMessage(), e);
        }
    }

    /**
     * 关闭向量存储
     */
    public void close() {
        try {
            if (hugeGraphService != null) {
                hugeGraphService.close();
                log.info("HugeGraph VectorStore closed successfully.");
            }
        } catch (Exception e) {
            log.error("Failed to close HugeGraph VectorStore", e);
        }
    }

    /**
     * 获取存储统计信息
     */
    public Map<String, Object> getStorageStats() {
        try {
            return Map.of(
                "graph_name", hugeGraphParam.getServerConfig().getGraph(),
                "vertex_label", hugeGraphParam.getVectorConfig().getVertexLabel(),
                "vector_dimension", hugeGraphParam.getVectorConfig().getVectorDimension(),
                "similarity_function", similarityFunction.getName(),
                "initialized", hugeGraphService != null
            );
        } catch (Exception e) {
            log.error("Failed to get storage stats", e);
            return Map.of("error", e.getMessage());
        }
    }
}
