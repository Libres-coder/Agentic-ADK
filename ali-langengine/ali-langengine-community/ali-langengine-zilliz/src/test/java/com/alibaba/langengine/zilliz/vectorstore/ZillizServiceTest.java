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
package com.alibaba.langengine.zilliz.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ZillizServiceTest {

    private ZillizService zillizService;
    private FakeEmbeddings fakeEmbeddings;
    private ZillizParam zillizParam;

    @BeforeEach
    public void setUp() {
        fakeEmbeddings = new FakeEmbeddings();
        zillizParam = new ZillizParam();
        
        // Mock configuration for testing
        String clusterEndpoint = System.getenv("ZILLIZ_CLUSTER_ENDPOINT");
        String apiKey = System.getenv("ZILLIZ_API_KEY");
        String databaseName = System.getenv("ZILLIZ_DATABASE_NAME");
        
        if (clusterEndpoint != null && apiKey != null) {
            zillizService = new ZillizService(
                clusterEndpoint, 
                apiKey, 
                databaseName != null ? databaseName : "default",
                "test_service_collection",
                null,
                zillizParam
            );
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ZILLIZ_CLUSTER_ENDPOINT", matches = ".*")
    public void testInit() {
        assertDoesNotThrow(() -> zillizService.init(fakeEmbeddings));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ZILLIZ_CLUSTER_ENDPOINT", matches = ".*")
    public void testAddDocuments() {
        zillizService.init(fakeEmbeddings);
        
        List<Document> documents = Lists.newArrayList(
            createTestDocument("doc1", "Hello world"),
            createTestDocument("doc2", "How are you"),
            createTestDocument("doc3", "Good morning")
        );
        
        assertDoesNotThrow(() -> zillizService.addDocuments(documents));
    }

    @Test
    public void testAddEmptyDocuments() {
        if (zillizService != null) {
            assertDoesNotThrow(() -> zillizService.addDocuments(Lists.newArrayList()));
            assertDoesNotThrow(() -> zillizService.addDocuments(null));
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ZILLIZ_CLUSTER_ENDPOINT", matches = ".*")
    public void testSimilaritySearch() {
        zillizService.init(fakeEmbeddings);
        
        // Add test documents first
        List<Document> documents = Lists.newArrayList(
            createTestDocument("doc1", "Machine learning"),
            createTestDocument("doc2", "Deep learning")
        );
        zillizService.addDocuments(documents);
        
        // Search
        List<Float> queryEmbedding = Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f);
        List<Document> results = zillizService.similaritySearch(queryEmbedding, 2);
        
        assertNotNull(results);
        assertTrue(results.size() <= 2);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ZILLIZ_CLUSTER_ENDPOINT", matches = ".*")
    public void testDropCollection() {
        assertDoesNotThrow(() -> zillizService.dropCollection());
    }

    @Test
    public void testClose() {
        if (zillizService != null) {
            assertDoesNotThrow(() -> zillizService.close());
        }
    }

    @Test
    public void testZillizParamDefaults() {
        ZillizParam param = new ZillizParam();
        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("embeddings", param.getFieldNameEmbedding());
        assertEquals("row_content", param.getFieldNamePageContent());
        assertNotNull(param.getSearchParams());
        assertNotNull(param.getInitParam());
        
        ZillizParam.InitParam initParam = param.getInitParam();
        assertEquals(8192, initParam.getFieldPageContentMaxLength());
        assertEquals(1536, initParam.getFieldEmbeddingsDimension());
        assertEquals(2, initParam.getShardsNum());
        assertEquals(io.milvus.common.clientenum.ConsistencyLevelEnum.BOUNDED, initParam.getConsistencyLevel());
    }

    private Document createTestDocument(String id, String content) {
        Document doc = new Document(content, null);
        doc.setUniqueId(id);
        // Add fake embedding
        doc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3, 0.4));
        return doc;
    }
}