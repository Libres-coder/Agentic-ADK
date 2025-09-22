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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArangoDBQueryResponse {
    
    /**
     * 查询是否成功
     */
    private boolean success;
    
    /**
     * 查询结果
     */
    private List<ArangoDBVector> results;
    
    /**
     * 总结果数量
     */
    private long totalCount;
    
    /**
     * 查询执行时间(毫秒)
     */
    private long executionTimeMs;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 查询统计信息
     */
    private QueryStatistics statistics;
    
    /**
     * 查询统计信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryStatistics {
        
        /**
         * 扫描的文档数量
         */
        private long scannedCount;
        
        /**
         * 过滤的文档数量
         */
        private long filteredCount;
        
        /**
         * 返回的文档数量
         */
        private long writesExecuted;
        
        /**
         * 读取的文档数量
         */
        private long writesIgnored;
        
        /**
         * 查询缓存命中
         */
        private boolean cacheHit;
        
        /**
         * 使用的索引数量
         */
        private int indexesUsed;
        
        /**
         * 查询计划节点数量
         */
        private int planNodes;
        
        /**
         * 查询优化次数
         */
        private int rulesExecuted;
    }
    
    /**
     * 创建成功响应
     */
    public static ArangoDBQueryResponse success(List<ArangoDBVector> results, long executionTimeMs) {
        return ArangoDBQueryResponse.builder()
                .success(true)
                .results(results)
                .totalCount(results != null ? results.size() : 0)
                .executionTimeMs(executionTimeMs)
                .build();
    }
    
    /**
     * 创建成功响应（带统计信息）
     */
    public static ArangoDBQueryResponse success(List<ArangoDBVector> results, long executionTimeMs, QueryStatistics statistics) {
        return ArangoDBQueryResponse.builder()
                .success(true)
                .results(results)
                .totalCount(results != null ? results.size() : 0)
                .executionTimeMs(executionTimeMs)
                .statistics(statistics)
                .build();
    }
    
    /**
     * 创建失败响应
     */
    public static ArangoDBQueryResponse failure(String errorCode, String errorMessage) {
        return ArangoDBQueryResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .results(List.of())
                .totalCount(0)
                .build();
    }
    
    /**
     * 创建失败响应（带执行时间）
     */
    public static ArangoDBQueryResponse failure(String errorCode, String errorMessage, long executionTimeMs) {
        return ArangoDBQueryResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .executionTimeMs(executionTimeMs)
                .results(List.of())
                .totalCount(0)
                .build();
    }
    
    /**
     * 是否有结果
     */
    public boolean hasResults() {
        return success && results != null && !results.isEmpty();
    }
    
    /**
     * 获取结果数量
     */
    public int getResultCount() {
        return results != null ? results.size() : 0;
    }
    
    /**
     * 是否为空结果
     */
    public boolean isEmpty() {
        return !hasResults();
    }
    
    /**
     * 获取第一个结果
     */
    public ArangoDBVector getFirstResult() {
        if (hasResults()) {
            return results.get(0);
        }
        return null;
    }
    
    /**
     * 获取最高分结果
     */
    public ArangoDBVector getHighestScoreResult() {
        if (!hasResults()) {
            return null;
        }
        
        return results.stream()
                .filter(result -> result.getScore() != null)
                .max((r1, r2) -> Double.compare(r1.getScore(), r2.getScore()))
                .orElse(results.get(0));
    }
    
    /**
     * 过滤结果（基于分数阈值）
     */
    public ArangoDBQueryResponse filterByScore(double minScore) {
        if (!hasResults()) {
            return this;
        }
        
        List<ArangoDBVector> filteredResults = results.stream()
                .filter(result -> result.getScore() != null && result.getScore() >= minScore)
                .toList();
        
        return ArangoDBQueryResponse.builder()
                .success(this.success)
                .results(filteredResults)
                .totalCount(filteredResults.size())
                .executionTimeMs(this.executionTimeMs)
                .statistics(this.statistics)
                .errorCode(this.errorCode)
                .errorMessage(this.errorMessage)
                .build();
    }
    
    /**
     * 限制结果数量
     */
    public ArangoDBQueryResponse limitResults(int limit) {
        if (!hasResults() || limit <= 0) {
            return this;
        }
        
        List<ArangoDBVector> limitedResults = results.stream()
                .limit(limit)
                .toList();
        
        return ArangoDBQueryResponse.builder()
                .success(this.success)
                .results(limitedResults)
                .totalCount(this.totalCount) // 保持原始总数
                .executionTimeMs(this.executionTimeMs)
                .statistics(this.statistics)
                .errorCode(this.errorCode)
                .errorMessage(this.errorMessage)
                .build();
    }
}
