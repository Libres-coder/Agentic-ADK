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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimescaleDBVector {
    
    /**
     * 向量ID，主键
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * 向量内容
     */
    @JsonProperty("content")
    private String content;
    
    /**
     * 向量数据
     */
    @JsonProperty("vector")
    private List<Double> vector;
    
    /**
     * 向量维度
     */
    @JsonProperty("vector_dimension")
    private Integer vectorDimension;
    
    /**
     * 文档元数据
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    /**
     * 时间戳，TimescaleDB核心字段
     */
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 相似度分数（查询时使用）
     */
    @JsonProperty("score")
    private Double score;
    
    /**
     * 距离值（查询时使用）
     */
    @JsonProperty("distance")
    private Double distance;
    
    /**
     * 文档索引
     */
    @JsonProperty("doc_index")
    private Integer docIndex;
    
    /**
     * 数据分区标识
     */
    @JsonProperty("partition_key")
    private String partitionKey;
    
    /**
     * 数据版本
     */
    @JsonProperty("version")
    private Integer version;
    
    /**
     * 状态标识
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * 标签信息
     */
    @JsonProperty("tags")
    private List<String> tags;
    
    /**
     * 获取向量维度，如果未设置则根据向量数据计算
     */
    public Integer getVectorDimension() {
        if (vectorDimension != null) {
            return vectorDimension;
        }
        if (vector != null && !vector.isEmpty()) {
            return vector.size();
        }
        return null;
    }
    
    /**
     * 验证向量数据的有效性
     */
    public boolean isValidVector() {
        if (vector == null || vector.isEmpty()) {
            return false;
        }
        
        // 检查是否包含null值
        for (Double val : vector) {
            if (val == null || !Double.isFinite(val)) {
                return false;
            }
        }
        
        // 检查维度是否一致
        if (vectorDimension != null && !vectorDimension.equals(vector.size())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取安全的元数据，避免null
     */
    public Map<String, Object> getSafeMetadata() {
        return metadata != null ? metadata : new HashMap<>();
    }
    
    /**
     * 设置默认时间戳
     */
    public void setDefaultTimestamp() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 构建时序分区键
     */
    public String buildPartitionKey() {
        if (timestamp != null) {
            return timestamp.toLocalDate().toString();
        }
        return LocalDateTime.now().toLocalDate().toString();
    }
    
    /**
     * 获取时间戳的Instant表示
     */
    public Instant getTimestampAsInstant() {
        if (timestamp != null) {
            return timestamp.atZone(java.time.ZoneId.systemDefault()).toInstant();
        }
        return null;
    }
    
    /**
     * 克隆向量对象，用于缓存和数据处理
     */
    public TimescaleDBVector deepCopy() {
        return TimescaleDBVector.builder()
                .id(this.id)
                .content(this.content)
                .vector(this.vector != null ? List.copyOf(this.vector) : null)
                .vectorDimension(this.vectorDimension)
                .metadata(this.metadata != null ? new HashMap<>(this.metadata) : null)
                .timestamp(this.timestamp)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .score(this.score)
                .distance(this.distance)
                .docIndex(this.docIndex)
                .partitionKey(this.partitionKey)
                .version(this.version)
                .status(this.status)
                .tags(this.tags != null ? List.copyOf(this.tags) : null)
                .build();
    }
    
    /**
     * 获取向量的字符串表示，用于日志和调试
     */
    public String getVectorSummary() {
        if (vector == null || vector.isEmpty()) {
            return "empty vector";
        }
        
        int size = vector.size();
        if (size <= 4) {
            return "vector" + vector.toString();
        } else {
            return String.format("vector[%d](%.3f, %.3f, ..., %.3f, %.3f)", 
                    size, vector.get(0), vector.get(1), 
                    vector.get(size-2), vector.get(size-1));
        }
    }
    
    /**
     * 检查是否为时序数据
     */
    public boolean isTimeSeriesData() {
        return timestamp != null && partitionKey != null;
    }
}
