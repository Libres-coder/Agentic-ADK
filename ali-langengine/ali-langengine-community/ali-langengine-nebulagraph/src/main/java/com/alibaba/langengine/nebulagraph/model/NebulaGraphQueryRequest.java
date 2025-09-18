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
package com.alibaba.langengine.nebulagraph.model;

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
public class NebulaGraphQueryRequest {
    
    /**
     * 查询向量
     */
    private List<Double> queryVector;
    
    /**
     * 返回结果数量
     */
    private int topK = 10;
    
    /**
     * 相似度阈值
     */
    private Double similarityThreshold;
    
    /**
     * 距离函数类型
     */
    private DistanceFunction distanceFunction = DistanceFunction.COSINE;
    
    /**
     * 是否包含向量数据
     */
    private boolean includeVector = false;
    
    /**
     * 是否包含元数据
     */
    private boolean includeMetadata = true;
    
    /**
     * 过滤条件
     */
    private Map<String, Object> filter;
    
    /**
     * 标签名称
     */
    private String tagName = "Document";
    
    /**
     * 图空间名称
     */
    private String spaceName;
    
    /**
     * 距离函数枚举
     */
    public enum DistanceFunction {
        COSINE("cosine"),
        L2("l2"),
        INNER_PRODUCT("inner_product");
        
        private final String value;
        
        DistanceFunction(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static DistanceFunction fromString(String value) {
            for (DistanceFunction function : values()) {
                if (function.getValue().equalsIgnoreCase(value)) {
                    return function;
                }
            }
            throw new IllegalArgumentException("Unknown distance function: " + value);
        }
    }
    
    /**
     * 验证查询请求参数
     */
    public void validate() {
        if (queryVector == null || queryVector.isEmpty()) {
            throw new IllegalArgumentException("Query vector cannot be null or empty");
        }
        
        if (topK <= 0) {
            throw new IllegalArgumentException("TopK must be positive");
        }
        
        if (similarityThreshold != null && (similarityThreshold < 0.0 || similarityThreshold > 1.0)) {
            throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
        }
        
        if (distanceFunction == null) {
            throw new IllegalArgumentException("Distance function cannot be null");
        }
        
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }
        
        if (spaceName == null || spaceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Space name cannot be null or empty");
        }
    }
    
    /**
     * 构建查询语句
     */
    public String buildQuery() {
        validate();
        
        StringBuilder query = new StringBuilder();
        query.append("USE ").append(spaceName).append("; ");
        
        // 构建向量查询语句
        query.append("LOOKUP ON ").append(tagName).append(" ");
        query.append("WHERE vector_distance(").append(tagName).append(".vector, [");
        
        // 添加向量值
        for (int i = 0; i < queryVector.size(); i++) {
            if (i > 0) query.append(", ");
            query.append(queryVector.get(i));
        }
        
        query.append("]) ");
        
        // 添加距离函数和阈值条件
        if (similarityThreshold != null) {
            if (distanceFunction == DistanceFunction.COSINE) {
                query.append(">= ").append(similarityThreshold);
            } else {
                query.append("<= ").append(1.0 - similarityThreshold);
            }
        }
        
        // 添加过滤条件
        if (filter != null && !filter.isEmpty()) {
            query.append(" AND ");
            boolean first = true;
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                if (!first) query.append(" AND ");
                query.append(tagName).append(".").append(entry.getKey());
                query.append(" == ");
                if (entry.getValue() instanceof String) {
                    query.append("\"").append(entry.getValue()).append("\"");
                } else {
                    query.append(entry.getValue());
                }
                first = false;
            }
        }
        
        // 添加返回字段
        query.append(" YIELD ");
        query.append(tagName).append(".unique_id AS unique_id, ");
        query.append(tagName).append(".content AS content, ");
        
        if (includeMetadata) {
            query.append(tagName).append(".metadata AS metadata, ");
        }
        
        if (includeVector) {
            query.append(tagName).append(".vector AS vector, ");
        }
        
        query.append("vector_distance(").append(tagName).append(".vector, [");
        for (int i = 0; i < queryVector.size(); i++) {
            if (i > 0) query.append(", ");
            query.append(queryVector.get(i));
        }
        query.append("]) AS distance ");
        
        // 添加排序和限制
        query.append("ORDER BY distance ");
        if (distanceFunction == DistanceFunction.COSINE) {
            query.append("DESC ");
        } else {
            query.append("ASC ");
        }
        query.append("LIMIT ").append(topK);
        
        return query.toString();
    }
}
