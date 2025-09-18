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

import com.alibaba.langengine.faiss.exception.FaissException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * FAISS索引模型测试类
 * 
 * @author langengine
 */
public class FaissIndexTest {

    private FaissIndex faissIndex;
    private int testVectorDimension = 64;
    private String testIndexType = "IVFFlat";
    private boolean testUseGpu = false;
    private int testGpuDeviceId = 0;

    @Before
    public void setUp() {
        faissIndex = new FaissIndex(testVectorDimension, testIndexType, testUseGpu, testGpuDeviceId);
    }

    @After
    public void tearDown() {
        if (faissIndex != null) {
            faissIndex.cleanup();
        }
    }

    @Test
    public void testConstructor() {
        // 验证构造函数
        assertNotNull(faissIndex);
        assertEquals(testVectorDimension, faissIndex.getVectorDimension());
        assertEquals(testIndexType, faissIndex.getIndexType());
        assertEquals(testUseGpu, faissIndex.isUseGpu());
        assertEquals(testGpuDeviceId, faissIndex.getGpuDeviceId());
        assertEquals(0, faissIndex.getTotalVectors());
    }

    @Test
    public void testAddVector() {
        // 创建测试向量
        float[] vector = createTestVector();
        int id = 1;
        
        // 添加向量
        faissIndex.addVector(vector, id);
        
        // 验证向量已添加
        assertEquals(1, faissIndex.getTotalVectors());
    }

    @Test
    public void testAddMultipleVectors() {
        // 添加多个向量
        Random random = new Random(42);
        for (int i = 0; i < 10; i++) {
            float[] vector = new float[testVectorDimension];
            for (int j = 0; j < testVectorDimension; j++) {
                vector[j] = random.nextFloat();
            }
            faissIndex.addVector(vector, i);
        }
        
        // 验证向量数量
        assertEquals(10, faissIndex.getTotalVectors());
    }

    @Test(expected = FaissException.class)
    public void testAddVectorWithDimensionMismatch() {
        // 创建错误维度的向量
        float[] wrongVector = new float[testVectorDimension + 10];
        Arrays.fill(wrongVector, 1.0f);
        
        // 添加向量应该抛出异常
        faissIndex.addVector(wrongVector, 1);
    }

    @Test(expected = FaissException.class)
    public void testAddVectorWithNullVector() {
        // 添加null向量应该抛出异常
        faissIndex.addVector(null, 1);
    }

