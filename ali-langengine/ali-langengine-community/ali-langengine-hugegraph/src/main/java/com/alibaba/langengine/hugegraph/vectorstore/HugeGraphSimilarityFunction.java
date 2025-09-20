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
package com.alibaba.langengine.hugegraph.vectorstore;

import java.util.List;


public enum HugeGraphSimilarityFunction {
    
    /**
     * 余弦相似度
     * 计算两个向量夹角的余弦值，范围 [-1, 1]，值越大越相似
     */
    COSINE("cosine", "Cosine Similarity") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            validateVectors(vector1, vector2);
            
            double dotProduct = 0.0;
            double norm1 = 0.0;
            double norm2 = 0.0;
            
            for (int i = 0; i < vector1.size(); i++) {
                double v1 = vector1.get(i);
                double v2 = vector2.get(i);
                
                dotProduct += v1 * v2;
                norm1 += v1 * v1;
                norm2 += v2 * v2;
            }
            
            if (norm1 == 0.0 || norm2 == 0.0) {
                return 0.0; // 零向量的相似度为0
            }
            
            return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        }
        
        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            double similarity = calculateSimilarity(vector1, vector2);
            return 1.0 - similarity; // 距离 = 1 - 相似度
        }
    },
    
    /**
     * 欧氏距离
     * 计算两个向量在欧几里得空间中的直线距离，值越小越相似
     */
    EUCLIDEAN("euclidean", "Euclidean Distance") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            double distance = calculateDistance(vector1, vector2);
            return 1.0 / (1.0 + distance); // 相似度 = 1 / (1 + 距离)
        }
        
        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            validateVectors(vector1, vector2);
            
            double sum = 0.0;
            for (int i = 0; i < vector1.size(); i++) {
                double diff = vector1.get(i) - vector2.get(i);
                sum += diff * diff;
            }
            
            return Math.sqrt(sum);
        }
    },
    
    /**
     * 曼哈顿距离（L1距离）
     * 计算两个向量各维度差值的绝对值之和，值越小越相似
     */
    MANHATTAN("manhattan", "Manhattan Distance") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            double distance = calculateDistance(vector1, vector2);
            return 1.0 / (1.0 + distance); // 相似度 = 1 / (1 + 距离)
        }
        
        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            validateVectors(vector1, vector2);
            
            double sum = 0.0;
            for (int i = 0; i < vector1.size(); i++) {
                sum += Math.abs(vector1.get(i) - vector2.get(i));
            }
            
            return sum;
        }
    },
    
    /**
     * 点积相似度
     * 计算两个向量的点积，值越大越相似
     */
    DOT_PRODUCT("dot_product", "Dot Product") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            validateVectors(vector1, vector2);
            
            double dotProduct = 0.0;
            for (int i = 0; i < vector1.size(); i++) {
                dotProduct += vector1.get(i) * vector2.get(i);
            }
            
            return dotProduct;
        }
        
        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            double similarity = calculateSimilarity(vector1, vector2);
            return -similarity; // 距离 = -相似度
        }
    },
    
    /**
     * 汉明距离
     * 计算两个向量中不同元素的个数（适用于二进制向量）
     */
    HAMMING("hamming", "Hamming Distance") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            double distance = calculateDistance(vector1, vector2);
            return 1.0 - (distance / vector1.size()); // 相似度 = 1 - (距离 / 维度)
        }
        
        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            validateVectors(vector1, vector2);
            
            int diffCount = 0;
            for (int i = 0; i < vector1.size(); i++) {
                if (!vector1.get(i).equals(vector2.get(i))) {
                    diffCount++;
                }
            }
            
            return diffCount;
        }
    },
    
    /**
     * 切比雪夫距离（L∞距离）
     * 计算两个向量各维度差值绝对值的最大值
     */
    CHEBYSHEV("chebyshev", "Chebyshev Distance") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            double distance = calculateDistance(vector1, vector2);
            return 1.0 / (1.0 + distance); // 相似度 = 1 / (1 + 距离)
        }
        
        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            validateVectors(vector1, vector2);
            
            double maxDiff = 0.0;
            for (int i = 0; i < vector1.size(); i++) {
                double diff = Math.abs(vector1.get(i) - vector2.get(i));
                maxDiff = Math.max(maxDiff, diff);
            }
            
            return maxDiff;
        }
    },
    
    /**
     * 杰卡德相似度
     * 计算两个集合的交集与并集的比值（适用于稀疏向量）
     */
    JACCARD("jaccard", "Jaccard Similarity") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            validateVectors(vector1, vector2);
            
            double intersection = 0.0;
            double union = 0.0;
            
            for (int i = 0; i < vector1.size(); i++) {
                double v1 = vector1.get(i);
                double v2 = vector2.get(i);
                
                if (v1 != 0.0 || v2 != 0.0) { // 至少一个非零
                    union++;
                    if (v1 != 0.0 && v2 != 0.0) { // 都非零
                        intersection++;
                    }
                }
            }
            
            return union == 0.0 ? 0.0 : intersection / union;
        }
        
        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            double similarity = calculateSimilarity(vector1, vector2);
            return 1.0 - similarity; // 杰卡德距离 = 1 - 杰卡德相似度
        }
    },
    
    /**
     * 皮尔逊相关系数
     * 计算两个向量的线性相关性，范围 [-1, 1]
     */
    PEARSON("pearson", "Pearson Correlation") {
        @Override
        public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
            validateVectors(vector1, vector2);
            
            int n = vector1.size();
            
            // 计算均值
            double mean1 = vector1.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double mean2 = vector2.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            
            // 计算协方差和标准差
            double covariance = 0.0;
            double variance1 = 0.0;
            double variance2 = 0.0;
            
            for (int i = 0; i < n; i++) {
                double diff1 = vector1.get(i) - mean1;
                double diff2 = vector2.get(i) - mean2;
                
                covariance += diff1 * diff2;
                variance1 += diff1 * diff1;
                variance2 += diff2 * diff2;
            }
            
            if (variance1 == 0.0 || variance2 == 0.0) {
                return 0.0; // 如果某个向量方差为0，相关系数为0
            }
            
            return covariance / Math.sqrt(variance1 * variance2);
        }
        
        @Override
        public double calculateDistance(List<Double> vector1, List<Double> vector2) {
            double similarity = calculateSimilarity(vector1, vector2);
            return 1.0 - Math.abs(similarity); // 距离 = 1 - |相关系数|
        }
    };
    
    private final String name;
    private final String description;
    
    HugeGraphSimilarityFunction(String name, String description) {
        this.name = name;
        this.description = description;
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
     * 获取函数名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取函数描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 验证向量有效性
     */
    protected static void validateVectors(List<Double> vector1, List<Double> vector2) {
        if (vector1 == null || vector2 == null) {
            throw new IllegalArgumentException("Vectors cannot be null");
        }
        
        if (vector1.isEmpty() || vector2.isEmpty()) {
            throw new IllegalArgumentException("Vectors cannot be empty");
        }
        
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException(
                String.format("Vector dimensions must match: %d vs %d", 
                            vector1.size(), vector2.size())
            );
        }
    }
    
    /**
     * 根据名称获取相似度函数
     */
    public static HugeGraphSimilarityFunction fromName(String name) {
        if (name == null) {
            return COSINE; // 默认使用余弦相似度
        }
        
        for (HugeGraphSimilarityFunction func : values()) {
            if (func.getName().equalsIgnoreCase(name)) {
                return func;
            }
        }
        
        throw new IllegalArgumentException("Unknown similarity function: " + name);
    }
    
    /**
     * 检查是否为距离度量（值越小越相似）
     */
    public boolean isDistanceMetric() {
        return this == EUCLIDEAN || this == MANHATTAN || this == HAMMING || 
               this == CHEBYSHEV || this == JACCARD;
    }
    
    /**
     * 检查是否为相似度度量（值越大越相似）
     */
    public boolean isSimilarityMetric() {
        return !isDistanceMetric();
    }
    
    /**
     * 获取所有可用的相似度函数名称
     */
    public static String[] getAllFunctionNames() {
        HugeGraphSimilarityFunction[] functions = values();
        String[] names = new String[functions.length];
        for (int i = 0; i < functions.length; i++) {
            names[i] = functions[i].getName();
        }
        return names;
    }
    
    /**
     * 批量计算相似度（优化版本）
     */
    public double[] calculateBatchSimilarity(List<Double> queryVector, List<List<Double>> candidates) {
        if (queryVector == null || candidates == null || candidates.isEmpty()) {
            return new double[0];
        }
        
        double[] similarities = new double[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            try {
                similarities[i] = calculateSimilarity(queryVector, candidates.get(i));
            } catch (Exception e) {
                similarities[i] = Double.NaN; // 标记计算失败的情况
            }
        }
        
        return similarities;
    }
    
    /**
     * 批量计算距离（优化版本）
     */
    public double[] calculateBatchDistance(List<Double> queryVector, List<List<Double>> candidates) {
        if (queryVector == null || candidates == null || candidates.isEmpty()) {
            return new double[0];
        }
        
        double[] distances = new double[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            try {
                distances[i] = calculateDistance(queryVector, candidates.get(i));
            } catch (Exception e) {
                distances[i] = Double.NaN; // 标记计算失败的情况
            }
        }
        
        return distances;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", description, name);
    }
}
