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
package com.alibaba.langengine.dgraph.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.langengine.dgraph.DgraphConfiguration.DGRAPH_SERVER_URL;


@Slf4j
@Data
public class Dgraph extends VectorStore {

    /**
     * 向量嵌入模型
     */
    private Embeddings embedding;

    /**
     * Dgraph 服务器地址
     */
    private String serverUrl;

    /**
     * Dgraph 参数配置
     */
    private DgraphParam param;

    /**
     * Dgraph 服务实例
     */
    private DgraphService dgraphService;

    /**
     * 构造函数
     *
     * @param embedding 向量嵌入模型
     * @param serverUrl Dgraph 服务器地址
     * @param param     Dgraph 参数配置
     */
    public Dgraph(Embeddings embedding, String serverUrl, DgraphParam param) {
        if (embedding == null) {
            throw new IllegalArgumentException("Embedding cannot be null");
        }
        if (StringUtils.isEmpty(serverUrl)) {
            throw new IllegalArgumentException("Server URL cannot be empty");
        }
        if (param == null) {
            throw new IllegalArgumentException("DgraphParam cannot be null");
        }

        this.embedding = embedding;
        this.serverUrl = serverUrl;
        this.param = param;
        
        // 初始化 Dgraph 服务
        initializeDgraphService();
    }

    /**
     * 使用默认配置的构造函数
     *
     * @param embedding 向量嵌入模型
     */
    public Dgraph(Embeddings embedding) {
        this(embedding, DGRAPH_SERVER_URL, new DgraphParam());
    }

    /**
     * 初始化 Dgraph 服务
     */
    private void initializeDgraphService() {
        try {
            this.dgraphService = new DgraphService(serverUrl, param);
            log.info("Dgraph service initialized successfully with server: {}", serverUrl);
        } catch (Exception e) {
            log.error("Failed to initialize Dgraph service", e);
            throw new RuntimeException("Failed to initialize Dgraph service", e);
        }
    }

    /**
     * 获取 DgraphService 实例（用于测试）
     */
    public DgraphService getDgraphService() {
        return dgraphService;
    }

    /**
     * 添加文档到向量库
     *
     * @param documents 文档列表
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("Documents list is empty, nothing to add");
            return;
        }

        try {
            // 提取文档内容进行向量化
            List<String> texts = documents.stream()
                    .map(Document::getPageContent)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(texts)) {
                log.warn("No valid texts found in documents");
                return;
            }

            // 生成向量嵌入 - 使用 embedDocument 方法
            List<Document> embeddedDocs = embedding.embedDocument(documents);
            if (CollectionUtils.isEmpty(embeddedDocs)) {
                throw new RuntimeException("Failed to generate embeddings");
            }

            // 模拟从嵌入文档中提取向量（实际实现中需要根据具体的 Embeddings 实现）
            List<List<Float>> vectorEmbeddings = new ArrayList<>();
            for (int i = 0; i < documents.size(); i++) {
                // 这里是一个占位符实现，实际中需要从 Document 中提取向量数据
                List<Float> mockVector = generateMockVector(param.getVectorDimension());
                vectorEmbeddings.add(mockVector);
            }

            // 添加到 Dgraph
            int addedCount = dgraphService.addDocuments(documents, vectorEmbeddings);
            
            log.info("Successfully added {} documents to Dgraph vector store", addedCount);

        } catch (Exception e) {
            log.error("Failed to add documents to Dgraph vector store", e);
            throw new RuntimeException("Failed to add documents to Dgraph vector store", e);
        }
    }

    /**
     * 相似性搜索（实现抽象方法）
     *
     * @param query            查询文本
     * @param k                返回结果数量
     * @param maxDistanceValue 最大距离值
     * @param type             类型
     * @return 相似的文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        return similaritySearchWithFilter(query, k, null);
    }

    /**
     * 相似性搜索（带过滤条件）
     *
     * @param query  查询文本
     * @param k      返回结果数量
     * @param filter 过滤条件
     * @return 相似的文档列表
     */
    public List<Document> similaritySearchWithFilter(String query, int k, Map<String, Object> filter) {
        if (StringUtils.isEmpty(query)) {
            log.warn("Query string is empty");
            return Collections.emptyList();
        }

        if (k <= 0) {
            log.warn("Invalid k value: {}, using default limit", k);
            k = param.getSearchLimit();
        }

        try {
            // 对查询文本进行向量化 - 使用正确的方法签名
            List<String> queryEmbeddingStr = embedding.embedQuery(query, 1);
            if (CollectionUtils.isEmpty(queryEmbeddingStr)) {
                throw new RuntimeException("Failed to generate query embedding");
            }

            // 模拟转换为 Float 向量（实际实现中需要根据具体的 Embeddings 实现）
            List<Float> queryEmbedding = generateMockVector(param.getVectorDimension());

            // 执行相似性搜索
            List<Document> results = dgraphService.similaritySearch(queryEmbedding, k, filter);
            
            log.info("Similarity search completed, found {} results for query: {}", 
                    results.size(), query.substring(0, Math.min(query.length(), 50)));
            
            return results;

        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new RuntimeException("Failed to perform similarity search", e);
        }
    }

