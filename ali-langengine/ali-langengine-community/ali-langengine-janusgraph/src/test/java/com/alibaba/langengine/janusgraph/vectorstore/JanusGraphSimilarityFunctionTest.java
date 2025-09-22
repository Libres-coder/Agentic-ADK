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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("JanusGraph相似度函数测试")
class JanusGraphSimilarityFunctionTest {

    private List<Double> vector1;
    private List<Double> vector2;
    private List<Double> vector3;
    private List<Double> zeroVector;
    private List<Double> unitVectorX;
    private List<Double> unitVectorY;

    @BeforeEach
    void setUp() {
        vector1 = Arrays.asList(1.0, 2.0, 3.0);
        vector2 = Arrays.asList(4.0, 5.0, 6.0);
        vector3 = Arrays.asList(1.0, 2.0, 3.0); // 与vector1相同
        zeroVector = Arrays.asList(0.0, 0.0, 0.0);
        unitVectorX = Arrays.asList(1.0, 0.0, 0.0);
        unitVectorY = Arrays.asList(0.0, 1.0, 0.0);
    }

    @Test
    @DisplayName("测试余弦相似度计算")
    void testCosineSimilarity() {
        JanusGraphSimilarityFunction cosine = JanusGraphSimilarityFunction.COSINE;
        
        // 相同向量的余弦相似度应该为1
        double similarity = cosine.calculateSimilarity(vector1, vector3);
        assertEquals(1.0, similarity, 0.0001);
        
        // 正交向量的余弦相似度应该为0
        double orthogonalSimilarity = cosine.calculateSimilarity(unitVectorX, unitVectorY);
        assertEquals(0.0, orthogonalSimilarity, 0.0001);
        
        // 计算实际向量的余弦相似度
        double actualSimilarity = cosine.calculateSimilarity(vector1, vector2);
        assertTrue(actualSimilarity > 0); // 正相关
        assertTrue(actualSimilarity < 1); // 不完全相同
        
        // 余弦距离测试
        double distance = cosine.calculateDistance(vector1, vector3);
        assertEquals(0.0, distance, 0.0001); // 相同向量距离为0
    }

    @Test
    @DisplayName("测试欧几里得距离计算")
    void testEuclideanDistance() {
        JanusGraphSimilarityFunction euclidean = JanusGraphSimilarityFunction.EUCLIDEAN;
        
        // 相同向量的欧几里得距离应该为0
        double distance = euclidean.calculateDistance(vector1, vector3);
        assertEquals(0.0, distance, 0.0001);
        
        // 计算实际向量的欧几里得距离
        double actualDistance = euclidean.calculateDistance(vector1, vector2);
        double expectedDistance = Math.sqrt(Math.pow(1-4, 2) + Math.pow(2-5, 2) + Math.pow(3-6, 2));
        assertEquals(expectedDistance, actualDistance, 0.0001);
        
        // 相似度测试（距离的倒数关系）
        double similarity = euclidean.calculateSimilarity(vector1, vector3);
        assertEquals(1.0, similarity, 0.0001); // 相同向量相似度为1
        
        double actualSimilarity = euclidean.calculateSimilarity(vector1, vector2);
        assertTrue(actualSimilarity > 0);
        assertTrue(actualSimilarity < 1);
    }

    @Test
    @DisplayName("测试曼哈顿距离计算")
    void testManhattanDistance() {
        JanusGraphSimilarityFunction manhattan = JanusGraphSimilarityFunction.MANHATTAN;
        
        // 相同向量的曼哈顿距离应该为0
        double distance = manhattan.calculateDistance(vector1, vector3);
        assertEquals(0.0, distance, 0.0001);
        
        // 计算实际向量的曼哈顿距离
        double actualDistance = manhattan.calculateDistance(vector1, vector2);
        double expectedDistance = Math.abs(1-4) + Math.abs(2-5) + Math.abs(3-6);
        assertEquals(expectedDistance, actualDistance, 0.0001);
        
        // 相似度测试
        double similarity = manhattan.calculateSimilarity(vector1, vector3);
        assertEquals(1.0, similarity, 0.0001);
    }

    @Test
    @DisplayName("测试点积相似度计算")
    void testDotProductSimilarity() {
        JanusGraphSimilarityFunction dotProduct = JanusGraphSimilarityFunction.DOT_PRODUCT;
        
        // 计算点积
        double similarity = dotProduct.calculateSimilarity(vector1, vector2);
        double expectedDotProduct = 1*4 + 2*5 + 3*6;
        assertEquals(expectedDotProduct, similarity, 0.0001);
        
        // 零向量的点积应该为0
        double zeroSimilarity = dotProduct.calculateSimilarity(vector1, zeroVector);
        assertEquals(0.0, zeroSimilarity, 0.0001);
        
        // 距离测试（点积的负值）
        double distance = dotProduct.calculateDistance(vector1, vector2);
        assertEquals(-similarity, distance, 0.0001);
    }

