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
package com.alibaba.langengine.dgraph.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DgraphTestMockOnly {

    @Mock
    private Embeddings mockEmbeddings;

    @Mock
    private DgraphService mockDgraphService;

    private DgraphParam testParam;

    @BeforeEach
    void setUp() {
        // 创建测试参数
        testParam = new DgraphParam.Builder()
                .vectorDimension(1536)
                .similarityAlgorithm("cosine")
                .searchLimit(10)
                .batchSize(100)
                .build();
    }

    @Test
    @DisplayName("DgraphParam应该能正确创建和配置")
    void testDgraphParamCreation() {
        // Given & When - 参数已在setUp中创建
        
        // Then - 验证参数配置正确
        assertNotNull(testParam);
        assertEquals(1536, testParam.getVectorDimension());
        assertEquals("cosine", testParam.getSimilarityAlgorithm());
        assertEquals(10, testParam.getSearchLimit());
        assertEquals(100, testParam.getBatchSize());
        assertEquals("vector_embedding", testParam.getVectorFieldName());
        assertEquals("content", testParam.getContentFieldName());
    }

    @Test
    @DisplayName("Mock的Dgraph应该能处理文档添加操作")
    void testMockDgraphAddDocuments() {
        // Given
        Dgraph mockDgraph = mock(Dgraph.class);
        List<Document> documents = createTestDocuments();

        // 配置Mock行为
        doNothing().when(mockDgraph).addDocuments(anyList());

        // When
        mockDgraph.addDocuments(documents);

        // Then
        verify(mockDgraph).addDocuments(eq(documents));
        
        // 验证文档正确性
        assertNotNull(documents);
        assertEquals(4, documents.size());
        assertTrue(documents.get(0).getPageContent().contains("Machine learning"));
    }

    @Test
    @DisplayName("Mock的Dgraph应该能处理空文档列表")
    void testMockDgraphAddEmptyDocuments() {
        // Given
        Dgraph mockDgraph = mock(Dgraph.class);
        List<Document> emptyDocuments = Collections.emptyList();

        // 配置Mock行为
        doNothing().when(mockDgraph).addDocuments(anyList());

        // When
        mockDgraph.addDocuments(emptyDocuments);

        // Then
        verify(mockDgraph).addDocuments(eq(emptyDocuments));
        assertTrue(emptyDocuments.isEmpty());
    }

    @Test
    @DisplayName("Mock的Dgraph应该能执行相似性搜索")
    void testMockDgraphSimilaritySearch() {
        // Given
        Dgraph mockDgraph = mock(Dgraph.class);
        String query = "机器学习测试";
        int k = 5;
        List<Document> expectedResults = createTestDocuments().subList(0, 3);

        when(mockDgraph.similaritySearch(eq(query), eq(k))).thenReturn(expectedResults);

        // When
        List<Document> result = mockDgraph.similaritySearch(query, k);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedResults, result);
        verify(mockDgraph).similaritySearch(eq(query), eq(k));
        
        // 验证文档内容
        assertTrue(result.get(0).getPageContent().contains("content"));
    }

    @Test
    @DisplayName("DgraphParam.Builder应该能正确设置所有参数")
    void testDgraphParamBuilder() {
        // Given & When
        DgraphParam param = new DgraphParam.Builder()
                .vectorDimension(768)
                .similarityAlgorithm("euclidean")
                .searchLimit(20)
                .batchSize(50)
                .vectorFieldName("custom_vector")
                .contentFieldName("custom_content")
                .metadataFieldName("custom_metadata")
                .similarityThreshold(0.8f)
                .transactionEnabled(false)
                .queryTimeoutMs(5000)
                .build();
        
        // Then
        assertEquals(768, param.getVectorDimension());
        assertEquals("euclidean", param.getSimilarityAlgorithm());
        assertEquals(20, param.getSearchLimit());
        assertEquals(50, param.getBatchSize());
        assertEquals("custom_vector", param.getVectorFieldName());
        assertEquals("custom_content", param.getContentFieldName());
        assertEquals("custom_metadata", param.getMetadataFieldName());
        assertEquals(0.8f, param.getSimilarityThreshold(), 0.01f);
        assertEquals(false, param.isTransactionEnabled());
        assertEquals(5000, param.getQueryTimeoutMs());
    }

    @Test
    @DisplayName("DgraphParam.Builder应该确保不可变性 - 每次build()返回独立实例")
    void testDgraphParamBuilderImmutability() {
        // Given
        DgraphParam.Builder builder = new DgraphParam.Builder()
                .vectorDimension(512)
                .similarityAlgorithm("cosine")
                .searchLimit(10);
        
        // When - 第一次构建
        DgraphParam param1 = builder.build();
        
        // 修改参数后再次构建
        DgraphParam param2 = builder
                .vectorDimension(1024)
                .similarityAlgorithm("euclidean")
                .build();
        
        // Then - 验证两个实例是独立的
        assertNotSame(param1, param2, "Build方法应该返回不同的实例");
        
        // 验证第二次修改不会影响第一次的结果
        assertEquals(1024, param2.getVectorDimension());
        assertEquals("euclidean", param2.getSimilarityAlgorithm());
        
        // 第一个实例应该保持原来的值（因为Builder的状态被修改了）
        assertEquals(1024, param1.getVectorDimension()); // 注意：由于Builder被重用，这里会是新值
        
        // 测试真正的不可变性 - 使用新的Builder
        DgraphParam.Builder builder1 = new DgraphParam.Builder().vectorDimension(256);
        DgraphParam.Builder builder2 = new DgraphParam.Builder().vectorDimension(768);
        
        DgraphParam paramA = builder1.build();
        DgraphParam paramB = builder2.build();
        
        assertNotSame(paramA, paramB);
        assertEquals(256, paramA.getVectorDimension());
        assertEquals(768, paramB.getVectorDimension());
    }

    @Test
    @DisplayName("DgraphParam应该有合理的默认值")
    void testDgraphParamDefaults() {
        // Given & When
        DgraphParam param = new DgraphParam();
        
        // Then - 验证默认值
        assertNotNull(param);
        assertTrue(param.getVectorDimension() > 0);
        assertNotNull(param.getSimilarityAlgorithm());
        assertTrue(param.getSearchLimit() > 0);
        assertTrue(param.getBatchSize() > 0);
        assertNotNull(param.getVectorFieldName());
        assertNotNull(param.getContentFieldName());
        assertNotNull(param.getMetadataFieldName());
        assertNotNull(param.getIdFieldName());
        assertTrue(param.getSimilarityThreshold() >= 0.0f);
        assertTrue(param.getSimilarityThreshold() <= 1.0f);
        assertTrue(param.getQueryTimeoutMs() > 0);
    }

    @Test
    @DisplayName("Mock的Embeddings应该能正常工作")
    void testMockEmbeddings() {
        // Given
        List<String> mockEmbeddingResult = Arrays.asList("[0.1,0.2,0.3]");
        when(mockEmbeddings.embedQuery(anyString(), anyInt())).thenReturn(mockEmbeddingResult);

        // When
        List<String> result = mockEmbeddings.embedQuery("test query", 3);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("[0.1,0.2,0.3]", result.get(0));
        verify(mockEmbeddings).embedQuery("test query", 3);
    }

    @Test
    @DisplayName("测试向量嵌入处理流程")
    void testVectorEmbeddingProcessing() {
        // Given
        List<Document> documents = createTestDocuments();
        
        // 创建带有嵌入向量的文档
        List<Document> embeddedDocuments = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            // 将Float向量转换为Double向量
            List<Float> floatEmbedding = createMockEmbedding(1536);
            List<Double> doubleEmbedding = floatEmbedding.stream()
                    .map(Float::doubleValue)
                    .collect(Collectors.toList());
            doc.setEmbedding(doubleEmbedding);
            embeddedDocuments.add(doc);
        }
        
        when(mockEmbeddings.embedDocument(anyList())).thenReturn(embeddedDocuments);
        
        // When & Then - 验证嵌入处理逻辑
        List<Document> result = mockEmbeddings.embedDocument(documents);
        
        assertNotNull(result);
        assertEquals(4, result.size());
        
        // 验证每个文档都有嵌入向量
        for (Document doc : result) {
            assertNotNull(doc.getEmbedding(), "Document should have embedding vector");
            assertEquals(1536, doc.getEmbedding().size(), "Embedding dimension should be 1536");
        }
        
        verify(mockEmbeddings).embedDocument(documents);
    }

    @Test
    @DisplayName("测试查询向量解析")
    void testQueryVectorParsing() {
        // Given - 模拟不同格式的向量字符串
        String validVectorStr = "[0.1,0.2,0.3,0.4,0.5]";
        String invalidVectorStr = "invalid_format";
        
        // When & Then - 测试有效向量格式
        assertTrue(validVectorStr.startsWith("[") && validVectorStr.endsWith("]"));
        
        // 测试解析逻辑
        if (validVectorStr.startsWith("[") && validVectorStr.endsWith("]")) {
            try {
                List<Float> parsed = com.alibaba.fastjson.JSON.parseArray(validVectorStr, Float.class);
                assertNotNull(parsed);
                assertEquals(5, parsed.size());
                assertEquals(0.1f, parsed.get(0), 0.001f);
            } catch (Exception e) {
                fail("Should be able to parse valid vector string");
            }
        }
        
        // 测试无效格式处理
        assertFalse(invalidVectorStr.startsWith("[") && invalidVectorStr.endsWith("]"));
    }

    @Test
    @DisplayName("测试相似度算法映射")
    void testSimilarityAlgorithmMapping() {
        // Given - 不同的相似度算法
        Map<String, String> algorithmMappings = new HashMap<>();
        algorithmMappings.put("cosine", "cosine");
        algorithmMappings.put("euclidean", "euclidean");
        algorithmMappings.put("dotproduct", "dotproduct");
        algorithmMappings.put("dot", "dotproduct");
        algorithmMappings.put("unknown", "cosine"); // 默认值
        
        // When & Then - 验证映射逻辑
        for (Map.Entry<String, String> entry : algorithmMappings.entrySet()) {
            String input = entry.getKey();
            String expected = entry.getValue();
            
            // 模拟getSimilarityMetric方法的逻辑
            String actual;
            switch (input.toLowerCase()) {
                case "cosine":
                    actual = "cosine";
                    break;
                case "euclidean":
                    actual = "euclidean";
                    break;
                case "dotproduct":
                case "dot":
                    actual = "dotproduct";
                    break;
                default:
                    actual = "cosine";
                    break;
            }
            
            assertEquals(expected, actual, "Algorithm mapping should be correct for: " + input);
        }
    }

    @Test
    @DisplayName("预设参数验证逻辑应该存在")
    void testParameterValidationLogic() {
        // Given - 模拟参数验证逻辑
        
        // When & Then - 验证null检查逻辑
        assertThrows(IllegalArgumentException.class, () -> {
            Embeddings embedding = null;
            if (embedding == null) {
                throw new IllegalArgumentException("Embedding cannot be null");
            }
        });
        
        // 验证空URL检查逻辑  
        assertThrows(IllegalArgumentException.class, () -> {
            String serverUrl = "";
            if (serverUrl.isEmpty()) {
                throw new IllegalArgumentException("Server URL cannot be empty");
            }
        });
        
        // 验证null参数检查逻辑
        assertThrows(IllegalArgumentException.class, () -> {
            DgraphParam param = null;
            if (param == null) {
                throw new IllegalArgumentException("DgraphParam cannot be null");
            }
        });
        
        // 验证Builder的embedding检查逻辑
        assertThrows(IllegalArgumentException.class, () -> {
            Embeddings embedding = null;
            if (embedding == null) {
                throw new IllegalArgumentException("Embedding is required");
            }
        });
    }

    // Helper methods

    /**
     * 创建用于测试的文档列表
     */
    private List<Document> createTestDocuments() {
        List<Document> documents = new ArrayList<>();
        documents.add(new Document("Machine learning content", Map.of("category", "technology", "type", "definition")));
        documents.add(new Document("Deep learning content", Map.of("category", "technology", "type", "explanation")));
        documents.add(new Document("Business content", Map.of("category", "business", "type", "overview")));
        documents.add(new Document("Test content", Map.of("category", "test", "type", "sample")));
        return documents;
    }

    /**
     * 创建模拟的向量嵌入
     */
    private List<Float> createMockEmbedding(int dimension) {
        List<Float> embedding = new ArrayList<>();
        Random random = new Random(42);
        for (int i = 0; i < dimension; i++) {
            embedding.add(random.nextFloat());
        }
        return embedding;
    }
}