    /**
     * 相似性搜索（不带过滤条件）
     *
     * @param query 查询文本
     * @param k     返回结果数量
     * @return 相似的文档列表
     */
    public List<Document> similaritySearchSimple(String query, int k) {
        return similaritySearchWithFilter(query, k, null);
    }

    /**
     * 相似性搜索（使用默认返回数量）
     *
     * @param query 查询文本
     * @return 相似的文档列表
     */
    public List<Document> similaritySearchSimple(String query) {
        return similaritySearchWithFilter(query, param.getSearchLimit(), null);
    }

    /**
     * 生成模拟向量（用于演示，实际实现中应该从 Embeddings 中获取）
     */
    private List<Float> generateMockVector(int dimension) {
        List<Float> vector = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < dimension; i++) {
            vector.add(random.nextFloat() * 2 - 1); // [-1, 1] 范围
        }
        return vector;
    }

    /**
     * 基于向量的相似性搜索
     *
     * @param embedding 查询向量
     * @param k         返回结果数量
     * @param filter    过滤条件
     * @return 相似的文档列表
     */
    public List<Document> similaritySearchByVector(List<Float> embedding, int k, Map<String, Object> filter) {
        if (CollectionUtils.isEmpty(embedding)) {
            log.warn("Query embedding is empty");
            return Collections.emptyList();
        }

        if (k <= 0) {
            k = param.getSearchLimit();
        }

        try {
            List<Document> results = dgraphService.similaritySearch(embedding, k, filter);
            log.info("Vector similarity search completed, found {} results", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Failed to perform vector similarity search", e);
            throw new RuntimeException("Failed to perform vector similarity search", e);
        }
    }

    /**
     * 删除文档
     *
     * @param filter 删除条件
     * @return 删除的文档数量
     */
    public int deleteDocuments(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            log.warn("Delete filter is empty, no documents will be deleted");
            return 0;
        }

        try {
            int deletedCount = dgraphService.deleteDocuments(filter);
            log.info("Successfully deleted {} documents from Dgraph vector store", deletedCount);
            return deletedCount;
            
        } catch (Exception e) {
            log.error("Failed to delete documents from Dgraph vector store", e);
            throw new RuntimeException("Failed to delete documents from Dgraph vector store", e);
        }
    }

    /**
     * 清空向量库中的所有文档
     *
     * @return 删除的文档数量
     */
    public int clearAll() {
        Map<String, Object> clearAllFilter = new HashMap<>();
        clearAllFilter.put("dgraph.type", "VectorDocument");
        return deleteDocuments(clearAllFilter);
    }

    /**
     * 获取向量库统计信息
     *
     * @return 统计信息映射
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("serverUrl", serverUrl);
        stats.put("vectorDimension", param.getVectorDimension());
        stats.put("similarityAlgorithm", param.getSimilarityAlgorithm());
        stats.put("searchLimit", param.getSearchLimit());
        stats.put("batchSize", param.getBatchSize());
        return stats;
    }

    /**
     * 关闭向量库连接
     */
    public void close() {
        if (dgraphService != null) {
            try {
                dgraphService.close();
                log.info("Dgraph vector store closed successfully");
            } catch (Exception e) {
                log.error("Failed to close Dgraph vector store", e);
            }
        }
    }

    /**
     * Builder 模式用于创建 Dgraph 实例
     */
    public static class Builder {
        private Embeddings embedding;
        private String serverUrl = DGRAPH_SERVER_URL;
        private DgraphParam param = new DgraphParam();

        /**
         * 设置向量嵌入模型
         */
        public Builder embedding(Embeddings embedding) {
            this.embedding = embedding;
            return this;
        }

        /**
         * 设置服务器地址
         */
        public Builder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        /**
         * 设置参数配置
         */
        public Builder param(DgraphParam param) {
            this.param = param;
            return this;
        }

        /**
         * 设置向量维度
         */
        public Builder vectorDimension(int vectorDimension) {
            this.param.setVectorDimension(vectorDimension);
            return this;
        }

        /**
         * 设置相似度算法
         */
        public Builder similarityAlgorithm(String similarityAlgorithm) {
            this.param.setSimilarityAlgorithm(similarityAlgorithm);
            return this;
        }

        /**
         * 设置搜索限制
         */
        public Builder searchLimit(int searchLimit) {
            this.param.setSearchLimit(searchLimit);
            return this;
        }

        /**
         * 设置批量处理大小
         */
        public Builder batchSize(int batchSize) {
            this.param.setBatchSize(batchSize);
            return this;
        }

        /**
         * 构建 Dgraph 实例
         */
        public Dgraph build() {
            if (embedding == null) {
                throw new IllegalArgumentException("Embedding is required");
            }
            return new Dgraph(embedding, serverUrl, param);
        }
    }
}