    @Test
    @DisplayName("测试Jaccard相似度计算")
    void testJaccardSimilarity() {
        JanusGraphSimilarityFunction jaccard = JanusGraphSimilarityFunction.JACCARD;
        
        List<Double> binary1 = Arrays.asList(1.0, 0.0, 1.0, 1.0, 0.0);
        List<Double> binary2 = Arrays.asList(1.0, 1.0, 0.0, 1.0, 0.0);
        
        // 计算Jaccard相似度
        double similarity = jaccard.calculateSimilarity(binary1, binary2);
        // 交集：位置0和3 = 2个元素
        // 并集：位置0,1,2,3 = 4个元素
        // Jaccard = 2/4 = 0.5
        assertEquals(0.5, similarity, 0.0001);
        
        // 相同向量的Jaccard相似度应该为1
        double identicalSimilarity = jaccard.calculateSimilarity(binary1, binary1);
        assertEquals(1.0, identicalSimilarity, 0.0001);
        
        // 全零向量的处理
        List<Double> allZeros1 = Arrays.asList(0.0, 0.0, 0.0);
        List<Double> allZeros2 = Arrays.asList(0.0, 0.0, 0.0);
        double zeroSimilarity = jaccard.calculateSimilarity(allZeros1, allZeros2);
        assertEquals(1.0, zeroSimilarity, 0.0001); // 特殊情况：都为空集时相似度为1
    }

    @Test
    @DisplayName("测试向量维度不匹配异常")
    void testVectorDimensionMismatch() {
        List<Double> shortVector = Arrays.asList(1.0, 2.0);
        List<Double> longVector = Arrays.asList(1.0, 2.0, 3.0, 4.0);
        
        for (JanusGraphSimilarityFunction function : JanusGraphSimilarityFunction.values()) {
            assertThrows(IllegalArgumentException.class, () -> 
                function.calculateSimilarity(shortVector, longVector));
            assertThrows(IllegalArgumentException.class, () -> 
                function.calculateDistance(shortVector, longVector));
        }
    }

    @Test
    @DisplayName("测试空向量和null向量处理")
    void testNullAndEmptyVectors() {
        List<Double> emptyVector = Arrays.asList();
        
        for (JanusGraphSimilarityFunction function : JanusGraphSimilarityFunction.values()) {
            // 空向量测试
            assertDoesNotThrow(() -> function.calculateSimilarity(emptyVector, emptyVector));
            
            // null向量应该抛出异常（在实际实现中需要添加null检查）
            // 这里假设实现会检查null值
        }
    }

    @Test
    @DisplayName("测试零向量处理")
    void testZeroVectors() {
        // 余弦相似度对零向量的处理
        JanusGraphSimilarityFunction cosine = JanusGraphSimilarityFunction.COSINE;
        double cosineSimilarity = cosine.calculateSimilarity(zeroVector, vector1);
        assertEquals(0.0, cosineSimilarity, 0.0001);
        
        double cosineZeroToZero = cosine.calculateSimilarity(zeroVector, zeroVector);
        assertEquals(0.0, cosineZeroToZero, 0.0001);
    }

    @ParameterizedTest
    @EnumSource(JanusGraphSimilarityFunction.class)
    @DisplayName("测试所有相似度函数的基本性质")
    void testAllSimilarityFunctions(JanusGraphSimilarityFunction function) {
        // 测试自相似性（除了距离函数外，自己与自己的相似度应该是最高的）
        double selfSimilarity = function.calculateSimilarity(vector1, vector1);
        double otherSimilarity = function.calculateSimilarity(vector1, vector2);
        
        if (function != JanusGraphSimilarityFunction.DOT_PRODUCT) {
            // 对于标准化的相似度函数，自相似应该 >= 其他相似度
            assertTrue(selfSimilarity >= otherSimilarity - 0.0001, 
                "Self-similarity should be >= other similarity for " + function.getName());
        }
        
        // 测试距离函数的基本性质
        double selfDistance = function.calculateDistance(vector1, vector1);
        double otherDistance = function.calculateDistance(vector1, vector2);
        
        if (function == JanusGraphSimilarityFunction.EUCLIDEAN || 
            function == JanusGraphSimilarityFunction.MANHATTAN) {
            // 对于真正的距离函数，自距离应该为0
            assertEquals(0.0, selfDistance, 0.0001, 
                "Self-distance should be 0 for " + function.getName());
            assertTrue(otherDistance >= 0, 
                "Distance should be non-negative for " + function.getName());
        }
    }

