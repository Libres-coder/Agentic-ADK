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
public class Neo4jUnitTest {

    @Test
    @Order(1)
    public void test_neo4j_param_creation() {
        System.out.println("=== Testing Neo4j Parameter Creation ===");
        
        Neo4jParam param = new Neo4jParam();
        
        // 验证默认值
        assertEquals("Document", param.getNodeLabel());
        assertEquals("vector_index", param.getVectorIndexName());
        assertEquals("id", param.getFieldNameUniqueId());
        assertEquals("content", param.getFieldNamePageContent());
        assertEquals("embedding", param.getFieldNameEmbedding());
        assertEquals("metadata", param.getFieldNameMetadata());
        
        // 验证初始化参数
        Neo4jParam.InitParam initParam = param.getInitParam();
        assertNotNull(initParam);
        assertEquals(1536, initParam.getVectorDimensions());
        assertEquals(Neo4jSimilarityFunction.COSINE, initParam.getSimilarityFunction());
        assertEquals(16, initParam.getHnswM());
        assertEquals(200, initParam.getHnswEfConstruction());
        assertTrue(initParam.isAutoCreateIndex());
        assertTrue(initParam.isAutoCreateNode());
        assertEquals(1000, initParam.getBatchSize());
        
        System.out.println("Neo4j parameter creation test: SUCCESS");
    }

    @Test
    @Order(2)
    public void test_neo4j_param_customization() {
        System.out.println("=== Testing Neo4j Parameter Customization ===");
        
        Neo4jParam param = new Neo4jParam();
        
        // 自定义参数
        param.setNodeLabel("CustomDocument");
        param.setVectorIndexName("custom_vector_index");
        param.setFieldNameUniqueId("custom_id");
        param.setFieldNamePageContent("custom_content");
        param.setFieldNameEmbedding("custom_embedding");
        param.setFieldNameMetadata("custom_metadata");
        
        // 自定义初始化参数
        Neo4jParam.InitParam initParam = param.getInitParam();
        initParam.setVectorDimensions(768);
        initParam.setSimilarityFunction(Neo4jSimilarityFunction.EUCLIDEAN);
        initParam.setHnswM(32);
        initParam.setHnswEfConstruction(400);
        initParam.setAutoCreateIndex(false);
        initParam.setAutoCreateNode(false);
        initParam.setBatchSize(500);
        
        // 验证自定义值
        assertEquals("CustomDocument", param.getNodeLabel());
        assertEquals("custom_vector_index", param.getVectorIndexName());
        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_embedding", param.getFieldNameEmbedding());
        assertEquals("custom_metadata", param.getFieldNameMetadata());
        
        assertEquals(768, initParam.getVectorDimensions());
        assertEquals(Neo4jSimilarityFunction.EUCLIDEAN, initParam.getSimilarityFunction());
        assertEquals(32, initParam.getHnswM());
        assertEquals(400, initParam.getHnswEfConstruction());
        assertFalse(initParam.isAutoCreateIndex());
        assertFalse(initParam.isAutoCreateNode());
        assertEquals(500, initParam.getBatchSize());
        
        System.out.println("Neo4j parameter customization test: SUCCESS");
    }

    @Test
    @Order(3)
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
    @Order(4)
    public void test_fake_embeddings_integration() {
        System.out.println("=== Testing FakeEmbeddings Integration ===");
        
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
        
        // 测试查询嵌入
        List<String> queryEmbeddings = fakeEmbeddings.embedQuery("test query", 5);
        assertNotNull(queryEmbeddings);
        
        System.out.println("FakeEmbeddings integration test: SUCCESS");
    }

    @Test
    @Order(5)
    public void test_neo4j_param_object_creation() {
        System.out.println("=== Testing Neo4j Parameter Object Creation ===");

        // 测试自定义参数对象创建
        Neo4jParam customParam = new Neo4jParam();
        customParam.setNodeLabel("TestNode");
        customParam.setVectorIndexName("test_vector_index");
        customParam.setFieldNameUniqueId("test_id");

        assertNotNull(customParam);
        assertEquals("TestNode", customParam.getNodeLabel());
        assertEquals("test_vector_index", customParam.getVectorIndexName());
        assertEquals("test_id", customParam.getFieldNameUniqueId());

        System.out.println("Neo4j parameter object creation test: SUCCESS");
    }

