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
public class HugeGraphQueryResponse {
    
    /**
     * 查询结果列表
     */
    private List<HugeGraphVector> results;
    
    /**
     * 实际返回的结果数量
     */
    private int resultCount;
    
    /**
     * 查询执行时间（毫秒）
     */
    private long executionTimeMs;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息（如果查询失败）
     */
    private String errorMessage;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 总匹配数量（用于分页）
     */
    private Long totalCount;
    
    /**
     * 是否有更多结果
     */
    private Boolean hasMore;
    
    /**
     * 下一页的偏移量
     */
    private Integer nextOffset;
    
    /**
     * 查询统计信息
     */
    private Map<String, Object> statistics;
    
    /**
     * 查询参数回显
     */
    private HugeGraphQueryRequest originalRequest;
    
    /**
     * 查询ID（用于追踪）
     */
    private String queryId;
    
    /**
     * 查询开始时间戳
     */
    private Long startTimestamp;
    
    /**
     * 查询结束时间戳
     */
    private Long endTimestamp;
    
    /**
     * 使用的相似度函数
     */
    private String similarityFunction;
    
    /**
     * 是否使用了缓存
     */
    private Boolean fromCache;
    
    /**
     * 缓存键（如果使用了缓存）
     */
    private String cacheKey;
    
    /**
     * 服务器节点信息
     */
    private String serverNode;
    
    /**
     * 数据库版本信息
     */
    private String dbVersion;
    
    /**
     * 扩展信息
     */
    private Map<String, Object> extensions;
    
    /**
     * 创建成功响应
     */
    public static HugeGraphQueryResponse success(List<HugeGraphVector> results, long executionTimeMs) {
        return HugeGraphQueryResponse.builder()
                .results(results)
                .resultCount(results != null ? results.size() : 0)
                .executionTimeMs(executionTimeMs)
                .success(true)
                .build();
    }
    
    /**
     * 创建成功响应（带分页信息）
     */
    public static HugeGraphQueryResponse success(List<HugeGraphVector> results, long executionTimeMs, 
                                               Long totalCount, Boolean hasMore, Integer nextOffset) {
        return HugeGraphQueryResponse.builder()
                .results(results)
                .resultCount(results != null ? results.size() : 0)
                .executionTimeMs(executionTimeMs)
                .success(true)
                .totalCount(totalCount)
                .hasMore(hasMore)
                .nextOffset(nextOffset)
                .build();
    }
    
    /**
     * 创建失败响应
     */
    public static HugeGraphQueryResponse failure(String errorMessage, String errorCode, long executionTimeMs) {
        return HugeGraphQueryResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .executionTimeMs(executionTimeMs)
                .resultCount(0)
                .build();
    }
    
    /**
     * 创建异常响应
     */
    public static HugeGraphQueryResponse error(Exception exception, long executionTimeMs) {
        return HugeGraphQueryResponse.builder()
                .success(false)
                .errorMessage(exception.getMessage())
                .errorCode("QUERY_ERROR")
                .executionTimeMs(executionTimeMs)
                .resultCount(0)
                .build();
    }
    
    /**
     * 检查是否有结果
     */
    public boolean hasResults() {
        return results != null && !results.isEmpty();
    }
    
    /**
     * 获取最高相似度分数
     */
    public Double getMaxScore() {
        if (!hasResults()) return null;
        return results.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(HugeGraphVector::getScore)
                .max()
                .orElse(0.0);
    }
    
    /**
     * 获取最低相似度分数
     */
    public Double getMinScore() {
        if (!hasResults()) return null;
        return results.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(HugeGraphVector::getScore)
                .min()
                .orElse(0.0);
    }
    
    /**
     * 获取平均相似度分数
     */
    public Double getAverageScore() {
        if (!hasResults()) return null;
        return results.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(HugeGraphVector::getScore)
                .average()
                .orElse(0.0);
    }
    
    /**
     * 添加统计信息
     */
    public void addStatistic(String key, Object value) {
        if (statistics == null) {
            statistics = new java.util.HashMap<>();
        }
        statistics.put(key, value);
    }
    
    /**
     * 获取统计信息
     */
    public Object getStatistic(String key) {
        return statistics != null ? statistics.get(key) : null;
    }
    
    /**
     * 添加扩展信息
     */
    public void addExtension(String key, Object value) {
        if (extensions == null) {
            extensions = new java.util.HashMap<>();
        }
        extensions.put(key, value);
    }
    
    /**
     * 获取扩展信息
     */
    public Object getExtension(String key) {
        return extensions != null ? extensions.get(key) : null;
    }
    
    /**
     * 设置时间戳
     */
    public void setTimestamps(long startTime, long endTime) {
        this.startTimestamp = startTime;
        this.endTimestamp = endTime;
        this.executionTimeMs = endTime - startTime;
    }
    
    /**
     * 过滤结果（根据最小相似度）
     */
    public void filterByMinSimilarity(double minSimilarity) {
        if (!hasResults()) return;
        
        results = results.stream()
                .filter(r -> r.getScore() != null && r.getScore() >= minSimilarity)
                .collect(java.util.stream.Collectors.toList());
        
        resultCount = results.size();
    }
    
    /**
     * 过滤结果（根据最大距离）
     */
    public void filterByMaxDistance(double maxDistance) {
        if (!hasResults()) return;
        
        results = results.stream()
                .filter(r -> r.getDistance() != null && r.getDistance() <= maxDistance)
                .collect(java.util.stream.Collectors.toList());
        
        resultCount = results.size();
    }
    
    /**
     * 排序结果（按相似度降序）
     */
    public void sortByScoreDesc() {
        if (!hasResults()) return;
        
        results.sort((a, b) -> {
            Double scoreA = a.getScore();
            Double scoreB = b.getScore();
            if (scoreA == null && scoreB == null) return 0;
            if (scoreA == null) return 1;
            if (scoreB == null) return -1;
            return Double.compare(scoreB, scoreA); // 降序
        });
    }
    
    /**
     * 排序结果（按距离升序）
     */
    public void sortByDistanceAsc() {
        if (!hasResults()) return;
        
        results.sort((a, b) -> {
            Double distA = a.getDistance();
            Double distB = b.getDistance();
            if (distA == null && distB == null) return 0;
            if (distA == null) return 1;
            if (distB == null) return -1;
            return Double.compare(distA, distB); // 升序
        });
    }
    
    @Override
    public String toString() {
        return String.format("HugeGraphQueryResponse{success=%s, resultCount=%d, executionTimeMs=%d, errorMessage='%s'}", 
                           success, resultCount, executionTimeMs, errorMessage);
    }
}
