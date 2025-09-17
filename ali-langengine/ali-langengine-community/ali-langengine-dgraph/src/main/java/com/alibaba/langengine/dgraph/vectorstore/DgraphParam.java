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

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.langengine.dgraph.DgraphConfiguration.*;


@Data
public class DgraphParam {

    /**
     * 向量字段名
     */
    private String vectorFieldName = "vector_embedding";

    /**
     * 内容字段名
     */
    private String contentFieldName = "content";

    /**
     * 元数据字段名
     */
    private String metadataFieldName = "metadata";

    /**
     * 唯一ID字段名
     */
    private String idFieldName = "uid";

    /**
     * 向量维度
     */
    private int vectorDimension = DGRAPH_DEFAULT_VECTOR_DIMENSION;

    /**
     * 相似度算法类型：cosine, euclidean, dotproduct
     */
    private String similarityAlgorithm = DGRAPH_SIMILARITY_ALGORITHM;

    /**
     * 搜索结果限制数量
     */
    private int searchLimit = DGRAPH_DEFAULT_SEARCH_LIMIT;

    /**
     * 批量处理大小
     */
    private int batchSize = DGRAPH_DEFAULT_BATCH_SIZE;

    /**
     * 相似度阈值
     */
    private float similarityThreshold = 0.5f;

    /**
     * 自定义搜索扩展参数
     */
    private Map<String, Object> searchParams = new HashMap<>();

    /**
     * Dgraph 谓词映射配置
     */
    private Map<String, String> predicateMapping = new HashMap<>();

    /**
     * 是否启用事务
     */
    private boolean transactionEnabled = true;

    /**
     * 查询超时时间（毫秒）
     */
    private int queryTimeoutMs = DGRAPH_TIMEOUT_MS;

    /**
     * HNSW索引参数 - m: 每个节点的最大连接数
     */
    private int hnswM = 16;

    /**
     * HNSW索引参数 - efConstruction: 构建时的搜索候选数
     */
    private int hnswEfConstruction = 200;

    /**
     * HNSW索引参数 - ef: 查询时的搜索候选数
     */
    private int hnswEf = 100;

    /**
     * 构造函数
     */
    public DgraphParam() {
        initializeDefaultPredicates();
    }

    /**
     * 初始化默认谓词映射
     */
    private void initializeDefaultPredicates() {
        predicateMapping.put("vector", vectorFieldName);
        predicateMapping.put("content", contentFieldName);
        predicateMapping.put("metadata", metadataFieldName);
        predicateMapping.put("id", idFieldName);
    }

    // Getter方法
    public String getVectorFieldName() {
        return vectorFieldName;
    }

    public String getContentFieldName() {
        return contentFieldName;
    }

    public String getMetadataFieldName() {
        return metadataFieldName;
    }

    public String getIdFieldName() {
        return idFieldName;
    }

    public int getVectorDimension() {
        return vectorDimension;
    }

    public String getSimilarityAlgorithm() {
        return similarityAlgorithm;
    }

