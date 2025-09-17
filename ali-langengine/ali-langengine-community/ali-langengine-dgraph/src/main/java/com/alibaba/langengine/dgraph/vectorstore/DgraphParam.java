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

    /**
     * 构建器模式
     */
    public static class Builder {
        private DgraphParam param = new DgraphParam();

        public Builder vectorFieldName(String vectorFieldName) {
            param.setVectorFieldName(vectorFieldName);
            return this;
        }

        public Builder contentFieldName(String contentFieldName) {
            param.setContentFieldName(contentFieldName);
            return this;
        }

        public Builder metadataFieldName(String metadataFieldName) {
            param.setMetadataFieldName(metadataFieldName);
            return this;
        }

        public Builder vectorDimension(int vectorDimension) {
            param.setVectorDimension(vectorDimension);
            return this;
        }

        public Builder similarityAlgorithm(String similarityAlgorithm) {
            param.setSimilarityAlgorithm(similarityAlgorithm);
            return this;
        }

        public Builder searchLimit(int searchLimit) {
            param.setSearchLimit(searchLimit);
            return this;
        }

        public Builder batchSize(int batchSize) {
            param.setBatchSize(batchSize);
            return this;
        }

        public Builder similarityThreshold(float similarityThreshold) {
            param.setSimilarityThreshold(similarityThreshold);
            return this;
        }

        public Builder searchParams(Map<String, Object> searchParams) {
            param.setSearchParams(searchParams);
            return this;
        }

        public Builder transactionEnabled(boolean transactionEnabled) {
            param.setTransactionEnabled(transactionEnabled);
            return this;
        }

        public Builder queryTimeoutMs(int queryTimeoutMs) {
            param.setQueryTimeoutMs(queryTimeoutMs);
            return this;
        }

        public DgraphParam build() {
            return param;
        }
    }
}
