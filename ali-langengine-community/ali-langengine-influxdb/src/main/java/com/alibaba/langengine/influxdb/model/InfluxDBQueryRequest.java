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
package com.alibaba.langengine.influxdb.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class InfluxDBQueryRequest {

    /**
     * 查询向量
     */
    private List<Double> queryVector;

    /**
     * 返回结果数量
     */
    private int limit = 10;

    /**
     * 相似度阈值
     */
    private Double similarityThreshold = 0.0;

    /**
     * 测量名称
     */
    private String measurement = "vectors";

    /**
     * 标签过滤条件
     */
    private Map<String, String> tagFilters = new HashMap<>();

    /**
     * 字段过滤条件
     */
    private Map<String, Object> fieldFilters = new HashMap<>();

    /**
     * 时间范围开始
     */
    private Instant timeRangeStart;

    /**
     * 时间范围结束
     */
    private Instant timeRangeEnd;

    /**
     * 相似度计算方法
     */
    private SimilarityMetric similarityMetric = SimilarityMetric.COSINE;

    /**
     * 是否包含向量数据
     */
    private boolean includeVectors = false;

    /**
     * 是否包含元数据
     */
    private boolean includeMetadata = true;

    /**
     * 是否包含内容数据
     */
    private boolean includeContent = true;

    /**
     * 排序方式
     */
    private SortOrder sortOrder = SortOrder.DESC;

    /**
     * 相似度计算方法枚举
     */
    public enum SimilarityMetric {
        COSINE("cosine"),
        DOT_PRODUCT("dot_product"),
        EUCLIDEAN("euclidean"),
        MANHATTAN("manhattan");

        private final String value;

        SimilarityMetric(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        /**
         * 根据字符串值获取枚举
         */
        public static SimilarityMetric fromValue(String value) {
            for (SimilarityMetric metric : values()) {
                if (metric.value.equals(value)) {
                    return metric;
                }
            }
            throw new IllegalArgumentException("Unknown similarity metric: " + value);
        }
    }

    /**
     * 排序方式枚举
     */
    public enum SortOrder {
        ASC("asc"),
        DESC("desc");

        private final String value;

        SortOrder(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 验证查询请求有效性
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return queryVector != null && !queryVector.isEmpty() && limit > 0;
    }

    /**
     * 验证参数并抛出异常
     */
    public void validate() {
        if (queryVector == null || queryVector.isEmpty()) {
            throw new IllegalArgumentException("Query vector cannot be null or empty");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        if (similarityThreshold != null && (similarityThreshold < 0.0 || similarityThreshold > 1.0)) {
            throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
        }
    }

    /**
     * 获取向量维度
     *
     * @return 向量维度
     */
    public int getVectorDimension() {
        return queryVector != null ? queryVector.size() : 0;
    }

    /**
     * 创建Builder
     */
    public static InfluxDBQueryRequestBuilder builder() {
        return new InfluxDBQueryRequestBuilder();
    }

    /**
     * 自定义Builder类，支持验证
     */
    public static class InfluxDBQueryRequestBuilder {
        private List<Double> queryVector;
        private int limit = 10;
        private Double similarityThreshold = 0.0;
        private String measurement = "vectors";
        private Map<String, String> tagFilters = new HashMap<>();
        private Map<String, Object> fieldFilters = new HashMap<>();
        private Instant timeRangeStart;
        private Instant timeRangeEnd;
        private SimilarityMetric similarityMetric = SimilarityMetric.COSINE;
        private boolean includeVectors = false;
        private boolean includeMetadata = true;
        private boolean includeContent = true;
        private SortOrder sortOrder = SortOrder.DESC;

        public InfluxDBQueryRequestBuilder queryVector(List<Double> queryVector) {
            this.queryVector = queryVector;
            return this;
        }

        public InfluxDBQueryRequestBuilder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public InfluxDBQueryRequestBuilder similarityThreshold(Double similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
            return this;
        }

        public InfluxDBQueryRequestBuilder measurement(String measurement) {
            this.measurement = measurement;
            return this;
        }

        public InfluxDBQueryRequestBuilder tagFilters(Map<String, String> tagFilters) {
            this.tagFilters = tagFilters;
            return this;
        }

        public InfluxDBQueryRequestBuilder fieldFilters(Map<String, Object> fieldFilters) {
            this.fieldFilters = fieldFilters;
            return this;
        }

        public InfluxDBQueryRequestBuilder timeRangeStart(Instant timeRangeStart) {
            this.timeRangeStart = timeRangeStart;
            return this;
        }

        public InfluxDBQueryRequestBuilder timeRangeEnd(Instant timeRangeEnd) {
            this.timeRangeEnd = timeRangeEnd;
            return this;
        }

        public InfluxDBQueryRequestBuilder similarityMetric(SimilarityMetric similarityMetric) {
            this.similarityMetric = similarityMetric;
            return this;
        }

        public InfluxDBQueryRequestBuilder includeVectors(boolean includeVectors) {
            this.includeVectors = includeVectors;
            return this;
        }

        public InfluxDBQueryRequestBuilder includeMetadata(boolean includeMetadata) {
            this.includeMetadata = includeMetadata;
            return this;
        }

        public InfluxDBQueryRequestBuilder includeContent(boolean includeContent) {
            this.includeContent = includeContent;
            return this;
        }

        public InfluxDBQueryRequestBuilder sortOrder(SortOrder sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public InfluxDBQueryRequest build() {
            InfluxDBQueryRequest request = new InfluxDBQueryRequest();
            request.queryVector = this.queryVector;
            request.limit = this.limit;
            request.similarityThreshold = this.similarityThreshold;
            request.measurement = this.measurement != null ? this.measurement : "vectors";
            request.tagFilters = this.tagFilters != null ? this.tagFilters : new HashMap<>();
            request.fieldFilters = this.fieldFilters != null ? this.fieldFilters : new HashMap<>();
            request.timeRangeStart = this.timeRangeStart;
            request.timeRangeEnd = this.timeRangeEnd;
            request.similarityMetric = this.similarityMetric != null ? this.similarityMetric : SimilarityMetric.COSINE;
            request.includeVectors = this.includeVectors;
            request.includeMetadata = this.includeMetadata;
            request.includeContent = this.includeContent;
            request.sortOrder = this.sortOrder != null ? this.sortOrder : SortOrder.DESC;
            
            // 验证
            request.validate();
            
            return request;
        }
    }
}
