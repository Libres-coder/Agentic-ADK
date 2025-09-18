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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HugeGraphSimilarityFunctionTest {

    private static final List<Double> VECTOR_A = Arrays.asList(1.0, 2.0, 3.0, 4.0);
    private static final List<Double> VECTOR_B = Arrays.asList(2.0, 3.0, 4.0, 5.0);
    private static final List<Double> IDENTICAL_VECTOR = Arrays.asList(1.0, 2.0, 3.0, 4.0);
    private static final List<Double> ZERO_VECTOR = Arrays.asList(0.0, 0.0, 0.0, 0.0);

    @Test
    public void testCosineSimilarity() {
        HugeGraphSimilarityFunction function = HugeGraphSimilarityFunction.COSINE;
        
        // 测试相同向量的余弦相似度应该接近1.0
        double similarity = function.calculateSimilarity(VECTOR_A, IDENTICAL_VECTOR);
        assertEquals(1.0, similarity, 0.0001);
        
        // 测试距离应该接近0.0
        double distance = function.calculateDistance(VECTOR_A, IDENTICAL_VECTOR);
        assertEquals(0.0, distance, 0.0001);
        
        // 测试不同向量的余弦相似度
        double similarity2 = function.calculateSimilarity(VECTOR_A, VECTOR_B);
        assertTrue(similarity2 > 0.9, "Cosine similarity should be high for similar vectors");
        
        // 测试与零向量的相似度
        double zeroSimilarity = function.calculateSimilarity(VECTOR_A, ZERO_VECTOR);
        assertEquals(0.0, zeroSimilarity, 0.0001);
    }

    @Test
    public void testEuclideanDistance() {
        HugeGraphSimilarityFunction function = HugeGraphSimilarityFunction.EUCLIDEAN;
        
        // 测试相同向量的欧几里得距离应该为0
        double distance = function.calculateDistance(VECTOR_A, IDENTICAL_VECTOR);
        assertEquals(0.0, distance, 0.0001);
        
        // 测试相似度应该接近1.0
        double similarity = function.calculateSimilarity(VECTOR_A, IDENTICAL_VECTOR);
        assertEquals(1.0, similarity, 0.0001);
        
        // 测试不同向量的距离
        double distance2 = function.calculateDistance(VECTOR_A, VECTOR_B);
        assertEquals(2.0, distance2, 0.0001); // sqrt((1-2)^2 + (2-3)^2 + (3-4)^2 + (4-5)^2) = sqrt(4) = 2
    }

    @Test
    public void testManhattanDistance() {
        HugeGraphSimilarityFunction function = HugeGraphSimilarityFunction.MANHATTAN;
        
        // 测试相同向量的曼哈顿距离应该为0
        double distance = function.calculateDistance(VECTOR_A, IDENTICAL_VECTOR);
        assertEquals(0.0, distance, 0.0001);
        
        // 测试不同向量的曼哈顿距离
        double distance2 = function.calculateDistance(VECTOR_A, VECTOR_B);
        assertEquals(4.0, distance2, 0.0001); // |1-2| + |2-3| + |3-4| + |4-5| = 1+1+1+1 = 4
    }

    @Test
    public void testDotProductSimilarity() {
        HugeGraphSimilarityFunction function = HugeGraphSimilarityFunction.DOT_PRODUCT;
        
        // 测试点积相似度
        double similarity = function.calculateSimilarity(VECTOR_A, VECTOR_B);
        assertEquals(40.0, similarity, 0.0001); // 1*2 + 2*3 + 3*4 + 4*5 = 2+6+12+20 = 40
        
        // 测试与零向量的点积
        double zeroSimilarity = function.calculateSimilarity(VECTOR_A, ZERO_VECTOR);
        assertEquals(0.0, zeroSimilarity, 0.0001);
    }

    @Test
    public void testHammingDistance() {
        HugeGraphSimilarityFunction function = HugeGraphSimilarityFunction.HAMMING;
        
        // 测试相同向量的汉明距离应该为0
        double distance = function.calculateDistance(VECTOR_A, IDENTICAL_VECTOR);
        assertEquals(0.0, distance, 0.0001);
        
        // 测试不同向量的汉明距离
        List<Double> vectorD = Arrays.asList(2.0, 1.0, 6.0, 1.0);
        double distance2 = function.calculateDistance(VECTOR_A, vectorD);
        
        // VECTOR_A = [1.0, 2.0, 3.0, 4.0], vectorD = [2.0, 1.0, 6.0, 1.0]
        // 所有4个维度都不相等，所以汉明距离为4
        assertEquals(4.0, distance2, 0.0001);
    }

    @Test
    public void testChebyshevDistance() {
        HugeGraphSimilarityFunction function = HugeGraphSimilarityFunction.CHEBYSHEV;
        
        // 测试切比雪夫距离（最大维度差值）
        double distance = function.calculateDistance(VECTOR_A, VECTOR_B);
        assertEquals(1.0, distance, 0.0001); // max(|1-2|, |2-3|, |3-4|, |4-5|) = 1
    }

    @Test
    public void testJaccardSimilarity() {
        HugeGraphSimilarityFunction function = HugeGraphSimilarityFunction.JACCARD;
        
        // 对于连续向量，Jaccard相似度使用阈值方法
        double similarity = function.calculateSimilarity(VECTOR_A, VECTOR_B);
        assertTrue(similarity >= 0.0 && similarity <= 1.0, "Jaccard similarity should be between 0 and 1");
    }

    @Test
    public void testPearsonCorrelation() {
        HugeGraphSimilarityFunction function = HugeGraphSimilarityFunction.PEARSON;
        
        // 测试完全正相关的向量
        List<Double> positiveCorrelated = Arrays.asList(2.0, 4.0, 6.0, 8.0); // VECTOR_A * 2
        double correlation = function.calculateSimilarity(VECTOR_A, positiveCorrelated);
        assertEquals(1.0, correlation, 0.0001);
        
        // 测试负相关的向量
        List<Double> negativeCorrelated = Arrays.asList(-1.0, -2.0, -3.0, -4.0);
        double negativeCorr = function.calculateSimilarity(VECTOR_A, negativeCorrelated);
        assertEquals(-1.0, negativeCorr, 0.0001);
    }

    @ParameterizedTest
    @EnumSource(HugeGraphSimilarityFunction.class)
    public void testAllFunctionsWithValidInputs(HugeGraphSimilarityFunction function) {
        // 确保所有函数都能处理有效输入而不抛出异常
        assertDoesNotThrow(() -> {
            double similarity = function.calculateSimilarity(VECTOR_A, VECTOR_B);
            double distance = function.calculateDistance(VECTOR_A, VECTOR_B);
            
            // 基本范围检查
            assertTrue(Double.isFinite(similarity), "Similarity should be finite for " + function);
            assertTrue(Double.isFinite(distance), "Distance should be finite for " + function);
            
            // 对于大多数函数，距离应该非负（除了点积）
            if (function != HugeGraphSimilarityFunction.DOT_PRODUCT) {
                assertTrue(distance >= 0, "Distance should be non-negative for " + function + " (got " + distance + ")");
            }
        });
    }

    @Test
    public void testFromName() {
        // 测试通过名称获取函数
        assertEquals(HugeGraphSimilarityFunction.COSINE, HugeGraphSimilarityFunction.fromName("COSINE"));
        assertEquals(HugeGraphSimilarityFunction.EUCLIDEAN, HugeGraphSimilarityFunction.fromName("EUCLIDEAN"));
        
        // 测试忽略大小写
        assertEquals(HugeGraphSimilarityFunction.MANHATTAN, HugeGraphSimilarityFunction.fromName("manhattan"));
        assertEquals(HugeGraphSimilarityFunction.DOT_PRODUCT, HugeGraphSimilarityFunction.fromName("dot_product"));
        
        // 测试默认值
        assertEquals(HugeGraphSimilarityFunction.COSINE, HugeGraphSimilarityFunction.fromName(null));
        
        // 测试未知函数名应该抛出异常
        assertThrows(IllegalArgumentException.class, 
                () -> HugeGraphSimilarityFunction.fromName("UNKNOWN"));
    }

    @Test
    public void testValidateVectors() {
        // 测试有效向量
        assertDoesNotThrow(() -> {
            HugeGraphSimilarityFunction.validateVectors(VECTOR_A, VECTOR_B);
        });
        
        // 测试null向量
        assertThrows(IllegalArgumentException.class, () -> {
            HugeGraphSimilarityFunction.validateVectors(null, VECTOR_B);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            HugeGraphSimilarityFunction.validateVectors(VECTOR_A, null);
        });
        
        // 测试空向量
        assertThrows(IllegalArgumentException.class, () -> {
            HugeGraphSimilarityFunction.validateVectors(Arrays.asList(), VECTOR_B);
        });
        
        // 测试维度不匹配的向量
        List<Double> shortVector = Arrays.asList(1.0, 2.0);
        assertThrows(IllegalArgumentException.class, () -> {
            HugeGraphSimilarityFunction.validateVectors(VECTOR_A, shortVector);
        });
    }

    @Test
    public void testEdgeCases() {
        // 测试单维向量
        List<Double> singleDim1 = Arrays.asList(1.0);
        List<Double> singleDim2 = Arrays.asList(2.0);
        
        for (HugeGraphSimilarityFunction function : HugeGraphSimilarityFunction.values()) {
            assertDoesNotThrow(() -> {
                function.calculateSimilarity(singleDim1, singleDim2);
                function.calculateDistance(singleDim1, singleDim2);
            }, "Function " + function + " should handle single-dimension vectors");
        }
        
        // 测试包含NaN或无穷大的向量（应该被验证器捕获）
        List<Double> invalidVector = Arrays.asList(1.0, Double.NaN, 3.0, 4.0);
        
        for (HugeGraphSimilarityFunction function : HugeGraphSimilarityFunction.values()) {
            // 大多数函数应该能处理或抛出合理的异常
            try {
                function.calculateSimilarity(VECTOR_A, invalidVector);
            } catch (Exception e) {
                // 预期可能会有异常，这是合理的
                assertTrue(e instanceof IllegalArgumentException || e instanceof ArithmeticException,
                        "Should throw appropriate exception for invalid input");
            }
        }
    }
}
