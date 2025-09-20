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
package com.alibaba.langengine.faiss.model;

import lombok.Data;

/**
 * FAISS 搜索结果模型
 * 
 * @author langengine
 */
@Data
public class FaissSearchResult {
    
    /**
     * 向量在索引中的ID
     */
    private int index;
    
    /**
     * 文档ID
     */
    private String documentId;
    
    /**
     * 距离值
     */
    private float distance;
    
    /**
     * 相似性分数
     */
    private float score;
    
    /**
     * 向量数据（可选）
     */
    private float[] vector;
    
    /**
     * 元数据（可选）
     */
    private Object metadata;
    
    public FaissSearchResult() {
    }
    
    public FaissSearchResult(int index, float distance, float score) {
        this.index = index;
        this.distance = distance;
        this.score = score;
    }
    
    public FaissSearchResult(int index, String documentId, float distance, float score) {
        this.index = index;
        this.documentId = documentId;
        this.distance = distance;
        this.score = score;
    }
    
    /**
     * 获取相似性百分比
     */
    public float getSimilarityPercentage() {
        return score * 100.0f;
    }
    
    /**
     * 检查是否超过相似性阈值
     */
    public boolean isSimilar(float threshold) {
        return score >= threshold;
    }
    
    /**
     * 检查是否超过距离阈值
     */
    public boolean isWithinDistance(float maxDistance) {
        return distance <= maxDistance;
    }
    
    @Override
    public String toString() {
        return String.format("FaissSearchResult{index=%d, documentId='%s', distance=%.4f, score=%.4f}", 
            index, documentId, distance, score);
    }
}