    @Test
    @Order(6)
    public void test_embedding_integration_without_connection() {
        System.out.println("=== Testing Embedding Integration (No Connection) ===");

        FakeEmbeddings embeddings = new FakeEmbeddings();

        // 测试文档嵌入
        List<Document> documents = new ArrayList<>();
        Document doc = new Document();
        doc.setPageContent("Test document for embedding");
        doc.setUniqueId("embed-test");
        documents.add(doc);

        // 这里只测试嵌入逻辑，不涉及数据库连接
        List<Document> embeddedDocs = embeddings.embedDocument(documents);
        assertNotNull(embeddedDocs);

        // 测试查询嵌入
        List<String> queryEmbeddings = embeddings.embedQuery("test query", 5);
        assertNotNull(queryEmbeddings);

        System.out.println("Embedding integration test: SUCCESS");
    }

    @Test
    @Order(7)
    public void test_search_params_configuration() {
        System.out.println("=== Testing Search Parameters Configuration ===");
        
        Neo4jParam param = new Neo4jParam();
        
        // 验证默认搜索参数
        Map<String, Object> searchParams = param.getSearchParams();
        assertNotNull(searchParams);
        assertTrue(searchParams.containsKey("ef"));
        assertEquals(64, searchParams.get("ef"));
        
        // 自定义搜索参数
        Map<String, Object> customSearchParams = new HashMap<>();
        customSearchParams.put("ef", 128);
        customSearchParams.put("custom_param", "custom_value");
        param.setSearchParams(customSearchParams);
        
        assertEquals(128, param.getSearchParams().get("ef"));
        assertEquals("custom_value", param.getSearchParams().get("custom_param"));
        
        System.out.println("Search parameters configuration test: SUCCESS");
    }

    @Test
    @Order(8)
    public void test_field_name_configuration() {
        System.out.println("=== Testing Field Name Configuration ===");
        
        Neo4jParam param = new Neo4jParam();
        
        // 测试字段名称设置
        param.setFieldNameUniqueId("custom_id");
        param.setFieldNamePageContent("custom_content");
        param.setFieldNameEmbedding("custom_embedding");
        param.setFieldNameMetadata("custom_metadata");
        
        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_embedding", param.getFieldNameEmbedding());
        assertEquals("custom_metadata", param.getFieldNameMetadata());
        
        System.out.println("Field name configuration test: SUCCESS");
    }

    @Test
    @Order(9)
    public void test_init_param_edge_cases() {
        System.out.println("=== Testing Init Parameter Edge Cases ===");
        
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
        
        // 测试相似性函数
        initParam.setSimilarityFunction(Neo4jSimilarityFunction.EUCLIDEAN);
        assertEquals(Neo4jSimilarityFunction.EUCLIDEAN, initParam.getSimilarityFunction());

        initParam.setSimilarityFunction(Neo4jSimilarityFunction.COSINE);
        assertEquals(Neo4jSimilarityFunction.COSINE, initParam.getSimilarityFunction());
        
        System.out.println("Init parameter edge cases test: SUCCESS");
    }

    @Test
    @Order(10)
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
        }
        
        System.out.println("Document list operations test: SUCCESS");
    }

    /**
     * 手动测试方法 - 用于验证类的基本功能
     */
    public static void main(String[] args) {
        System.out.println("=== Neo4j Unit Test Manual Run ===");

        try {
            Neo4jUnitTest test = new Neo4jUnitTest();

            test.test_neo4j_param_creation();
            test.test_neo4j_param_customization();
            test.test_document_creation_and_validation();
            test.test_fake_embeddings_integration();
            test.test_neo4j_param_object_creation();
            test.test_embedding_integration_without_connection();
            test.test_search_params_configuration();
            test.test_field_name_configuration();
            test.test_init_param_edge_cases();
            test.test_document_list_operations();

            System.out.println("\n=== All Unit Tests Passed ===");

        } catch (Exception e) {
            System.err.println("Unit test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
