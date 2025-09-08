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
public class TimescaleDBQueryResponse {
    
    /**
     * 查询结果向量列表
     */
    private List<TimescaleDBVector> vectors;
    
    /**
     * 查询执行时间（毫秒）
     */
    private Long executionTimeMs;
    
    /**
     * 总结果数量
     */
    private Integer totalCount;
    
    /**
     * 返回结果数量
     */
    private Integer returnedCount;
    
    /**
     * 查询是否成功
     */
    @Builder.Default
    private boolean success = true;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 查询开始时间
     */
    private LocalDateTime queryStartTime;
    
    /**
     * 查询结束时间
     */
    private LocalDateTime queryEndTime;
    
    /**
     * 使用的相似度计算方法
     */
    private TimescaleDBQueryRequest.SimilarityMetric similarityMetric;
    
    /**
     * 查询统计信息
     */
    private QueryStatistics statistics;
    
    /**
     * 时序聚合结果（如果有）
     */
    private List<TimeSeriesAggregation> timeSeriesAggregations;
    
    /**
     * 分页信息
     */
    private PaginationInfo pagination;
    
    /**
     * 查询统计信息
     */
    @Data
    @Builder
    public static class QueryStatistics {
        /**
         * 扫描的行数
         */
        private Long scannedRows;
        
        /**
         * 过滤的行数
         */
        private Long filteredRows;
        
        /**
         * 索引使用情况
         */
        private String indexUsage;
        
        /**
         * 查询计划
         */
        private String queryPlan;
        
        /**
         * 内存使用量（字节）
         */
        private Long memoryUsageBytes;
        
        /**
         * CPU时间（毫秒）
         */
        private Long cpuTimeMs;
        
        /**
         * IO时间（毫秒）
         */
        private Long ioTimeMs;
    }
    
    /**
     * 时序聚合结果
     */
    @Data
    @Builder
    public static class TimeSeriesAggregation {
        /**
         * 时间窗口开始时间
         */
        private LocalDateTime windowStart;
        
        /**
         * 时间窗口结束时间
         */
        private LocalDateTime windowEnd;
        
        /**
         * 聚合值
         */
        private Double aggregateValue;
        
        /**
         * 聚合类型
         */
        private TimescaleDBQueryRequest.TimeAggregationType aggregationType;
        
        /**
         * 数据点数量
         */
        private Integer dataPoints;
        
        /**
         * 聚合元数据
         */
        private Map<String, Object> metadata;
    }
    
    /**
     * 分页信息
     */
    @Data
    @Builder
    public static class PaginationInfo {
        /**
         * 当前页偏移量
         */
        private Integer offset;
        
        /**
         * 页大小
         */
        private Integer limit;
        
        /**
         * 总页数
         */
        private Integer totalPages;
        
        /**
         * 当前页号（从1开始）
         */
        private Integer currentPage;
        
        /**
         * 是否有下一页
         */
        private Boolean hasNext;
        
        /**
         * 是否有上一页
         */
        private Boolean hasPrevious;
    }
    
    /**
     * 获取平均距离
     */
    public Double getAverageDistance() {
        if (vectors == null || vectors.isEmpty()) {
            return null;
        }
        
        double sum = 0;
        int count = 0;
        for (TimescaleDBVector vector : vectors) {
            if (vector.getDistance() != null) {
                sum += vector.getDistance();
                count++;
            }
        }
        
        return count > 0 ? sum / count : null;
    }
    
    /**
     * 获取最高分数
     */
    public Double getMaxScore() {
        if (vectors == null || vectors.isEmpty()) {
            return null;
        }
        
        return vectors.stream()
                .filter(v -> v.getScore() != null)
                .mapToDouble(TimescaleDBVector::getScore)
                .max()
                .orElse(Double.NaN);
    }
    
    /**
     * 获取最低距离
     */
    public Double getMinDistance() {
        if (vectors == null || vectors.isEmpty()) {
            return null;
        }
        
        return vectors.stream()
                .filter(v -> v.getDistance() != null)
                .mapToDouble(TimescaleDBVector::getDistance)
                .min()
                .orElse(Double.NaN);
    }
    
    /**
     * 检查是否为空结果
     */
    public boolean isEmpty() {
        return vectors == null || vectors.isEmpty();
    }
    
    /**
     * 获取结果摘要
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("TimescaleDB Query Result: ");
        summary.append(success ? "SUCCESS" : "FAILED");
        
        if (success) {
            summary.append(String.format(" - Returned: %d/%d vectors", 
                    returnedCount != null ? returnedCount : 0,
                    totalCount != null ? totalCount : 0));
            
            if (executionTimeMs != null) {
                summary.append(String.format(" - Time: %dms", executionTimeMs));
            }
            
            Double avgDistance = getAverageDistance();
            if (avgDistance != null) {
                summary.append(String.format(" - Avg Distance: %.4f", avgDistance));
            }
        } else {
            summary.append(" - Error: ").append(errorMessage);
        }
        
        return summary.toString();
    }
    
    /**
     * 创建失败响应
     */
    public static TimescaleDBQueryResponse failure(String errorMessage) {
        return TimescaleDBQueryResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .queryEndTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建空结果响应
     */
    public static TimescaleDBQueryResponse empty() {
        return TimescaleDBQueryResponse.builder()
                .success(true)
                .vectors(List.of())
                .totalCount(0)
                .returnedCount(0)
                .queryEndTime(LocalDateTime.now())
                .build();
    }
}
