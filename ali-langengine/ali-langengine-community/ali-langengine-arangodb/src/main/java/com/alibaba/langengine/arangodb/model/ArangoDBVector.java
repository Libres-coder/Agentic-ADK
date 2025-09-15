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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArangoDBVector {
    
    /**
     * 文档唯一标识符
     */
    @JsonProperty("_key")
    private String key;
    
    /**
     * 文档ID
     */
    @JsonProperty("_id")
    private String id;
    
    /**
     * 文档修订版本
     */
    @JsonProperty("_rev")
    private String rev;
    
    /**
     * 向量嵌入
     */
    @JsonProperty("vector")
    private List<Double> vector;
    
    /**
     * 文档内容
     */
    @JsonProperty("content")
    private String content;
    
    /**
     * 文档标题
     */
    @JsonProperty("title")
    private String title;
    
    /**
     * 文档唯一ID
     */
    @JsonProperty("unique_id")
    private String uniqueId;
    
    /**
     * 文档索引
     */
    @JsonProperty("doc_index")
    private String docIndex;
    
    /**
     * 元数据
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    /**
     * 创建时间戳
     */
    @JsonProperty("created_at")
    private Instant createdAt;
    
    /**
     * 更新时间戳
     */
    @JsonProperty("updated_at")
    private Instant updatedAt;
    
    /**
     * 相似度得分
     */
    @JsonProperty("score")
    private Double score;
    
    /**
     * 距离值
     */
    @JsonProperty("distance")
    private Double distance;
    
    /**
     * 向量维度
     */
    @JsonProperty("dimension")
    private Integer dimension;
    
    /**
     * 文档类型
     */
    @JsonProperty("doc_type")
    private String docType;
    
    /**
     * 标签
     */
    @JsonProperty("tags")
    private List<String> tags;
    
    /**
     * 自定义字段
     */
    @JsonProperty("custom_fields")
    private Map<String, Object> customFields;
    
    /**
     * 验证向量数据
     */
    public void validate() {
        if (vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("Vector cannot be null or empty");
        }
        
        if (uniqueId == null || uniqueId.trim().isEmpty()) {
            throw new IllegalArgumentException("Unique ID cannot be null or empty");
        }
        
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        
        // 检查向量维度一致性
        if (dimension != null && !dimension.equals(vector.size())) {
            throw new IllegalArgumentException(
                String.format("Vector dimension mismatch: expected %d, actual %d", 
                            dimension, vector.size()));
        }
        
        // 设置维度
        if (dimension == null) {
            this.dimension = vector.size();
        }
        
        // 设置时间戳
        if (createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
        
        // 初始化元数据
        if (metadata == null) {
            this.metadata = new HashMap<>();
        }
        
        // 初始化自定义字段
        if (customFields == null) {
            this.customFields = new HashMap<>();
        }
    }
    
    /**
     * 添加元数据
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
    
    /**
     * 添加自定义字段
     */
    public void addCustomField(String key, Object value) {
        if (customFields == null) {
            customFields = new HashMap<>();
        }
        customFields.put(key, value);
    }
    
    /**
     * 获取向量维度
     */
    public int getVectorDimension() {
        return vector != null ? vector.size() : 0;
    }
    
    /**
     * 设置向量并自动更新维度
     */
    public void setVector(List<Double> vector) {
        this.vector = vector;
        if (vector != null) {
            this.dimension = vector.size();
        }
    }
    
    /**
     * 是否有效的向量文档
     */
    public boolean isValid() {
        return vector != null && !vector.isEmpty() && 
               uniqueId != null && !uniqueId.trim().isEmpty() &&
               content != null && !content.trim().isEmpty();
    }
    
    /**
     * 创建副本
     */
    public ArangoDBVector copy() {
        return ArangoDBVector.builder()
                .key(this.key)
                .id(this.id)
                .rev(this.rev)
                .vector(this.vector != null ? List.copyOf(this.vector) : null)
                .content(this.content)
                .title(this.title)
                .uniqueId(this.uniqueId)
                .docIndex(this.docIndex)
                .metadata(this.metadata != null ? new HashMap<>(this.metadata) : null)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .score(this.score)
                .distance(this.distance)
                .dimension(this.dimension)
                .docType(this.docType)
                .tags(this.tags != null ? List.copyOf(this.tags) : null)
                .customFields(this.customFields != null ? new HashMap<>(this.customFields) : null)
                .build();
    }
}
