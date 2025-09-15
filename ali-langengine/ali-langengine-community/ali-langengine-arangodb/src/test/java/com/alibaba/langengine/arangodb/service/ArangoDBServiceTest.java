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
package com.alibaba.langengine.arangodb.service;

import com.alibaba.langengine.arangodb.ArangoDBConfiguration;
import com.alibaba.langengine.arangodb.vectorstore.ArangoDBParam;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("ArangoDB 配置测试")
class ArangoDBServiceTest {
    
    private ArangoDBConfiguration configuration;
    private ArangoDBParam param;
    
    @BeforeEach
    void setUp() {
        configuration = new ArangoDBConfiguration();
        configuration.setHost("localhost");
        configuration.setPort(8529);
        configuration.setUsername("root");
        configuration.setPassword("test123");
        configuration.setDatabase("testdb");
        
        param = new ArangoDBParam();
        param.getInitParam().setDimension(384);
        param.getInitParam().setDefaultTopK(10);
        param.setFieldNamePageContent("page_content");
        param.setFieldNameUniqueId("unique_id");
    }
    
    @Test
    @DisplayName("测试ArangoDB配置")
    void testArangoDBConfiguration() {
        // 验证配置不为空
        assertNotNull(configuration);
        
        // 验证配置属性
        assertEquals("localhost", configuration.getHost());
        assertEquals(8529, configuration.getPort());
        assertEquals("root", configuration.getUsername());
        assertEquals("test123", configuration.getPassword());
        assertEquals("testdb", configuration.getDatabase());
        
        // 测试配置验证（通过异常处理来验证）
        assertDoesNotThrow(() -> configuration.validate());
    }
    
    @Test
    @DisplayName("测试Document对象")
    void testDocumentCreation() {
        // 创建测试文档
        Document document = new Document();
        document.setPageContent("Test content for ArangoDB");
        document.setUniqueId("test_doc_001");
        document.setIndex(1);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", "test");
        metadata.put("priority", "high");
        document.setMetadata(metadata);
        
        // 验证文档属性
        assertEquals("Test content for ArangoDB", document.getPageContent());
        assertEquals("test_doc_001", document.getUniqueId());
        assertEquals(Integer.valueOf(1), document.getIndex());
        assertNotNull(document.getMetadata());
        assertEquals("test", document.getMetadata().get("category"));
        assertEquals("high", document.getMetadata().get("priority"));
        assertTrue(document.hasMetadata());
    }
    
    @Test
    @DisplayName("测试向量数据结构")
    void testVectorDataStructure() {
        // 创建测试向量
        List<Double> testVector = createTestVector();
        
        // 验证向量
        assertNotNull(testVector);
        assertEquals(param.getInitParam().getDimension(), testVector.size());
        
        // 验证向量值都在合理范围内
        for (Double value : testVector) {
            assertNotNull(value);
            assertTrue(Math.abs(value) <= 2.0);
        }
    }
    
    /**
     * 创建测试向量
     */
    private List<Double> createTestVector() {
        List<Double> vector = new ArrayList<>();
        Random random = new Random(42);
        
        int dimension = param.getInitParam().getDimension();
        for (int i = 0; i < dimension; i++) {
            vector.add(random.nextGaussian() * 0.1);
        }
        
        // 归一化向量
        double norm = Math.sqrt(vector.stream().mapToDouble(x -> x * x).sum());
        if (norm > 0) {
            vector = vector.stream()
                    .map(x -> x / norm)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        
        return vector;
    }
}