    public int getSearchLimit() {
        return searchLimit;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public float getSimilarityThreshold() {
        return similarityThreshold;
    }

    public Map<String, Object> getSearchParams() {
        return searchParams;
    }

    public Map<String, String> getPredicateMapping() {
        return predicateMapping;
    }

    public boolean isTransactionEnabled() {
        return transactionEnabled;
    }

    public int getQueryTimeoutMs() {
        return queryTimeoutMs;
    }

    public int getHnswM() {
        return hnswM;
    }

    public int getHnswEfConstruction() {
        return hnswEfConstruction;
    }

    public int getHnswEf() {
        return hnswEf;
    }

    // Setter方法
    public void setVectorFieldName(String vectorFieldName) {
        this.vectorFieldName = vectorFieldName;
    }

    public void setContentFieldName(String contentFieldName) {
        this.contentFieldName = contentFieldName;
    }

    public void setMetadataFieldName(String metadataFieldName) {
        this.metadataFieldName = metadataFieldName;
    }

    public void setIdFieldName(String idFieldName) {
        this.idFieldName = idFieldName;
    }

    public void setVectorDimension(int vectorDimension) {
        this.vectorDimension = vectorDimension;
    }

    public void setSimilarityAlgorithm(String similarityAlgorithm) {
        this.similarityAlgorithm = similarityAlgorithm;
    }

    public void setSearchLimit(int searchLimit) {
        this.searchLimit = searchLimit;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setSimilarityThreshold(float similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public void setSearchParams(Map<String, Object> searchParams) {
        this.searchParams = searchParams;
    }

    public void setPredicateMapping(Map<String, String> predicateMapping) {
        this.predicateMapping = predicateMapping;
    }

    public void setTransactionEnabled(boolean transactionEnabled) {
        this.transactionEnabled = transactionEnabled;
    }

    public void setQueryTimeoutMs(int queryTimeoutMs) {
        this.queryTimeoutMs = queryTimeoutMs;
    }

    public void setHnswM(int hnswM) {
        this.hnswM = hnswM;
    }

    public void setHnswEfConstruction(int hnswEfConstruction) {
        this.hnswEfConstruction = hnswEfConstruction;
    }

    public void setHnswEf(int hnswEf) {
        this.hnswEf = hnswEf;
    }

    /**
     * 构建器模式 - 确保每次build()都返回独立的实例
     */
    public static class Builder {
        private String vectorFieldName = "vector_embedding";
        private String contentFieldName = "content";
        private String metadataFieldName = "metadata";
        private String idFieldName = "uid";
        private int vectorDimension = DGRAPH_DEFAULT_VECTOR_DIMENSION;
        private String similarityAlgorithm = DGRAPH_SIMILARITY_ALGORITHM;
        private int searchLimit = DGRAPH_DEFAULT_SEARCH_LIMIT;
        private int batchSize = DGRAPH_DEFAULT_BATCH_SIZE;
        private float similarityThreshold = 0.5f;
        private Map<String, Object> searchParams = new HashMap<>();
        private Map<String, String> predicateMapping = new HashMap<>();
        private boolean transactionEnabled = true;
        private int queryTimeoutMs = DGRAPH_TIMEOUT_MS;
        private int hnswM = 16;
        private int hnswEfConstruction = 200;
        private int hnswEf = 100;

        public Builder vectorFieldName(String vectorFieldName) {
            this.vectorFieldName = vectorFieldName;
            return this;
        }

        public Builder contentFieldName(String contentFieldName) {
            this.contentFieldName = contentFieldName;
            return this;
        }

        public Builder metadataFieldName(String metadataFieldName) {
            this.metadataFieldName = metadataFieldName;
            return this;
        }

        public Builder idFieldName(String idFieldName) {
            this.idFieldName = idFieldName;
            return this;
        }

        public Builder vectorDimension(int vectorDimension) {
            this.vectorDimension = vectorDimension;
            return this;
        }

        public Builder similarityAlgorithm(String similarityAlgorithm) {
            this.similarityAlgorithm = similarityAlgorithm;
            return this;
        }

        public Builder searchLimit(int searchLimit) {
            this.searchLimit = searchLimit;
            return this;
        }

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder similarityThreshold(float similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
            return this;
        }

        public Builder searchParams(Map<String, Object> searchParams) {
            if (searchParams == null) {
                this.searchParams = null;
            } else {
                this.searchParams = new HashMap<>(searchParams);
            }
            return this;
        }

        public Builder predicateMapping(Map<String, String> predicateMapping) {
            this.predicateMapping = new HashMap<>(predicateMapping != null ? predicateMapping : new HashMap<>());
            return this;
        }

        public Builder transactionEnabled(boolean transactionEnabled) {
            this.transactionEnabled = transactionEnabled;
            return this;
        }

        public Builder queryTimeoutMs(int queryTimeoutMs) {
            this.queryTimeoutMs = queryTimeoutMs;
            return this;
        }

        public Builder hnswM(int hnswM) {
            this.hnswM = hnswM;
            return this;
        }

        public Builder hnswEfConstruction(int hnswEfConstruction) {
            this.hnswEfConstruction = hnswEfConstruction;
            return this;
        }

        public Builder hnswEf(int hnswEf) {
            this.hnswEf = hnswEf;
            return this;
        }

        /**
         * 构建独立的DgraphParam实例，确保每次调用都返回新的对象
         */
        public DgraphParam build() {
            DgraphParam param = new DgraphParam();
            param.setVectorFieldName(this.vectorFieldName);
            param.setContentFieldName(this.contentFieldName);
            param.setMetadataFieldName(this.metadataFieldName);
            param.setIdFieldName(this.idFieldName);
            param.setVectorDimension(this.vectorDimension);
            param.setSimilarityAlgorithm(this.similarityAlgorithm);
            param.setSearchLimit(this.searchLimit);
            param.setBatchSize(this.batchSize);
            param.setSimilarityThreshold(this.similarityThreshold);
            param.setSearchParams(this.searchParams != null ? new HashMap<>(this.searchParams) : null);
            param.setTransactionEnabled(this.transactionEnabled);
            param.setQueryTimeoutMs(this.queryTimeoutMs);
            param.setHnswM(this.hnswM);
            param.setHnswEfConstruction(this.hnswEfConstruction);
            param.setHnswEf(this.hnswEf);
            
            // 重新初始化谓词映射
            param.initializeDefaultPredicates();
            
            // 如果Builder中有自定义的谓词映射，则合并
            if (!this.predicateMapping.isEmpty()) {
                param.getPredicateMapping().putAll(this.predicateMapping);
            }
            
            return param;
        }
    }
}
