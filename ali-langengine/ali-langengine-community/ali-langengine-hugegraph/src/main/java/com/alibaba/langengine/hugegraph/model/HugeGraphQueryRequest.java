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
package com.alibaba.langengine.hugegraph.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class HugeGraphQueryRequest {
    
    /**
     * 查询向量
     */
    private List<Double> queryVector;
    
    /**
     * 返回结果数量
     */
    private int k;
    
    /**
     * 最大距离阈值
     */
    private Double maxDistance;
    
    /**
     * 最小相似度阈值
     */
    private Double minSimilarity;
    
    /**
     * 元数据过滤条件
     */
    private Map<String, Object> metadataFilter;
    
    /**
     * 顶点标签过滤
     */
    private String vertexLabel;
    
    /**
     * 内容过滤关键词
     */
    private String contentFilter;
    
    /**
     * 文档类型过滤
     */
    private String docTypeFilter;
    
    /**
     * 标签过滤
     */
    private List<String> tagFilter;
    
    /**
     * 语言过滤
     */
    private String languageFilter;
    
    /**
     * 分类过滤
     */
    private String categoryFilter;
    
    /**
     * 状态过滤
     */
    private String statusFilter;
    
    /**
     * 来源过滤
     */
    private String sourceFilter;
    
    /**
     * 日期范围过滤（开始时间）
     */
    private String startDate;
    
    /**
     * 日期范围过滤（结束时间）
     */
    private String endDate;
    
    /**
     * 优先级范围过滤（最小值）
     */
    private Integer minPriority;
    
    /**
     * 优先级范围过滤（最大值）
     */
    private Integer maxPriority;
    
    /**
     * 排序字段
     */
    private String sortBy;
    
    /**
     * 排序方向（ASC/DESC）
     */
    private String sortOrder;
    
    /**
     * 分页偏移量
     */
    private Integer offset;
    
    /**
     * 分页大小
     */
    private Integer limit;
    
    /**
     * 是否包含向量数据
     */
    private Boolean includeVectors;
    
    /**
     * 是否包含元数据
     */
    private Boolean includeMetadata;
    
    /**
     * 自定义字段过滤
     */
    private Map<String, Object> customFieldFilter;
    
    /**
     * 分区键过滤
     */
    private String partitionKeyFilter;
    
    /**
     * 查询超时时间（毫秒）
     */
    private Long timeoutMs;
    
    /**
     * 是否启用缓存
     */
    private Boolean enableCache;
    
    /**
     * 相似度计算函数类型
     */
    private String similarityFunction;
    
    /**
     * 验证查询请求的有效性
     */
    public boolean isValid() {
        return queryVector != null && !queryVector.isEmpty() && k > 0;
    }
    
    /**
     * 获取有效的K值
     */
    public int getValidK() {
        return Math.max(1, Math.min(k, 10000)); // 限制在合理范围内
    }
    
    /**
     * 检查是否有元数据过滤条件
     */
    public boolean hasMetadataFilter() {
        return metadataFilter != null && !metadataFilter.isEmpty();
    }
    
    /**
     * 检查是否有内容过滤条件
     */
    public boolean hasContentFilter() {
        return contentFilter != null && !contentFilter.trim().isEmpty();
    }
    
    /**
     * 检查是否有日期范围过滤
     */
    public boolean hasDateRangeFilter() {
        return startDate != null || endDate != null;
    }
    
    /**
     * 检查是否有优先级范围过滤
     */
    public boolean hasPriorityRangeFilter() {
        return minPriority != null || maxPriority != null;
    }
    
    /**
     * 获取排序信息
     */
    public String getSortInfo() {
        if (sortBy == null) return null;
        return sortBy + " " + (sortOrder != null ? sortOrder : "ASC");
    }
    
    /**
     * 构建Gremlin查询条件字符串
     */
    public String buildGremlinFilter() {
        StringBuilder filter = new StringBuilder();
        
        if (vertexLabel != null) {
            filter.append(".hasLabel('").append(vertexLabel).append("')");
        }
        
        if (hasContentFilter()) {
            filter.append(".has('content', containing('").append(contentFilter).append("'))");
        }
        
        if (docTypeFilter != null) {
            filter.append(".has('docType', '").append(docTypeFilter).append("')");
        }
        
        if (languageFilter != null) {
            filter.append(".has('language', '").append(languageFilter).append("')");
        }
        
        if (categoryFilter != null) {
            filter.append(".has('category', '").append(categoryFilter).append("')");
        }
        
        if (statusFilter != null) {
            filter.append(".has('status', '").append(statusFilter).append("')");
        }
        
        if (sourceFilter != null) {
            filter.append(".has('source', '").append(sourceFilter).append("')");
        }
        
        if (hasPriorityRangeFilter()) {
            if (minPriority != null) {
                filter.append(".has('priority', gte(").append(minPriority).append("))");
            }
            if (maxPriority != null) {
                filter.append(".has('priority', lte(").append(maxPriority).append("))");
            }
        }
        
        if (partitionKeyFilter != null) {
            filter.append(".has('partitionKey', '").append(partitionKeyFilter).append("')");
        }
        
        return filter.toString();
    }
    
    @Override
    public String toString() {
        return String.format("HugeGraphQueryRequest{vectorDim=%d, k=%d, maxDistance=%s, filters=%s}", 
                           queryVector != null ? queryVector.size() : 0,
                           k,
                           maxDistance,
                           hasMetadataFilter() ? "yes" : "no");
    }
}
