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
package com.alibaba.langengine.timescaledb.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Data
@Builder
public class TimescaleDBQueryRequest {
    
    /**
     * 查询向量
     */
    private List<Double> queryVector;
    
    /**
     * 返回结果数量
     */
    @Builder.Default
    private int limit = 10;
    
    /**
     * 相似度阈值
     */
    private Double similarityThreshold;
    
    /**
     * 距离阈值
     */
    private Double distanceThreshold;
    
    /**
     * 相似度计算方法
     */
    @Builder.Default
    private SimilarityMetric similarityMetric = SimilarityMetric.COSINE;
    
    /**
     * 时间范围过滤 - 开始时间
     */
    private LocalDateTime timeRangeStart;
    
    /**
     * 时间范围过滤 - 结束时间
     */
    private LocalDateTime timeRangeEnd;
    
    /**
     * 元数据过滤条件
     */
    private Map<String, Object> metadataFilter;
    
    /**
     * 标签过滤
     */
    private List<String> tagFilter;
    
    /**
     * 状态过滤
     */
    private String statusFilter;
    
    /**
     * 分区过滤
     */
    private List<String> partitionFilter;
    
    /**
     * 是否包含向量数据
     */
    @Builder.Default
    private boolean includeVectors = false;
    
    /**
     * 是否包含元数据
     */
    @Builder.Default
    private boolean includeMetadata = true;
    
    /**
     * 排序字段
     */
    private String orderBy;
    
    /**
     * 排序方向
     */
    @Builder.Default
    private SortOrder sortOrder = SortOrder.ASC;
    
    /**
     * 时序聚合类型
     */
    private TimeAggregationType timeAggregation;
    
    /**
     * 时序聚合间隔
     */
    private String timeAggregationInterval;
    
    /**
     * 分页偏移量
     */
    @Builder.Default
    private int offset = 0;
    
    /**
     * 相似度计算方法枚举
     */
    public enum SimilarityMetric {
        /**
         * 余弦相似度
         */
        COSINE("cosine", "<->"),
        
        /**
         * L2距离（欧氏距离）
         */
        L2("l2", "<->"),
        
        /**
         * 内积
         */
        INNER_PRODUCT("inner_product", "<#>"),
        
        /**
         * L1距离（曼哈顿距离）
         */
        L1("l1", "<+>");
        
        private final String name;
        private final String operator;
        
        SimilarityMetric(String name, String operator) {
            this.name = name;
            this.operator = operator;
        }
        
        public String getName() {
            return name;
        }
        
        public String getOperator() {
            return operator;
        }
    }
    
    /**
     * 排序方向枚举
     */
    public enum SortOrder {
        ASC, DESC
    }
    
    /**
     * 时序聚合类型枚举
     */
    public enum TimeAggregationType {
        /**
         * 平均值
         */
        AVG,
        
        /**
         * 最大值
         */
        MAX,
        
        /**
         * 最小值
         */
        MIN,
        
        /**
         * 计数
         */
        COUNT,
        
        /**
         * 求和
         */
        SUM,
        
        /**
         * 最近值
         */
        LAST,
        
        /**
         * 最早值
         */
        FIRST
    }
    
    /**
     * 验证查询请求的有效性
     */
    public boolean isValid() {
        if (queryVector == null || queryVector.isEmpty()) {
            return false;
        }
        
        if (limit <= 0) {
            return false;
        }
        
        if (offset < 0) {
            return false;
        }
        
        // 验证时间范围
        if (timeRangeStart != null && timeRangeEnd != null) {
            return !timeRangeStart.isAfter(timeRangeEnd);
        }
        
        return true;
    }
    
    /**
     * 是否使用时间过滤
     */
    public boolean hasTimeFilter() {
        return timeRangeStart != null || timeRangeEnd != null;
    }
    
    /**
     * 是否使用元数据过滤
     */
    public boolean hasMetadataFilter() {
        return metadataFilter != null && !metadataFilter.isEmpty();
    }
    
    /**
     * 是否使用时序聚合
     */
    public boolean hasTimeAggregation() {
        return timeAggregation != null && timeAggregationInterval != null;
    }
    
    /**
     * 获取相似度操作符
     */
    public String getSimilarityOperator() {
        return similarityMetric.getOperator();
    }
    
    /**
     * 获取排序SQL片段
     */
    public String getOrderBySql() {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return "distance " + sortOrder.name();
        }
        return orderBy + " " + sortOrder.name();
    }
}
