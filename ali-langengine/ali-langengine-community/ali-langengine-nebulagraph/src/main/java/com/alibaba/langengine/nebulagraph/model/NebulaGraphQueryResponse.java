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
public class NebulaGraphQueryResponse {
    
    /**
     * 查询结果文档列表
     */
    private List<DocumentResult> documents;
    
    /**
     * 查询执行时间（毫秒）
     */
    private long executionTime;
    
    /**
     * 是否成功
     */
    private boolean success = true;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 文档结果
     */
    @Data
    public static class DocumentResult {
        
        /**
         * 文档唯一ID
         */
        private String uniqueId;
        
        /**
         * 文档内容
         */
        private String content;
        
        /**
         * 相似度分数
         */
        private Double score;
        
        /**
         * 距离值
         */
        private Double distance;
        
        /**
         * 文档向量
         */
        private List<Double> vector;
        
        /**
         * 元数据
         */
        private Map<String, Object> metadata;
        
        /**
         * 验证文档结果
         */
        public void validate() {
            if (uniqueId == null || uniqueId.trim().isEmpty()) {
                throw new IllegalArgumentException("Unique ID cannot be null or empty");
            }
            
            if (content == null) {
                throw new IllegalArgumentException("Content cannot be null");
            }
        }
        
        /**
         * 设置相似度分数（基于距离计算）
         */
        public void calculateScore() {
            if (distance != null) {
                // 对于余弦距离，相似度 = 1 - 距离
                // 对于欧氏距离，需要转换为相似度分数
                this.score = Math.max(0.0, 1.0 - distance);
            }
        }
    }
    
    /**
     * 验证查询响应
     */
    public void validate() {
        if (!success && (errorMessage == null || errorMessage.trim().isEmpty())) {
            throw new IllegalArgumentException("Error message is required when success is false");
        }
        
        if (success && documents != null) {
            for (DocumentResult document : documents) {
                document.validate();
            }
        }
    }
    
    /**
     * 获取文档数量
     */
    public int getDocumentCount() {
        return documents != null ? documents.size() : 0;
    }
    
    /**
     * 是否有结果
     */
    public boolean hasResults() {
        return documents != null && !documents.isEmpty();
    }
    
    /**
     * 获取平均相似度分数
     */
    public Double getAverageScore() {
        if (documents == null || documents.isEmpty()) {
            return null;
        }
        
        double sum = 0.0;
        int count = 0;
        
        for (DocumentResult document : documents) {
            if (document.getScore() != null) {
                sum += document.getScore();
                count++;
            }
        }
        
        return count > 0 ? sum / count : null;
    }
    
    /**
     * 获取最高相似度分数
     */
    public Double getMaxScore() {
        if (documents == null || documents.isEmpty()) {
            return null;
        }
        
        Double maxScore = null;
        for (DocumentResult document : documents) {
            if (document.getScore() != null) {
                if (maxScore == null || document.getScore() > maxScore) {
                    maxScore = document.getScore();
                }
            }
        }
        
        return maxScore;
    }
    
    /**
     * 获取最低相似度分数
     */
    public Double getMinScore() {
        if (documents == null || documents.isEmpty()) {
            return null;
        }
        
        Double minScore = null;
        for (DocumentResult document : documents) {
            if (document.getScore() != null) {
                if (minScore == null || document.getScore() < minScore) {
                    minScore = document.getScore();
                }
            }
        }
        
        return minScore;
    }
}
