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
package com.alibaba.langengine.core.util;

import java.util.List;


public class VectorUtils {

    /**
     * 计算两个向量的余弦相似度
     *
     * @param vector1 第一个向量
     * @param vector2 第二个向量
     * @return 余弦相似度值 (0.0 到 1.0)
     * @throws IllegalArgumentException 如果向量为空或长度不匹配
     */
    public static double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            throw new IllegalArgumentException("Invalid vectors for similarity calculation");
        }
        
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Vectors must have the same size");
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vector1.size(); i++) {
            double a = vector1.get(i);
            double b = vector2.get(i);
            
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }
        
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 计算两个向量的余弦相似度 (数组版本)
     *
     * @param vector1 第一个向量
     * @param vector2 第二个向量
     * @return 余弦相似度值 (0.0 到 1.0)
     * @throws IllegalArgumentException 如果向量为空或长度不匹配
     */
    public static double calculateCosineSimilarity(double[] vector1, double[] vector2) {
        if (vector1 == null || vector2 == null || vector1.length == 0 || vector2.length == 0) {
            throw new IllegalArgumentException("Invalid vectors for similarity calculation");
        }
        
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must have the same size");
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vector1.length; i++) {
            double a = vector1[i];
            double b = vector2[i];
            
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }
        
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 计算向量的欧几里得距离
     *
     * @param vector1 第一个向量
     * @param vector2 第二个向量
     * @return 欧几里得距离
     * @throws IllegalArgumentException 如果向量为空或长度不匹配
     */
    public static double calculateEuclideanDistance(List<Double> vector1, List<Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            throw new IllegalArgumentException("Invalid vectors for distance calculation");
        }
        
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Vectors must have the same size");
        }
        
        double sum = 0.0;
        for (int i = 0; i < vector1.size(); i++) {
            double diff = vector1.get(i) - vector2.get(i);
            sum += diff * diff;
        }
        
        return Math.sqrt(sum);
    }
}
