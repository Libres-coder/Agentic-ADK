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
package com.alibaba.langengine.tinkerpop.vectorstore.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;


public class TinkerPopServiceTest {

    private TinkerPopService service;
    private TinkerPopClient client;
    private final String TEST_SERVER_URL = "ws://127.0.0.1:8182";

    @BeforeEach
    public void setUp() {
        client = new TinkerPopClient(TEST_SERVER_URL);
        service = new TinkerPopService(client);
    }

    @AfterEach
    public void tearDown() {
        if (service != null) {
            service.close();
        }
    }

    @Test
    public void testClientCreation() {
        assertNotNull(client);
        assertEquals(TEST_SERVER_URL, client.getServerUrl());
        assertEquals(30000, client.getConnectionTimeout());
        assertEquals(60000, client.getRequestTimeout());
    }

    @Test
    public void testClientWithCustomTimeout() {
        TinkerPopClient customClient = new TinkerPopClient(TEST_SERVER_URL, 5000, 10000);

        assertEquals(TEST_SERVER_URL, customClient.getServerUrl());
        assertEquals(5000, customClient.getConnectionTimeout());
        assertEquals(10000, customClient.getRequestTimeout());
    }

    @Test
    public void testServiceCreation() {
        assertNotNull(service);
        assertNotNull(service.getClient());
        assertEquals(client, service.getClient());
        assertFalse(service.isConnected());
    }

    @Test
    @Disabled("需要运行中的TinkerPop服务器")
    public void testConnect() {
        assertDoesNotThrow(() -> {
            service.connect();
        });

        assertTrue(service.isConnected());
    }

    @Test
    @Disabled("需要运行中的TinkerPop服务器")
    public void testAddDocuments() {
        // 先连接
        service.connect();
        assertTrue(service.isConnected());

        // 准备测试数据
        List<String> ids = Arrays.asList("test-1", "test-2");
        List<String> texts = Arrays.asList("First test document", "Second test document");
        List<List<Double>> embeddings = Arrays.asList(
            Arrays.asList(0.1, 0.2, 0.3),
            Arrays.asList(0.4, 0.5, 0.6)
        );
        List<Map<String, Object>> metadatas = Arrays.asList(
            Map.of("source", "test1.txt"),
            Map.of("source", "test2.txt")
        );

        TinkerPopAddRequest request = new TinkerPopAddRequest(
            "test_collection", ids, texts, embeddings, metadatas);

        // 执行添加操作
        assertDoesNotThrow(() -> {
            service.addDocuments(request);
        });
    }

    @Test
    @Disabled("需要运行中的TinkerPop服务器")
    public void testQueryDocuments() {
        // 先连接并添加一些文档
        service.connect();
        testAddDocuments();

        // 准备查询请求
        TinkerPopQueryRequest queryRequest = new TinkerPopQueryRequest(
            "test_collection", "test document", 5);

        // 执行查询
        TinkerPopQueryResponse response = service.queryDocuments(queryRequest);

        assertNotNull(response);
        assertNotNull(response.getIds());
        assertNotNull(response.getTexts());
        assertNotNull(response.getDistances());
        assertNotNull(response.getMetadatas());
    }

    @Test
    public void testAddRequestCreation() {
        List<String> ids = Arrays.asList("id1", "id2");
        List<String> texts = Arrays.asList("text1", "text2");
        List<List<Double>> embeddings = Arrays.asList(
            Arrays.asList(0.1, 0.2),
            Arrays.asList(0.3, 0.4)
        );
        List<Map<String, Object>> metadatas = Arrays.asList(
            Map.of("key1", "value1"),
            Map.of("key2", "value2")
        );

        TinkerPopAddRequest request = new TinkerPopAddRequest(
            "test_collection", ids, texts, embeddings, metadatas);

        assertEquals("test_collection", request.getCollectionName());
        assertEquals(ids, request.getIds());
        assertEquals(texts, request.getTexts());
        assertEquals(embeddings, request.getEmbeddings());
        assertEquals(metadatas, request.getMetadatas());
    }

    @Test
    public void testQueryRequestCreation() {
        // 测试文本查询请求
        TinkerPopQueryRequest textRequest = new TinkerPopQueryRequest(
            "test_collection", "test query", 10);

        assertEquals("test_collection", textRequest.getCollectionName());
        assertEquals("test query", textRequest.getQueryText());
        assertEquals(10, textRequest.getK());

        // 测试向量查询请求
        List<Double> embedding = Arrays.asList(0.1, 0.2, 0.3);
        TinkerPopQueryRequest embeddingRequest = new TinkerPopQueryRequest(
            "test_collection", embedding, 5);

        assertEquals("test_collection", embeddingRequest.getCollectionName());
        assertEquals(embedding, embeddingRequest.getQueryEmbedding());
        assertEquals(5, embeddingRequest.getK());
    }

    @Test
    public void testQueryResponseCreation() {
        List<String> ids = Arrays.asList("id1", "id2");
        List<String> texts = Arrays.asList("text1", "text2");
        List<Double> distances = Arrays.asList(0.1, 0.2);
        List<Map<String, Object>> metadatas = Arrays.asList(
            Map.of("key1", "value1"),
            Map.of("key2", "value2")
        );

        TinkerPopQueryResponse response = new TinkerPopQueryResponse(
            ids, texts, distances, metadatas);

        assertEquals(ids, response.getIds());
        assertEquals(texts, response.getTexts());
        assertEquals(distances, response.getDistances());
        assertEquals(metadatas, response.getMetadatas());
    }

    @Test
    public void testConnectionFailureHandling() {
        // 测试无效服务器URL的连接失败处理
        TinkerPopClient invalidClient = new TinkerPopClient("ws://invalid-host:9999");
        TinkerPopService invalidService = new TinkerPopService(invalidClient);

        assertThrows(RuntimeException.class, () -> {
            invalidService.connect();
        });

        assertFalse(invalidService.isConnected());
    }

    @Test
    public void testClose() {
        // 测试关闭操作不会抛出异常
        assertDoesNotThrow(() -> {
            service.close();
        });

        assertFalse(service.isConnected());
    }
}