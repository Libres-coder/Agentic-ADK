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
public class HugeGraphVector {
    
    /**
     * 向量唯一标识符
     */
    private String id;
    
    /**
     * 顶点ID（HugeGraph中的顶点标识符）
     */
    private Object vertexId;
    
    /**
     * 文档内容
     */
    private String content;
    
    /**
     * 向量表示
     */
    private List<Double> vector;
    
    /**
     * 向量维度
     */
    private Integer vectorDimension;
    
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文档唯一标识
     */
    private String uniqueId;
    
    /**
     * 文档索引
     */
    private String docIndex;
    
    /**
     * 元数据信息
     */
    private Map<String, Object> metadata;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 相似度得分（查询时使用）
     */
    private Double score;
    
    /**
     * 距离值（查询时使用）
     */
    private Double distance;
    
    /**
     * 文档类型
     */
    private String docType;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 自定义字段
     */
    private Map<String, Object> customFields;
    
    /**
     * 顶点标签
     */
    private String vertexLabel;
    
    /**
     * 版本号（用于乐观锁）
     */
    private Long version;
    
    /**
     * 状态（如：ACTIVE, DELETED, ARCHIVED）
     */
    private String status;
    
    /**
     * 来源信息
     */
    private String source;
    
    /**
     * 语言代码
     */
    private String language;
    
    /**
     * 文档分类
     */
    private String category;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 外部引用ID
     */
    private String externalId;
    
    /**
     * 数据分区键
     */
    private String partitionKey;
    
    /**
     * 检查是否为有效的向量
     */
    public boolean isValidVector() {
        return vector != null && !vector.isEmpty() && vectorDimension != null && vectorDimension > 0;
    }
    
    /**
     * 检查向量维度是否匹配
     */
    public boolean isVectorDimensionMatch() {
        return vector != null && vectorDimension != null && vector.size() == vectorDimension;
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
     * 获取元数据
     */
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
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
     * 获取自定义字段
     */
    public Object getCustomField(String key) {
        return customFields != null ? customFields.get(key) : null;
    }
    
    /**
     * 设置更新时间为当前时间
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置创建时间为当前时间
     */
    public void setCreationTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    /**
     * 创建向量的副本
     */
    public HugeGraphVector copy() {
        return HugeGraphVector.builder()
                .id(this.id)
                .vertexId(this.vertexId)
                .content(this.content)
                .vector(this.vector != null ? List.copyOf(this.vector) : null)
                .vectorDimension(this.vectorDimension)
                .title(this.title)
                .uniqueId(this.uniqueId)
                .docIndex(this.docIndex)
                .metadata(this.metadata != null ? new HashMap<>(this.metadata) : null)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .score(this.score)
                .distance(this.distance)
                .docType(this.docType)
                .tags(this.tags != null ? List.copyOf(this.tags) : null)
                .customFields(this.customFields != null ? new HashMap<>(this.customFields) : null)
                .vertexLabel(this.vertexLabel)
                .version(this.version)
                .status(this.status)
                .source(this.source)
                .language(this.language)
                .category(this.category)
                .priority(this.priority)
                .externalId(this.externalId)
                .partitionKey(this.partitionKey)
                .build();
    }
    
    /**
     * 将向量转换为Map表示（用于存储到图数据库）
     */
    public Map<String, Object> toVertexProperties() {
        Map<String, Object> properties = new HashMap<>();
        
        if (id != null) properties.put("id", id);
        if (content != null) properties.put("content", content);
        if (vector != null) properties.put("vector", vector);
        if (vectorDimension != null) properties.put("vectorDimension", vectorDimension);
        if (title != null) properties.put("title", title);
        if (uniqueId != null) properties.put("uniqueId", uniqueId);
        if (docIndex != null) properties.put("docIndex", docIndex);
        if (metadata != null) properties.put("metadata", metadata);
        if (createdAt != null) properties.put("createdAt", createdAt.toString());
        if (updatedAt != null) properties.put("updatedAt", updatedAt.toString());
        if (docType != null) properties.put("docType", docType);
        if (tags != null) properties.put("tags", tags);
        if (customFields != null) properties.put("customFields", customFields);
        if (version != null) properties.put("version", version);
        if (status != null) properties.put("status", status);
        if (source != null) properties.put("source", source);
        if (language != null) properties.put("language", language);
        if (category != null) properties.put("category", category);
        if (priority != null) properties.put("priority", priority);
        if (externalId != null) properties.put("externalId", externalId);
        if (partitionKey != null) properties.put("partitionKey", partitionKey);
        
        return properties;
    }
    
    @Override
    public String toString() {
        return String.format("HugeGraphVector{id='%s', content='%s', vectorDimension=%d, score=%s, distance=%s}", 
                           id, 
                           content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content,
                           vectorDimension, 
                           score, 
                           distance);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HugeGraphVector that = (HugeGraphVector) obj;
        return java.util.Objects.equals(id, that.id) && 
               java.util.Objects.equals(uniqueId, that.uniqueId);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, uniqueId);
    }
}
