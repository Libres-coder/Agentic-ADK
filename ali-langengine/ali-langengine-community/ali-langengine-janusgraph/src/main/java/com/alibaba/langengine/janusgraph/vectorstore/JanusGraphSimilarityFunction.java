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
package com.alibaba.langengine.janusgraph.vectorstore;

import java.util.List;
import java.util.stream.Collectors;


public enum JanusGraphSimilarityFunction {

    /**
     * 余弦相似度 - 计算两个向量之间的余弦值
     * 值域：[-1, 1]，1表示完全相似，-1表示完全相反，0表示正交
     */
    COSINE("cosine") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            if (vector1.size() != vector2.size()) {
                throw new IllegalArgumentException("Vector dimensions must match");
            }
            
            double dotProduct = 0.0;
            double magnitude1 = 0.0;
            double magnitude2 = 0.0;
            
            for (int i = 0; i < vector1.size(); i++) {
                dotProduct += vector1.get(i) * vector2.get(i);
                magnitude1 += vector1.get(i) * vector1.get(i);
                magnitude2 += vector2.get(i) * vector2.get(i);
            }
            
            magnitude1 = Math.sqrt(magnitude1);
            magnitude2 = Math.sqrt(magnitude2);
            
            if (magnitude1 == 0.0 || magnitude2 == 0.0) {
                return 0.0;
            }
            
            return dotProduct / (magnitude1 * magnitude2);
        }

        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            // 余弦距离 = 1 - 余弦相似度
            return 1.0 - calculateSimilarity(vector1, vector2);
        }
    },

    /**
     * 欧几里得距离 - 计算两个向量之间的欧几里得距离
     * 值域：[0, +∞)，0表示完全相同，值越大表示越不相似
     */
    EUCLIDEAN("euclidean") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            double distance = calculateDistance(vector1, vector2);
            // 转换距离为相似度：1 / (1 + distance)
            return 1.0 / (1.0 + distance);
        }

        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            if (vector1.size() != vector2.size()) {
                throw new IllegalArgumentException("Vector dimensions must match");
            }
            
            double sum = 0.0;
            for (int i = 0; i < vector1.size(); i++) {
                double diff = vector1.get(i) - vector2.get(i);
                sum += diff * diff;
            }
            
            return Math.sqrt(sum);
        }
    },

    /**
     * 曼哈顿距离 - 计算两个向量之间的曼哈顿距离（L1距离）
     * 值域：[0, +∞)，0表示完全相同，值越大表示越不相似
     */
    MANHATTAN("manhattan") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            double distance = calculateDistance(vector1, vector2);
            // 转换距离为相似度：1 / (1 + distance)
            return 1.0 / (1.0 + distance);
        }

        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            if (vector1.size() != vector2.size()) {
                throw new IllegalArgumentException("Vector dimensions must match");
            }
            
            double sum = 0.0;
            for (int i = 0; i < vector1.size(); i++) {
                sum += Math.abs(vector1.get(i) - vector2.get(i));
            }
            
            return sum;
        }
    },

    /**
     * 点积相似度 - 计算两个向量的点积
     * 值域：(-∞, +∞)，值越大表示越相似
     */
    DOT_PRODUCT("dot_product") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            if (vector1.size() != vector2.size()) {
                throw new IllegalArgumentException("Vector dimensions must match");
            }
            
            double dotProduct = 0.0;
            for (int i = 0; i < vector1.size(); i++) {
                dotProduct += vector1.get(i) * vector2.get(i);
            }
            
            return dotProduct;
        }

        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            // 点积距离 = -点积相似度
            return -calculateSimilarity(vector1, vector2);
        }
    },

    /**
     * Jaccard相似度 - 适用于二进制向量或稀疏向量
     * 值域：[0, 1]，1表示完全相同，0表示完全不同
     */
    JACCARD("jaccard") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            if (vector1.size() != vector2.size()) {
                throw new IllegalArgumentException("Vector dimensions must match");
            }
            
            int intersection = 0;
            int union = 0;
            
            for (int i = 0; i < vector1.size(); i++) {
                boolean v1NonZero = vector1.get(i) != 0.0;
                boolean v2NonZero = vector2.get(i) != 0.0;
                
                if (v1NonZero && v2NonZero) {
                    intersection++;
                }
                if (v1NonZero || v2NonZero) {
                    union++;
                }
            }
            
            return union == 0 ? 1.0 : (double) intersection / union;
        }

        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            // Jaccard距离 = 1 - Jaccard相似度
            return 1.0 - calculateSimilarity(vector1, vector2);
        }
    };

    private final String name;

    JanusGraphSimilarityFunction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * 计算向量相似度
     * 
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 相似度值
     */
    public abstract double calculateSimilarity(List<Double> vector1, List<Double> vector2);

    /**
     * 计算向量距离
     * 
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 距离值
     */
    public abstract double calculateDistance(List<Double> vector1, List<Double> vector2);

    /**
     * 根据名称获取相似度函数
     * 
     * @param name 函数名称
     * @return 相似度函数
     */
    public static JanusGraphSimilarityFunction fromName(String name) {
        for (JanusGraphSimilarityFunction function : values()) {
            if (function.getName().equalsIgnoreCase(name)) {
                return function;
            }
        }
        throw new IllegalArgumentException("Unknown similarity function: " + name);
    }

    /**
     * 验证向量维度是否一致
     * 
     * @param vector1 向量1
     * @param vector2 向量2
     * @throws IllegalArgumentException 如果向量维度不一致
     */
    protected void validateVectorDimensions(List<Double> vector1, List<Double> vector2) {
        if (vector1 == null || vector2 == null) {
            throw new IllegalArgumentException("Vectors cannot be null");
        }
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException(
                String.format("Vector dimensions must match: %d vs %d", 
                    vector1.size(), vector2.size()));
        }
    }

    /**
     * 归一化向量
     * 
     * @param vector 原始向量
     * @return 归一化后的向量
     */
    public static List<Double> normalizeVector(List<Double> vector) {
        double magnitudeSquared = 0.0;
        for (Double value : vector) {
            magnitudeSquared += value * value;
        }
        final double magnitude = Math.sqrt(magnitudeSquared);
        
        if (magnitude == 0.0) {
            return vector; // 零向量无法归一化
        }
        
        return vector.stream()
                .map(value -> value / magnitude)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return name;
    }
}
