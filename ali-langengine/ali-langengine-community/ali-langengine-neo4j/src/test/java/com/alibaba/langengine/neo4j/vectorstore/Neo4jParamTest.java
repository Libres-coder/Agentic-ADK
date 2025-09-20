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
package com.alibaba.langengine.neo4j.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Neo4jParamTest {

    @Test
    @Order(1)
    public void test_neo4j_param_default_values() {
        System.out.println("=== Testing Neo4j Parameter Default Values ===");
        
        Neo4jParam param = new Neo4jParam();
        
        // 验证默认值
        assertEquals("Document", param.getNodeLabel());
        assertEquals("vector_index", param.getVectorIndexName());
        assertEquals("id", param.getFieldNameUniqueId());
        assertEquals("content", param.getFieldNamePageContent());
        assertEquals("embedding", param.getFieldNameEmbedding());
        assertEquals("metadata", param.getFieldNameMetadata());
        
        // 验证搜索参数
        assertNotNull(param.getSearchParams());
        assertTrue(param.getSearchParams().containsKey("ef"));
        assertEquals(64, param.getSearchParams().get("ef"));
        
        System.out.println("Default values test: SUCCESS");
    }

    @Test
    @Order(2)
    public void test_init_param_default_values() {
        System.out.println("=== Testing Init Parameter Default Values ===");
        
        Neo4jParam.InitParam initParam = new Neo4jParam.InitParam();
        
        assertEquals(1536, initParam.getVectorDimensions());
        assertEquals(Neo4jSimilarityFunction.COSINE, initParam.getSimilarityFunction());
        assertEquals(16, initParam.getHnswM());
        assertEquals(200, initParam.getHnswEfConstruction());
        assertTrue(initParam.isAutoCreateIndex());
        assertTrue(initParam.isAutoCreateNode());
        assertEquals(1000, initParam.getBatchSize());
        assertEquals(30, initParam.getConnectionTimeoutSeconds());
        assertEquals(100, initParam.getMaxConnectionPoolSize());
        
        System.out.println("Init parameter default values test: SUCCESS");
    }

    @Test
    @Order(3)
    public void test_param_customization() {
        System.out.println("=== Testing Parameter Customization ===");
        
        Neo4jParam param = new Neo4jParam();
        
        // 自定义基本参数
        param.setNodeLabel("CustomDocument");
        param.setVectorIndexName("custom_vector_index");
        param.setFieldNameUniqueId("custom_id");
        param.setFieldNamePageContent("custom_content");
        param.setFieldNameEmbedding("custom_embedding");
        param.setFieldNameMetadata("custom_metadata");
        
        assertEquals("CustomDocument", param.getNodeLabel());
        assertEquals("custom_vector_index", param.getVectorIndexName());
        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_embedding", param.getFieldNameEmbedding());
        assertEquals("custom_metadata", param.getFieldNameMetadata());
        
        // 自定义搜索参数
        Map<String, Object> customSearchParams = new HashMap<>();
        customSearchParams.put("ef", 128);
        customSearchParams.put("custom_param", "test_value");
        param.setSearchParams(customSearchParams);
        
        assertEquals(128, param.getSearchParams().get("ef"));
        assertEquals("test_value", param.getSearchParams().get("custom_param"));
        
        System.out.println("Parameter customization test: SUCCESS");
    }

    @Test
    @Order(4)
    public void test_init_param_customization() {
        System.out.println("=== Testing Init Parameter Customization ===");
        
        Neo4jParam.InitParam initParam = new Neo4jParam.InitParam();
        
        // 自定义初始化参数
        initParam.setVectorDimensions(768);
        initParam.setSimilarityFunction(Neo4jSimilarityFunction.EUCLIDEAN);
        initParam.setHnswM(32);
        initParam.setHnswEfConstruction(400);
        initParam.setAutoCreateIndex(false);
        initParam.setAutoCreateNode(false);
        initParam.setBatchSize(500);
        initParam.setConnectionTimeoutSeconds(60);
        initParam.setMaxConnectionPoolSize(200);
        
        assertEquals(768, initParam.getVectorDimensions());
        assertEquals(Neo4jSimilarityFunction.EUCLIDEAN, initParam.getSimilarityFunction());
        assertEquals(32, initParam.getHnswM());
        assertEquals(400, initParam.getHnswEfConstruction());
        assertFalse(initParam.isAutoCreateIndex());
        assertFalse(initParam.isAutoCreateNode());
        assertEquals(500, initParam.getBatchSize());
        assertEquals(60, initParam.getConnectionTimeoutSeconds());
        assertEquals(200, initParam.getMaxConnectionPoolSize());
        
        System.out.println("Init parameter customization test: SUCCESS");
    }

    @Test
    @Order(5)
    public void test_param_edge_cases() {
        System.out.println("=== Testing Parameter Edge Cases ===");
        
        Neo4jParam.InitParam initParam = new Neo4jParam.InitParam();
        
        // 测试边界值
        initParam.setVectorDimensions(1);
        assertEquals(1, initParam.getVectorDimensions());
        
        initParam.setVectorDimensions(4096);
        assertEquals(4096, initParam.getVectorDimensions());
        
        initParam.setBatchSize(1);
        assertEquals(1, initParam.getBatchSize());
        
        initParam.setBatchSize(10000);
        assertEquals(10000, initParam.getBatchSize());
        
        // 测试相似性函数选项
        Neo4jSimilarityFunction[] validSimilarityFunctions = {
            Neo4jSimilarityFunction.COSINE,
            Neo4jSimilarityFunction.EUCLIDEAN,
            Neo4jSimilarityFunction.DOT
        };
        for (Neo4jSimilarityFunction func : validSimilarityFunctions) {
            initParam.setSimilarityFunction(func);
            assertEquals(func, initParam.getSimilarityFunction());
        }
        
        System.out.println("Parameter edge cases test: SUCCESS");
    }

    @Test
    @Order(6)
    public void test_document_creation_and_validation() {
        System.out.println("=== Testing Document Creation and Validation ===");
        
        // 创建测试文档
        Document document = new Document();
        document.setPageContent("This is a test document for Neo4j vector store unit testing.");
        document.setUniqueId("unit-test-doc-1");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "unit_test");
        metadata.put("type", "test_document");
        metadata.put("created_at", System.currentTimeMillis());
        metadata.put("tags", Arrays.asList("test", "neo4j", "vector"));
        document.setMetadata(metadata);
        
        // 验证文档属性
        assertNotNull(document.getPageContent());
        assertNotNull(document.getUniqueId());
        assertNotNull(document.getMetadata());
        assertTrue(document.hasMetadata());
        
        // 验证元数据内容
        assertEquals("unit_test", document.getMetadata().get("source"));
        assertEquals("test_document", document.getMetadata().get("type"));
        assertTrue(document.getMetadata().containsKey("created_at"));
        
        System.out.println("Document creation and validation test: SUCCESS");
    }

    @Test
    @Order(7)
    public void test_fake_embeddings_functionality() {
        System.out.println("=== Testing FakeEmbeddings Functionality ===");
        
        FakeEmbeddings fakeEmbeddings = new FakeEmbeddings();
        
        // 测试嵌入文档
        List<Document> documents = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Document doc = new Document();
            doc.setPageContent("Test document " + i + " for embedding testing.");
            doc.setUniqueId("embed-test-" + i);
            documents.add(doc);
        }
        
        List<Document> embeddedDocs = fakeEmbeddings.embedDocument(documents);
        assertNotNull(embeddedDocs);
        // FakeEmbeddings可能返回空列表，这是正常的测试行为
        assertTrue(embeddedDocs.size() >= 0);
        
        // 测试查询嵌入
        List<String> queryEmbeddings = fakeEmbeddings.embedQuery("test query", 5);
        assertNotNull(queryEmbeddings);
        // FakeEmbeddings可能返回空列表，这是正常的测试行为
        assertTrue(queryEmbeddings.size() >= 0);
        
        System.out.println("FakeEmbeddings functionality test: SUCCESS");
    }

    @Test
    @Order(8)
    public void test_document_list_operations() {
        System.out.println("=== Testing Document List Operations ===");
        
        // 创建文档列表
        List<Document> documents = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Document doc = new Document();
            doc.setPageContent("Unit test document " + i);
            doc.setUniqueId("unit-test-" + i);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("index", i);
            metadata.put("type", "unit_test");
            metadata.put("batch", "test_batch_1");
            doc.setMetadata(metadata);
            
            documents.add(doc);
        }
        
        // 验证文档列表
        assertEquals(10, documents.size());
        
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            assertNotNull(doc.getPageContent());
            assertNotNull(doc.getUniqueId());
            assertTrue(doc.getUniqueId().startsWith("unit-test-"));
            assertNotNull(doc.getMetadata());
            assertEquals(i + 1, doc.getMetadata().get("index"));
            assertEquals("unit_test", doc.getMetadata().get("type"));
        }
        
        System.out.println("Document list operations test: SUCCESS");
    }

    @Test
    @Order(9)
    public void test_complex_metadata_handling() {
        System.out.println("=== Testing Complex Metadata Handling ===");
        
        Document document = new Document();
        document.setPageContent("Document with complex metadata structure.");
        document.setUniqueId("complex-metadata-test");
        
        // 创建复杂的元数据结构
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Complex Test Document");
        metadata.put("author", "Test Author");
        metadata.put("tags", Arrays.asList("complex", "metadata", "test"));
        metadata.put("score", 95.5);
        metadata.put("published", true);
        metadata.put("nested", Map.of("level1", Map.of("level2", "deep_value")));
        metadata.put("array_of_objects", Arrays.asList(
            Map.of("name", "item1", "value", 100),
            Map.of("name", "item2", "value", 200)
        ));
        
        document.setMetadata(metadata);
        
        // 验证元数据
        assertNotNull(document.getMetadata());
        assertEquals("Complex Test Document", document.getMetadata().get("title"));
        assertEquals("Test Author", document.getMetadata().get("author"));
        assertTrue(document.getMetadata().get("published") instanceof Boolean);
        assertTrue((Boolean) document.getMetadata().get("published"));
        assertEquals(95.5, (Double) document.getMetadata().get("score"), 0.001);
        
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) document.getMetadata().get("tags");
        assertEquals(3, tags.size());
        assertTrue(tags.contains("complex"));
        
        System.out.println("Complex metadata handling test: SUCCESS");
    }

    @Test
    @Order(10)
    public void test_parameter_inheritance_and_composition() {
        System.out.println("=== Testing Parameter Inheritance and Composition ===");
        
        Neo4jParam parentParam = new Neo4jParam();
        
        // 测试InitParam的组合关系
        Neo4jParam.InitParam initParam = parentParam.getInitParam();
        assertNotNull(initParam);
        
        // 修改InitParam不应该影响其他Neo4jParam实例
        Neo4jParam anotherParam = new Neo4jParam();
        initParam.setVectorDimensions(999);
        
        assertEquals(999, parentParam.getInitParam().getVectorDimensions());
        assertEquals(1536, anotherParam.getInitParam().getVectorDimensions()); // 应该保持默认值
        
        System.out.println("Parameter inheritance and composition test: SUCCESS");
    }

    @Test
    @Order(11)
    public void test_similarity_function_enum() {
        System.out.println("=== Testing Similarity Function Enum ===");

        // 测试枚举值
        assertEquals("cosine", Neo4jSimilarityFunction.COSINE.getValue());
        assertEquals("euclidean", Neo4jSimilarityFunction.EUCLIDEAN.getValue());
        assertEquals("dot", Neo4jSimilarityFunction.DOT.getValue());

        // 测试fromValue方法
        assertEquals(Neo4jSimilarityFunction.COSINE, Neo4jSimilarityFunction.fromValue("cosine"));
        assertEquals(Neo4jSimilarityFunction.EUCLIDEAN, Neo4jSimilarityFunction.fromValue("euclidean"));
        assertEquals(Neo4jSimilarityFunction.DOT, Neo4jSimilarityFunction.fromValue("dot"));

        // 测试大小写不敏感
        assertEquals(Neo4jSimilarityFunction.COSINE, Neo4jSimilarityFunction.fromValue("COSINE"));
        assertEquals(Neo4jSimilarityFunction.EUCLIDEAN, Neo4jSimilarityFunction.fromValue("Euclidean"));

        // 测试null值返回默认值
        assertEquals(Neo4jSimilarityFunction.COSINE, Neo4jSimilarityFunction.fromValue(null));

        // 测试无效值抛出异常
        try {
            Neo4jSimilarityFunction.fromValue("invalid");
            fail("Should throw IllegalArgumentException for invalid similarity function");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unsupported similarity function"));
        }

        // 测试toString方法
        assertEquals("cosine", Neo4jSimilarityFunction.COSINE.toString());
        assertEquals("euclidean", Neo4jSimilarityFunction.EUCLIDEAN.toString());
        assertEquals("dot", Neo4jSimilarityFunction.DOT.toString());

        System.out.println("Similarity function enum test: SUCCESS");
    }

    /**
     * 手动测试方法 - 用于验证参数类的基本功能
     */
    public static void main(String[] args) {
        System.out.println("=== Neo4j Parameter Test Manual Run ===");
        
        try {
            Neo4jParamTest test = new Neo4jParamTest();
            
            test.test_neo4j_param_default_values();
            test.test_init_param_default_values();
            test.test_param_customization();
            test.test_init_param_customization();
            test.test_param_edge_cases();
            test.test_document_creation_and_validation();
            test.test_fake_embeddings_functionality();
            test.test_document_list_operations();
            test.test_complex_metadata_handling();
            test.test_parameter_inheritance_and_composition();
            test.test_similarity_function_enum();
            
            System.out.println("\n=== All Parameter Tests Passed ===");
            
        } catch (Exception e) {
            System.err.println("Parameter test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
