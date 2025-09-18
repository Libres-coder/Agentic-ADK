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
package com.alibaba.langengine.faiss.service;

import com.alibaba.langengine.faiss.exception.FaissException;
import com.alibaba.langengine.faiss.model.FaissSearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

/**
 * FAISS服务测试类
 * 
 * @author langengine
 */
public class FaissServiceTest {

    private FaissService faissService;
    private String testIndexPath = "./test_faiss_service_index";
    private int testVectorDimension = 128;
    private String testIndexType = "IVFFlat";
    private boolean testUseGpu = false;
    private int testGpuDeviceId = 0;

    @Before
    public void setUp() {
        faissService = new FaissService(testIndexPath, testVectorDimension, testIndexType, testUseGpu, testGpuDeviceId);
    }

    @After
    public void tearDown() {
        if (faissService != null) {
            faissService.cleanup();
        }
        
        // 清理测试文件
        File indexFile = new File(testIndexPath);
        if (indexFile.exists()) {
            indexFile.delete();
        }
    }

    @Test
    public void testInitialize() {
        // 测试初始化
        faissService.initialize();
        
        // 验证初始化成功
        assertNotNull(faissService.getIndex());
        assertEquals(testIndexPath, faissService.getIndexPath());
        assertEquals(testVectorDimension, faissService.getVectorDimension());
        assertEquals(testIndexType, faissService.getIndexType());
    }

    @Test
    public void testAddVectors() {
        // 初始化服务
        faissService.initialize();
        
        // 准备测试向量
        List<float[]> vectors = createTestVectors(5);
        List<String> documentIds = Arrays.asList("doc1", "doc2", "doc3", "doc4", "doc5");
        
        // 添加向量
        faissService.addVectors(vectors, documentIds);
        
        // 验证向量已添加
        assertEquals(5, faissService.getIndex().getTotalVectors());
    }

    @Test
    public void testAddVectorsWithNullDocumentIds() {
        // 初始化服务
        faissService.initialize();
        
        // 准备测试向量
        List<float[]> vectors = createTestVectors(3);
        
        // 添加向量（documentIds为null）
        faissService.addVectors(vectors, null);
        
        // 验证向量已添加
        assertEquals(3, faissService.getIndex().getTotalVectors());
    }

    @Test(expected = FaissException.class)
    public void testAddVectorsWithDimensionMismatch() {
        // 初始化服务
        faissService.initialize();
        
        // 准备错误维度的向量
        List<float[]> vectors = new ArrayList<>();
        float[] wrongVector = new float[testVectorDimension + 10];
        Arrays.fill(wrongVector, 1.0f);
        vectors.add(wrongVector);
        
        // 添加向量应该抛出异常
        faissService.addVectors(vectors, Arrays.asList("doc1"));
    }

    @Test(expected = FaissException.class)
    public void testAddVectorsWithNullVectors() {
        // 初始化服务
        faissService.initialize();
        
        // 添加null向量应该抛出异常
        faissService.addVectors(null, Arrays.asList("doc1"));
    }

    @Test
    public void testSearch() {
        // 初始化服务
        faissService.initialize();
        
        // 添加测试向量
        List<float[]> vectors = createTestVectors(10);
        List<String> documentIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            documentIds.add("doc" + i);
        }
        faissService.addVectors(vectors, documentIds);
        
        // 执行搜索
        float[] queryVector = createTestVector();
        List<FaissSearchResult> results = faissService.search(queryVector, 5, null);
        
        // 验证搜索结果
        assertNotNull(results);
        assertTrue(results.size() <= 5);
        
