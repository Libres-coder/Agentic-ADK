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
package com.alibaba.langengine.arangodb.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
@Builder
public class ArangoDBQueryRequest {
    
    /**
     * 查询向量
     */
    private List<Double> queryVector;
    
    /**
     * 返回结果数量
     */
    private int topK;
    
    /**
     * 相似度阈值
     */
    private double similarityThreshold;
    
    /**
     * 距离函数类型
     */
    private DistanceFunction distanceFunction;
    
    /**
     * 元数据过滤条件
     */
    private Map<String, Object> metadataFilter;
    
    /**
     * 自定义字段过滤条件
     */
    private Map<String, Object> customFieldsFilter;
    
    /**
     * 文档类型过滤
     */
    private String docTypeFilter;
    
    /**
     * 标签过滤
     */
    private List<String> tagFilter;
    
    /**
     * 是否包含向量数据
     */
    private boolean includeVector;
    
    /**
     * 是否包含元数据
     */
    private boolean includeMetadata;
    
    /**
     * 最大距离值
     */
    private Double maxDistance;
    
    /**
     * 最小相似度
     */
    private Double minSimilarity;
    
    /**
     * 距离函数枚举
     */
    public enum DistanceFunction {
        COSINE("cosine", "余弦相似度"),
        EUCLIDEAN("euclidean", "欧几里得距离"),
        MANHATTAN("manhattan", "曼哈顿距离"),
        HAMMING("hamming", "汉明距离"),
        DOT_PRODUCT("dot", "点积相似度");
        
        private final String value;
        private final String description;
        
        DistanceFunction(String value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public String getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static DistanceFunction fromString(String value) {
            for (DistanceFunction function : values()) {
                if (function.getValue().equalsIgnoreCase(value)) {
                    return function;
                }
            }
            return COSINE; // 默认值
        }
    }
    
    /**
     * 验证查询请求
     */
    public void validate() {
        if (queryVector == null || queryVector.isEmpty()) {
            throw new IllegalArgumentException("Query vector cannot be null or empty");
        }
        
        if (topK <= 0) {
            throw new IllegalArgumentException("Top K must be positive");
        }
        
        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
        }
        
        if (distanceFunction == null) {
            this.distanceFunction = DistanceFunction.COSINE;
        }
        
        if (maxDistance != null && maxDistance < 0.0) {
            throw new IllegalArgumentException("Max distance must be non-negative");
        }
        
        if (minSimilarity != null && (minSimilarity < 0.0 || minSimilarity > 1.0)) {
            throw new IllegalArgumentException("Min similarity must be between 0.0 and 1.0");
        }
    }
    
    /**
     * 获取向量维度
     */
    public int getVectorDimension() {
        return queryVector != null ? queryVector.size() : 0;
    }
    
    /**
     * 是否有元数据过滤
     */
    public boolean hasMetadataFilter() {
        return metadataFilter != null && !metadataFilter.isEmpty();
    }
    
    /**
     * 是否有自定义字段过滤
     */
    public boolean hasCustomFieldsFilter() {
        return customFieldsFilter != null && !customFieldsFilter.isEmpty();
    }
    
    /**
     * 是否有文档类型过滤
     */
    public boolean hasDocTypeFilter() {
        return docTypeFilter != null && !docTypeFilter.trim().isEmpty();
    }
    
    /**
     * 是否有标签过滤
     */
    public boolean hasTagFilter() {
        return tagFilter != null && !tagFilter.isEmpty();
    }
    
    /**
     * 构建AQL查询的WHERE子句
     */
    public String buildWhereClause() {
        StringBuilder whereClause = new StringBuilder();
        
        if (hasDocTypeFilter()) {
            whereClause.append(" AND doc.doc_type == @docType");
        }
        
        if (hasMetadataFilter()) {
            for (String key : metadataFilter.keySet()) {
                whereClause.append(String.format(" AND doc.metadata.%s == @metadata_%s", key, key));
            }
        }
        
        if (hasCustomFieldsFilter()) {
            for (String key : customFieldsFilter.keySet()) {
                whereClause.append(String.format(" AND doc.custom_fields.%s == @custom_%s", key, key));
            }
        }
        
        if (hasTagFilter()) {
            whereClause.append(" AND LENGTH(INTERSECTION(doc.tags, @tags)) > 0");
        }
        
        return whereClause.toString();
    }
}
