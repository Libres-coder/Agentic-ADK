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

import java.util.HashMap;
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
     * 优化版本，支持更复杂的过滤条件
     */
    public String buildWhereClause() {
        StringBuilder whereClause = new StringBuilder();
        
        if (hasDocTypeFilter()) {
            whereClause.append(" AND doc.doc_type == @docType");
        }
        
        if (hasMetadataFilter()) {
            whereClause.append(buildMetadataWhereClause());
        }
        
        if (hasCustomFieldsFilter()) {
            whereClause.append(buildCustomFieldsWhereClause());
        }
        
        if (hasTagFilter()) {
            whereClause.append(" AND LENGTH(INTERSECTION(doc.tags, @tags)) > 0");
        }
        
        return whereClause.toString();
    }
    
    /**
     * 构建元数据过滤的WHERE子句
     * 支持多种操作符：==, !=, >, <, >=, <=, IN, NOT IN, LIKE, REGEX
     */
    private String buildMetadataWhereClause() {
        StringBuilder whereClause = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : metadataFilter.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                // 复杂条件：{"field": {"$op": "value"}}
                @SuppressWarnings("unchecked")
                Map<String, Object> condition = (Map<String, Object>) value;
                whereClause.append(buildComplexCondition("doc.metadata." + key, condition, "metadata_" + key));
            } else {
                // 简单条件：{"field": "value"}
                whereClause.append(String.format(" AND doc.metadata.%s == @metadata_%s", key, key));
            }
        }
        
        return whereClause.toString();
    }
    
    /**
     * 构建自定义字段过滤的WHERE子句
     */
    private String buildCustomFieldsWhereClause() {
        StringBuilder whereClause = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : customFieldsFilter.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                // 复杂条件
                @SuppressWarnings("unchecked")
                Map<String, Object> condition = (Map<String, Object>) value;
                whereClause.append(buildComplexCondition("doc.custom_fields." + key, condition, "custom_" + key));
            } else {
                // 简单条件
                whereClause.append(String.format(" AND doc.custom_fields.%s == @custom_%s", key, key));
            }
        }
        
        return whereClause.toString();
    }
    
    /**
     * 构建复杂条件表达式
     * 支持的操作符：$eq, $ne, $gt, $lt, $gte, $lte, $in, $nin, $like, $regex
     */
    private String buildComplexCondition(String fieldPath, Map<String, Object> condition, String paramPrefix) {
        StringBuilder conditionClause = new StringBuilder();
        
        for (Map.Entry<String, Object> opEntry : condition.entrySet()) {
            String operator = opEntry.getKey();
            Object value = opEntry.getValue();
            
            switch (operator) {
                case "$eq":
                    conditionClause.append(String.format(" AND %s == @%s", fieldPath, paramPrefix));
                    break;
                case "$ne":
                    conditionClause.append(String.format(" AND %s != @%s", fieldPath, paramPrefix));
                    break;
                case "$gt":
                    conditionClause.append(String.format(" AND %s > @%s", fieldPath, paramPrefix));
                    break;
                case "$lt":
                    conditionClause.append(String.format(" AND %s < @%s", fieldPath, paramPrefix));
                    break;
                case "$gte":
                    conditionClause.append(String.format(" AND %s >= @%s", fieldPath, paramPrefix));
                    break;
                case "$lte":
                    conditionClause.append(String.format(" AND %s <= @%s", fieldPath, paramPrefix));
                    break;
                case "$in":
                    conditionClause.append(String.format(" AND %s IN @%s", fieldPath, paramPrefix));
                    break;
                case "$nin":
                    conditionClause.append(String.format(" AND %s NOT IN @%s", fieldPath, paramPrefix));
                    break;
                case "$like":
                    conditionClause.append(String.format(" AND LIKE(%s, @%s)", fieldPath, paramPrefix));
                    break;
                case "$regex":
                    conditionClause.append(String.format(" AND REGEX_TEST(%s, @%s)", fieldPath, paramPrefix));
                    break;
                case "$exists":
                    if (Boolean.TRUE.equals(value)) {
                        conditionClause.append(String.format(" AND %s != null", fieldPath));
                    } else {
                        conditionClause.append(String.format(" AND %s == null", fieldPath));
                    }
                    break;
                case "$range":
                    // 范围查询：{"$range": [min, max]}
                    if (value instanceof List && ((List<?>) value).size() == 2) {
                        List<?> range = (List<?>) value;
                        conditionClause.append(String.format(" AND %s >= @%s_min AND %s <= @%s_max", 
                                fieldPath, paramPrefix, fieldPath, paramPrefix));
                    }
                    break;
                default:
                    // 默认使用等于操作
                    conditionClause.append(String.format(" AND %s == @%s", fieldPath, paramPrefix));
                    break;
            }
        }
        
        return conditionClause.toString();
    }
    
    /**
     * 构建优化的绑定变量映射
     * 处理复杂条件的参数绑定
     */
    public Map<String, Object> buildOptimizedBindVariables() {
        Map<String, Object> bindVars = new HashMap<>();
        
        // 基础参数
        bindVars.put("queryVector", queryVector);
        bindVars.put("topK", topK);
        bindVars.put("similarityThreshold", similarityThreshold);
        
        if (maxDistance != null) {
            bindVars.put("maxDistance", maxDistance);
        }
        
        if (minSimilarity != null) {
            bindVars.put("minSimilarity", minSimilarity);
        }
        
        if (hasDocTypeFilter()) {
            bindVars.put("docType", docTypeFilter);
        }
        
        if (hasTagFilter()) {
            bindVars.put("tags", tagFilter);
        }
        
        // 处理元数据过滤参数
        if (hasMetadataFilter()) {
            for (Map.Entry<String, Object> entry : metadataFilter.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof Map) {
                    // 复杂条件参数
                    @SuppressWarnings("unchecked")
                    Map<String, Object> condition = (Map<String, Object>) value;
                    bindComplexConditionParams(bindVars, "metadata_" + key, condition);
                } else {
                    // 简单条件参数
                    bindVars.put("metadata_" + key, value);
                }
            }
        }
        
        // 处理自定义字段过滤参数
        if (hasCustomFieldsFilter()) {
            for (Map.Entry<String, Object> entry : customFieldsFilter.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof Map) {
                    // 复杂条件参数
                    @SuppressWarnings("unchecked")
                    Map<String, Object> condition = (Map<String, Object>) value;
                    bindComplexConditionParams(bindVars, "custom_" + key, condition);
                } else {
                    // 简单条件参数
                    bindVars.put("custom_" + key, value);
                }
            }
        }
        
        return bindVars;
    }
    
    /**
     * 绑定复杂条件的参数
     */
    private void bindComplexConditionParams(Map<String, Object> bindVars, String paramPrefix, 
                                          Map<String, Object> condition) {
        for (Map.Entry<String, Object> opEntry : condition.entrySet()) {
            String operator = opEntry.getKey();
            Object value = opEntry.getValue();
            
            switch (operator) {
                case "$range":
                    if (value instanceof List && ((List<?>) value).size() == 2) {
                        List<?> range = (List<?>) value;
                        bindVars.put(paramPrefix + "_min", range.get(0));
                        bindVars.put(paramPrefix + "_max", range.get(1));
                    }
                    break;
                default:
                    bindVars.put(paramPrefix, value);
                    break;
            }
        }
    }
}