        // 验证结果按相似性排序
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i-1).getScore() >= results.get(i).getScore());
        }
    }

    @Test
    public void testSearchWithMaxDistance() {
        // 初始化服务
        faissService.initialize();
        
        // 添加测试向量
        List<float[]> vectors = createTestVectors(10);
        List<String> documentIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            documentIds.add("doc" + i);
        }
        faissService.addVectors(vectors, documentIds);
        
        // 执行带距离阈值的搜索
        float[] queryVector = createTestVector();
        double maxDistance = 0.5;
        List<FaissSearchResult> results = faissService.search(queryVector, 10, maxDistance);
        
        // 验证搜索结果
        assertNotNull(results);
        
        // 验证所有结果的距离都在阈值内
        for (FaissSearchResult result : results) {
            assertTrue(result.getDistance() <= maxDistance);
        }
    }

    @Test(expected = FaissException.class)
    public void testSearchWithDimensionMismatch() {
        // 初始化服务
        faissService.initialize();
        
        // 使用错误维度的查询向量
        float[] wrongQueryVector = new float[testVectorDimension + 10];
        Arrays.fill(wrongQueryVector, 1.0f);
        
        // 搜索应该抛出异常
        faissService.search(wrongQueryVector, 5, null);
    }

    @Test
    public void testDeleteVector() {
        // 初始化服务
        faissService.initialize();
        
        // 添加测试向量
        List<float[]> vectors = createTestVectors(5);
        List<String> documentIds = Arrays.asList("doc1", "doc2", "doc3", "doc4", "doc5");
        faissService.addVectors(vectors, documentIds);
        
        // 删除向量
        faissService.deleteVector("doc1");
        
        // 验证向量已删除
        assertEquals(4, faissService.getIndex().getTotalVectors());
    }

    @Test
    public void testDeleteNonExistentVector() {
        // 初始化服务
        faissService.initialize();
        
        // 删除不存在的向量
        faissService.deleteVector("non_existent_doc");
        
        // 验证没有异常抛出
        assertNotNull(faissService.getIndex());
    }

    @Test
    public void testGetIndexStats() {
        // 初始化服务
        faissService.initialize();
        
        // 添加测试向量
        List<float[]> vectors = createTestVectors(3);
        List<String> documentIds = Arrays.asList("doc1", "doc2", "doc3");
        faissService.addVectors(vectors, documentIds);
        
        // 获取统计信息
        Map<String, Object> stats = faissService.getIndexStats();
        
        // 验证统计信息
        assertNotNull(stats);
        assertTrue(stats.containsKey("total_vectors"));
        assertTrue(stats.containsKey("vector_dimension"));
        assertTrue(stats.containsKey("index_type"));
        assertTrue(stats.containsKey("use_gpu"));
        assertTrue(stats.containsKey("gpu_device_id"));
        assertTrue(stats.containsKey("index_path"));
        assertTrue(stats.containsKey("document_count"));
        
        assertEquals(3, stats.get("total_vectors"));
        assertEquals(testVectorDimension, stats.get("vector_dimension"));
        assertEquals(testIndexType, stats.get("index_type"));
        assertEquals(testUseGpu, stats.get("use_gpu"));
        assertEquals(testGpuDeviceId, stats.get("gpu_device_id"));
        assertEquals(testIndexPath, stats.get("index_path"));
        assertEquals(3, stats.get("document_count"));
    }

    @Test
    public void testSaveAndLoadIndex() {
        // 初始化服务
        faissService.initialize();
        
        // 添加测试向量
        List<float[]> vectors = createTestVectors(5);
        List<String> documentIds = Arrays.asList("doc1", "doc2", "doc3", "doc4", "doc5");
        faissService.addVectors(vectors, documentIds);
        
        // 保存索引
        faissService.saveIndex();
        
        // 验证索引文件存在
        File indexFile = new File(testIndexPath);
        assertTrue(indexFile.exists());
        
        // 创建新的服务实例
        FaissService newService = new FaissService(testIndexPath, testVectorDimension, testIndexType, testUseGpu, testGpuDeviceId);
        
        // 加载索引
        newService.loadIndex();
        
        // 验证索引加载成功
        assertNotNull(newService.getIndex());
    }

    @Test
    public void testGetIndexSize() {
        // 初始化服务
        faissService.initialize();
        
        // 添加测试向量
        List<float[]> vectors = createTestVectors(7);
        List<String> documentIds = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            documentIds.add("doc" + i);
        }
        faissService.addVectors(vectors, documentIds);
        
        // 获取索引大小
        long size = faissService.getIndexSize();
        
        // 验证索引大小
        assertEquals(7, size);
    }

    @Test
    public void testCleanup() {
        // 初始化服务
        faissService.initialize();
        
        // 添加测试向量
        List<float[]> vectors = createTestVectors(3);
        List<String> documentIds = Arrays.asList("doc1", "doc2", "doc3");
        faissService.addVectors(vectors, documentIds);
        
        // 清理资源
        faissService.cleanup();
        
        // 验证清理成功
        assertTrue(faissService.getDocumentIdToIndexMap().isEmpty());
        assertTrue(faissService.getIndexToDocumentIdMap().isEmpty());
    }

    @Test
    public void testRebuildIndex() {
        // 初始化服务
        faissService.initialize();
        
        // 添加测试向量
        List<float[]> vectors = createTestVectors(5);
        List<String> documentIds = Arrays.asList("doc1", "doc2", "doc3", "doc4", "doc5");
        faissService.addVectors(vectors, documentIds);
        
        // 重建索引
        faissService.rebuildIndex();
        
        // 验证重建成功（没有异常抛出）
        assertNotNull(faissService.getIndex());
    }

    /**
     * 创建测试向量
     */
    private List<float[]> createTestVectors(int count) {
        List<float[]> vectors = new ArrayList<>();
        Random random = new Random(42); // 使用固定种子确保测试可重复
        
        for (int i = 0; i < count; i++) {
            float[] vector = new float[testVectorDimension];
            for (int j = 0; j < testVectorDimension; j++) {
                vector[j] = random.nextFloat();
            }
            vectors.add(vector);
        }
        
        return vectors;
    }

    /**
     * 创建单个测试向量
     */
    private float[] createTestVector() {
        float[] vector = new float[testVectorDimension];
        Random random = new Random(123); // 使用固定种子确保测试可重复
        
        for (int i = 0; i < testVectorDimension; i++) {
            vector[i] = random.nextFloat();
        }
        
        return vector;
    }
}