    @Test
    public void testSearch() {
        // 添加测试向量
        Random random = new Random(42);
        for (int i = 0; i < 5; i++) {
            float[] vector = new float[testVectorDimension];
            for (int j = 0; j < testVectorDimension; j++) {
                vector[j] = random.nextFloat();
            }
            faissIndex.addVector(vector, i);
        }
        
        // 创建查询向量
        float[] queryVector = createTestVector();
        
        // 执行搜索
        List<FaissSearchResult> results = faissIndex.search(queryVector, 3);
        
        // 验证搜索结果
        assertNotNull(results);
        assertTrue(results.size() <= 3);
        assertTrue(results.size() <= 5); // 不能超过总向量数
        
        // 验证结果按相似性排序
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i-1).getScore() >= results.get(i).getScore());
        }
    }

    @Test
    public void testSearchWithEmptyIndex() {
        // 在空索引上执行搜索
        float[] queryVector = createTestVector();
        List<FaissSearchResult> results = faissIndex.search(queryVector, 5);
        
        // 验证返回空结果
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test(expected = FaissException.class)
    public void testSearchWithDimensionMismatch() {
        // 使用错误维度的查询向量
        float[] wrongQueryVector = new float[testVectorDimension + 10];
        Arrays.fill(wrongQueryVector, 1.0f);
        
        // 搜索应该抛出异常
        faissIndex.search(wrongQueryVector, 5);
    }

    @Test(expected = FaissException.class)
    public void testSearchWithNullQuery() {
        // 使用null查询向量
        faissIndex.search(null, 5);
    }

    @Test
    public void testRemoveVector() {
        // 添加测试向量
        float[] vector1 = createTestVector();
        float[] vector2 = createTestVector();
        
        faissIndex.addVector(vector1, 1);
        faissIndex.addVector(vector2, 2);
        
        // 验证初始状态
        assertEquals(2, faissIndex.getTotalVectors());
        
        // 删除向量
        faissIndex.removeVector(1);
        
        // 验证向量已删除
        assertEquals(1, faissIndex.getTotalVectors());
    }

    @Test
    public void testRemoveNonExistentVector() {
        // 删除不存在的向量
        faissIndex.removeVector(999);
        
        // 验证没有异常抛出
        assertEquals(0, faissIndex.getTotalVectors());
    }

    @Test
    public void testCalculateDistance() {
        // 创建两个相同的向量
        float[] vector1 = new float[testVectorDimension];
        float[] vector2 = new float[testVectorDimension];
        Arrays.fill(vector1, 1.0f);
        Arrays.fill(vector2, 1.0f);
        
        // 添加向量
        faissIndex.addVector(vector1, 1);
        faissIndex.addVector(vector2, 2);
        
        // 搜索应该返回距离为0的结果
        List<FaissSearchResult> results = faissIndex.search(vector1, 2);
        
        // 验证结果
        assertNotNull(results);
        assertTrue(results.size() >= 1);
        
        // 第一个结果应该是完全匹配（距离为0）
        FaissSearchResult firstResult = results.get(0);
        assertEquals(0.0f, firstResult.getDistance(), 0.001f);
        assertEquals(1.0f, firstResult.getScore(), 0.001f);
    }

    @Test
    public void testCalculateDistanceWithDifferentVectors() {
        // 创建两个不同的向量
        float[] vector1 = new float[testVectorDimension];
        float[] vector2 = new float[testVectorDimension];
        
        Arrays.fill(vector1, 0.0f);
        Arrays.fill(vector2, 1.0f);
        
        // 添加向量
        faissIndex.addVector(vector1, 1);
        faissIndex.addVector(vector2, 2);
        
        // 搜索
        List<FaissSearchResult> results = faissIndex.search(vector1, 2);
        
        // 验证结果
        assertNotNull(results);
        assertTrue(results.size() >= 1);
        
        // 第一个结果应该是完全匹配
        FaissSearchResult firstResult = results.get(0);
        assertEquals(0.0f, firstResult.getDistance(), 0.001f);
        assertEquals(1.0f, firstResult.getScore(), 0.001f);
    }

    @Test
    public void testSaveAndLoad() {
        // 添加测试向量
        float[] vector = createTestVector();
        faissIndex.addVector(vector, 1);
        
        // 保存索引
        String testPath = "./test_faiss_index_save";
        faissIndex.save(testPath);
        
        // 验证保存成功（没有异常抛出）
        assertNotNull(faissIndex);
        
        // 加载索引
        faissIndex.load(testPath);
        
        // 验证加载成功（没有异常抛出）
        assertNotNull(faissIndex);
    }

    @Test
    public void testCleanup() {
        // 添加测试向量
        float[] vector = createTestVector();
        faissIndex.addVector(vector, 1);
        
        // 验证初始状态
        assertEquals(1, faissIndex.getTotalVectors());
        
        // 清理资源
        faissIndex.cleanup();
        
        // 验证清理成功
        assertEquals(0, faissIndex.getTotalVectors());
        assertTrue(faissIndex.getVectors().isEmpty());
        assertTrue(faissIndex.getVectorMap().isEmpty());
    }

    @Test
    public void testGetTotalVectors() {
        // 验证初始状态
        assertEquals(0, faissIndex.getTotalVectors());
        
        // 添加向量
        float[] vector = createTestVector();
        faissIndex.addVector(vector, 1);
        
        // 验证向量数量
        assertEquals(1, faissIndex.getTotalVectors());
        
        // 添加更多向量
        faissIndex.addVector(vector, 2);
        faissIndex.addVector(vector, 3);
        
        // 验证向量数量
        assertEquals(3, faissIndex.getTotalVectors());
    }

    @Test
    public void testSearchResultOrdering() {
        // 添加多个不同的向量
        Random random = new Random(123);
        for (int i = 0; i < 10; i++) {
            float[] vector = new float[testVectorDimension];
            for (int j = 0; j < testVectorDimension; j++) {
                vector[j] = random.nextFloat();
            }
            faissIndex.addVector(vector, i);
        }
        
        // 创建查询向量
        float[] queryVector = createTestVector();
        
        // 执行搜索
        List<FaissSearchResult> results = faissIndex.search(queryVector, 5);
        
        // 验证结果按相似性排序
        assertNotNull(results);
        assertTrue(results.size() <= 5);
        
        for (int i = 1; i < results.size(); i++) {
            FaissSearchResult prev = results.get(i-1);
            FaissSearchResult curr = results.get(i);
            
            assertTrue("Results should be ordered by similarity score", 
                prev.getScore() >= curr.getScore());
            assertTrue("Results should be ordered by distance", 
                prev.getDistance() <= curr.getDistance());
        }
    }

    /**
     * 创建测试向量
     */
    private float[] createTestVector() {
        float[] vector = new float[testVectorDimension];
        Random random = new Random(42);
        
        for (int i = 0; i < testVectorDimension; i++) {
            vector[i] = random.nextFloat();
        }
        
        return vector;
    }
}
