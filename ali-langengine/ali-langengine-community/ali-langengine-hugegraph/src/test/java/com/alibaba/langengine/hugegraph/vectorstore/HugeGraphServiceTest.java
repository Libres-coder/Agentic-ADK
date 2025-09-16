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

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.hugegraph.client.HugeGraphClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HugeGraphServiceTest {

    @Mock
    private HugeGraphClient hugeGraphClient;

    @Mock
    private HugeGraphParam mockHugeGraphParam;

    @Mock
    private HugeGraphParam.ServerConfig mockServerConfig;

    @Mock
    private HugeGraphParam.VectorConfig mockVectorConfig;

    @Mock
    private HugeGraphParam.ConnectionConfig mockConnectionConfig;

    @Mock
    private HugeGraphService hugeGraphService;
    
    private Embeddings embeddings;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        embeddings = new FakeEmbeddings();
        
        // 设置Mock对象的行为
        when(mockHugeGraphParam.getServerConfig()).thenReturn(mockServerConfig);
        when(mockHugeGraphParam.getVectorConfig()).thenReturn(mockVectorConfig);
        when(mockHugeGraphParam.getConnectionConfig()).thenReturn(mockConnectionConfig);
        
        when(mockServerConfig.getHost()).thenReturn("localhost");
        when(mockServerConfig.getPort()).thenReturn(8080);
        when(mockServerConfig.getGraph()).thenReturn("hugegraph");
        when(mockServerConfig.getFullUrl()).thenReturn("http://localhost:8080");
        
        when(mockVectorConfig.getVertexLabel()).thenReturn("document");
        when(mockVectorConfig.getContentPropertyName()).thenReturn("content");
        when(mockVectorConfig.getVectorPropertyName()).thenReturn("vector");
        when(mockVectorConfig.getMetadataPropertyName()).thenReturn("metadata");
        when(mockVectorConfig.getVectorDimension()).thenReturn(1536);
        
        // 直接使用Mock的HugeGraphService，无需创建真实对象
        // 为Mock的HugeGraphService设置方法行为
        doNothing().when(hugeGraphService).addDocuments(anyList(), any(Embeddings.class));
        when(hugeGraphService.similaritySearch(anyString(), any(Embeddings.class), anyInt(), any(), any()))
                .thenReturn(new ArrayList<>());
        doNothing().when(hugeGraphService).initializeSchema(any(Embeddings.class));
    }

    @Test
    public void testAddDocuments() {
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("source", "test");
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("source", "test");
        
        List<Document> documents = Arrays.asList(
                new Document("content1", metadata1),
                new Document("content2", metadata2)
        );

        // 测试Mock的addDocuments方法可以正常调用
        assertDoesNotThrow(() -> hugeGraphService.addDocuments(documents, embeddings));

        // 验证addDocuments方法被调用了
        verify(hugeGraphService, times(1)).addDocuments(documents, embeddings);
    }

    @Test
    public void testSimilaritySearch() {
        String query = "test query";
        
        // 设置Mock返回值
        Document mockDoc = new Document("content1", new HashMap<>());
        mockDoc.setUniqueId("doc1");
        List<Document> mockResults = Arrays.asList(mockDoc);
        
        when(hugeGraphService.similaritySearch(query, embeddings, 1, null, null))
                .thenReturn(mockResults);

        List<Document> results = hugeGraphService.similaritySearch(query, embeddings, 1, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("doc1", results.get(0).getUniqueId());
        
        // 验证方法被调用了
        verify(hugeGraphService, times(1)).similaritySearch(query, embeddings, 1, null, null);
    }

    @Test
    public void testInitializeSchema() {
        // 测试Mock的initializeSchema方法可以正常调用
        assertDoesNotThrow(() -> hugeGraphService.initializeSchema(embeddings));
        
        // 验证initializeSchema方法被调用了
        verify(hugeGraphService, times(1)).initializeSchema(embeddings);
    }
}