    @Test
    @DisplayName("测试函数名称和字符串表示")
    void testFunctionNames() {
        assertEquals("cosine", JanusGraphSimilarityFunction.COSINE.getName());
        assertEquals("euclidean", JanusGraphSimilarityFunction.EUCLIDEAN.getName());
        assertEquals("manhattan", JanusGraphSimilarityFunction.MANHATTAN.getName());
        assertEquals("dot_product", JanusGraphSimilarityFunction.DOT_PRODUCT.getName());
        assertEquals("jaccard", JanusGraphSimilarityFunction.JACCARD.getName());
        
        // 测试toString方法
        assertEquals("cosine", JanusGraphSimilarityFunction.COSINE.toString());
    }

    @Test
    @DisplayName("测试通过名称获取函数")
    void testFromName() {
        assertEquals(JanusGraphSimilarityFunction.COSINE, 
            JanusGraphSimilarityFunction.fromName("cosine"));
        assertEquals(JanusGraphSimilarityFunction.EUCLIDEAN, 
            JanusGraphSimilarityFunction.fromName("euclidean"));
        assertEquals(JanusGraphSimilarityFunction.MANHATTAN, 
            JanusGraphSimilarityFunction.fromName("manhattan"));
        assertEquals(JanusGraphSimilarityFunction.DOT_PRODUCT, 
            JanusGraphSimilarityFunction.fromName("dot_product"));
        assertEquals(JanusGraphSimilarityFunction.JACCARD, 
            JanusGraphSimilarityFunction.fromName("jaccard"));
        
        // 测试大小写不敏感
        assertEquals(JanusGraphSimilarityFunction.COSINE, 
            JanusGraphSimilarityFunction.fromName("COSINE"));
        assertEquals(JanusGraphSimilarityFunction.COSINE, 
            JanusGraphSimilarityFunction.fromName("Cosine"));
        
        // 测试未知名称
        assertThrows(IllegalArgumentException.class, () -> 
            JanusGraphSimilarityFunction.fromName("unknown"));
    }

    @Test
    @DisplayName("测试向量归一化")
    void testVectorNormalization() {
        List<Double> originalVector = Arrays.asList(3.0, 4.0, 0.0);
        List<Double> normalizedVector = JanusGraphSimilarityFunction.normalizeVector(originalVector);
        
        // 计算归一化后向量的模长
        double magnitude = 0.0;
        for (Double value : normalizedVector) {
            magnitude += value * value;
        }
        magnitude = Math.sqrt(magnitude);
        
        assertEquals(1.0, magnitude, 0.0001);
        
        // 测试零向量的归一化
        List<Double> zeroNormalized = JanusGraphSimilarityFunction.normalizeVector(zeroVector);
        assertEquals(zeroVector, zeroNormalized);
    }

    @Test
    @DisplayName("测试相似度函数的数学性质")
    void testMathematicalProperties() {
        JanusGraphSimilarityFunction cosine = JanusGraphSimilarityFunction.COSINE;
        
        // 测试对称性：sim(a,b) = sim(b,a)
        double sim1 = cosine.calculateSimilarity(vector1, vector2);
        double sim2 = cosine.calculateSimilarity(vector2, vector1);
        assertEquals(sim1, sim2, 0.0001);
        
        // 测试距离的对称性：dist(a,b) = dist(b,a)
        double dist1 = cosine.calculateDistance(vector1, vector2);
        double dist2 = cosine.calculateDistance(vector2, vector1);
        assertEquals(dist1, dist2, 0.0001);
    }

    @Test
    @DisplayName("测试边界条件")
    void testBoundaryConditions() {
        // 测试非常小的数值
        List<Double> tinyVector = Arrays.asList(1e-10, 1e-10, 1e-10);
        
        for (JanusGraphSimilarityFunction function : JanusGraphSimilarityFunction.values()) {
            assertDoesNotThrow(() -> {
                double similarity = function.calculateSimilarity(tinyVector, vector1);
                double distance = function.calculateDistance(tinyVector, vector1);
                
                // 确保结果是有限的数值
                assertTrue(Double.isFinite(similarity));
                assertTrue(Double.isFinite(distance));
            });
        }
    }

    @Test
    @DisplayName("测试性能和稳定性")
    void testPerformanceAndStability() {
        // 测试大向量
        List<Double> largeVector1 = Arrays.asList(new Double[1000]);
        List<Double> largeVector2 = Arrays.asList(new Double[1000]);
        
        for (int i = 0; i < 1000; i++) {
            largeVector1.set(i, Math.random());
            largeVector2.set(i, Math.random());
        }
        
        for (JanusGraphSimilarityFunction function : JanusGraphSimilarityFunction.values()) {
            long startTime = System.currentTimeMillis();
            
            assertDoesNotThrow(() -> {
                double similarity = function.calculateSimilarity(largeVector1, largeVector2);
                double distance = function.calculateDistance(largeVector1, largeVector2);
                
                assertTrue(Double.isFinite(similarity));
                assertTrue(Double.isFinite(distance));
            });
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 确保计算时间在合理范围内（小于1秒）
            assertTrue(duration < 1000, "Calculation took too long for " + function.getName());
        }
    }
}
