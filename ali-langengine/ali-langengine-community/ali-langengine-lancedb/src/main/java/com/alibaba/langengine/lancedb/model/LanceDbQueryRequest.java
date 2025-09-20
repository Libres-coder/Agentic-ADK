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
package com.alibaba.langengine.lancedb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LanceDbQueryRequest {

    /**
     * 查询向量
     */
    @JsonProperty("vector")
    private List<Double> vector;

    /**
     * 查询文本（如果支持）
     */
    @JsonProperty("query")
    private String query;

    /**
     * 返回结果数量
     */
    @JsonProperty("limit")
    private Integer limit;

    /**
     * 距离阈值
     */
    @JsonProperty("distance_threshold")
    private Double distanceThreshold;

    /**
     * 相似度阈值
     */
    @JsonProperty("similarity_threshold")
    private Double similarityThreshold;

    /**
     * 过滤条件
     */
    @JsonProperty("filter")
    private Map<String, Object> filter;

    /**
     * 需要返回的字段
     */
    @JsonProperty("fields")
    private List<String> fields;

    /**
     * 度量类型（如：cosine, euclidean, dot）
     */
    @JsonProperty("metric")
    private String metric;

    /**
     * 是否包含向量数据
     */
    @JsonProperty("include_vectors")
    private Boolean includeVectors;

    /**
     * 是否包含元数据
     */
    @JsonProperty("include_metadata")
    private Boolean includeMetadata;

    /**
     * 搜索参数
     */
    @JsonProperty("search_params")
    private Map<String, Object> searchParams;

    /**
     * 验证查询请求是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return (vector != null && !vector.isEmpty()) || 
               (query != null && !query.trim().isEmpty());
    }

    /**
     * 获取有效的限制数量
     *
     * @return 限制数量
     */
    public int getEffectiveLimit() {
        return limit != null && limit > 0 ? limit : 10;
    }

    /**
     * 获取有效的度量类型
     *
     * @return 度量类型
     */
    public String getEffectiveMetric() {
        return metric != null && !metric.trim().isEmpty() ? metric : "cosine";
    }

    /**
     * 是否应该包含向量
     *
     * @return 是否包含向量
     */
    public boolean shouldIncludeVectors() {
        return includeVectors != null ? includeVectors : false;
    }

    /**
     * 是否应该包含元数据
     *
     * @return 是否包含元数据
     */
    public boolean shouldIncludeMetadata() {
        return includeMetadata != null ? includeMetadata : true;
    }

    /**
     * 创建基于向量的查询请求
     *
     * @param vector 查询向量
     * @param limit  结果数量
     * @return 查询请求
     */
    public static LanceDbQueryRequest vectorQuery(List<Double> vector, int limit) {
        return LanceDbQueryRequest.builder()
                .vector(vector)
                .limit(limit)
                .metric("cosine")
                .includeMetadata(true)
                .build();
    }

    /**
     * 创建基于文本的查询请求
     *
     * @param query 查询文本
     * @param limit 结果数量
     * @return 查询请求
     */
    public static LanceDbQueryRequest textQuery(String query, int limit) {
        return LanceDbQueryRequest.builder()
                .query(query)
                .limit(limit)
                .metric("cosine")
                .includeMetadata(true)
                .build();
    }
}
