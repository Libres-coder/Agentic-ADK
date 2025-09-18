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
package com.alibaba.langengine.nebulagraph.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NebulaGraphVector {
    
    /**
     * 文档唯一ID
     */
    private String uniqueId;
    
    /**
     * 文档内容
     */
    private String content;
    
    /**
     * 向量数据
     */
    private List<Double> vector;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 文档索引
     */
    private String docIndex;
    
    /**
     * 文档类型
     */
    private String docType;
    
    /**
     * 标签
     */
    private List<String> tags;
    
    /**
     * 自定义字段
     */
    private Map<String, Object> customFields;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
    
    /**
     * 验证向量数据
     */
    public void validate() {
        if (uniqueId == null || uniqueId.trim().isEmpty()) {
            throw new IllegalArgumentException("Unique ID cannot be null or empty");
        }
        
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        
        if (vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("Vector cannot be null or empty");
        }
        
        // 检查向量维度一致性
        for (Double value : vector) {
            if (value == null || !Double.isFinite(value)) {
                throw new IllegalArgumentException("Vector values must be finite numbers");
            }
        }
    }
    
    /**
     * 获取向量维度
     */
    public int getDimension() {
        return vector != null ? vector.size() : 0;
    }
    
    /**
     * 检查向量维度是否匹配
     */
    public boolean isDimensionMatch(int expectedDimension) {
        return getDimension() == expectedDimension;
    }
    
    /**
     * 计算向量的L2范数
     */
    public double calculateL2Norm() {
        if (vector == null || vector.isEmpty()) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (Double value : vector) {
            if (value != null) {
                sum += value * value;
            }
        }
        
        return Math.sqrt(sum);
    }
    
    /**
     * 向量归一化
     */
    public void normalize() {
        if (vector == null || vector.isEmpty()) {
            return;
        }
        
        double norm = calculateL2Norm();
        if (norm > 0.0) {
            for (int i = 0; i < vector.size(); i++) {
                Double value = vector.get(i);
                if (value != null) {
                    vector.set(i, value / norm);
                }
            }
        }
    }
    
    /**
     * 计算与另一个向量的余弦相似度
     */
    public double calculateCosineSimilarity(NebulaGraphVector other) {
        if (other == null || other.getVector() == null) {
            return 0.0;
        }
        
        List<Double> otherVector = other.getVector();
        if (vector.size() != otherVector.size()) {
            throw new IllegalArgumentException("Vector dimensions do not match");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector.size(); i++) {
            Double v1 = vector.get(i);
            Double v2 = otherVector.get(i);
            
            if (v1 != null && v2 != null) {
                dotProduct += v1 * v2;
                norm1 += v1 * v1;
                norm2 += v2 * v2;
            }
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 计算与另一个向量的欧氏距离
     */
    public double calculateEuclideanDistance(NebulaGraphVector other) {
        if (other == null || other.getVector() == null) {
            return Double.MAX_VALUE;
        }
        
        List<Double> otherVector = other.getVector();
        if (vector.size() != otherVector.size()) {
            throw new IllegalArgumentException("Vector dimensions do not match");
        }
        
        double sum = 0.0;
        for (int i = 0; i < vector.size(); i++) {
            Double v1 = vector.get(i);
            Double v2 = otherVector.get(i);
            
            if (v1 != null && v2 != null) {
                double diff = v1 - v2;
                sum += diff * diff;
            }
        }
        
        return Math.sqrt(sum);
    }
    
    /**
     * 复制向量数据
     */
    public NebulaGraphVector copy() {
        NebulaGraphVector copy = new NebulaGraphVector();
        copy.setUniqueId(this.uniqueId);
        copy.setContent(this.content);
        copy.setVector(this.vector != null ? List.copyOf(this.vector) : null);
        copy.setMetadata(this.metadata != null ? Map.copyOf(this.metadata) : null);
        copy.setTitle(this.title);
        copy.setDocIndex(this.docIndex);
        copy.setDocType(this.docType);
        copy.setTags(this.tags != null ? List.copyOf(this.tags) : null);
        copy.setCustomFields(this.customFields != null ? Map.copyOf(this.customFields) : null);
        copy.setCreatedAt(this.createdAt);
        copy.setUpdatedAt(this.updatedAt);
        return copy;
    }
}
