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
package com.alibaba.langengine.arangodb.vectorstore;

import com.alibaba.langengine.arangodb.vectorstore.ArangoDBVectorStore;
import com.alibaba.langengine.arangodb.ArangoDBConfiguration;
import com.alibaba.langengine.arangodb.vectorstore.ArangoDBParam;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("ArangoDBVectorStore简化测试")
public class ArangoDBVectorStoreTest {

    private ArangoDBParam param;

    @BeforeEach
    void setUp() {
        ArangoDBConfiguration configuration = new ArangoDBConfiguration();
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
    @DisplayName("测试参数配置")
    void testParameterConfiguration() {
        // 测试参数配置
        assertNotNull(param);
        assertNotNull(param.getInitParam());
        assertEquals(384, param.getInitParam().getDimension());
        assertEquals(10, param.getInitParam().getDefaultTopK());
        assertEquals("page_content", param.getFieldNamePageContent());
        assertEquals("unique_id", param.getFieldNameUniqueId());
    }

    @Test
    @DisplayName("测试文档创建")
    void testDocumentCreation() {
        List<Document> docs = Arrays.asList(createDoc("id1"), createDoc("id2"));
        
        // 验证文档创建
        assertNotNull(docs);
        assertEquals(2, docs.size());
        assertEquals("id1", docs.get(0).getUniqueId());
        assertEquals("content for id1", docs.get(0).getPageContent());
    }



    private Document createDoc(String id) {
        Document doc = new Document();
        doc.setUniqueId(id);
        doc.setPageContent("content for " + id);
        doc.setMetadata(Collections.singletonMap("meta", "test"));
        return doc;
    }
}
