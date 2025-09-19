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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TinkerPopServiceMockTest {

    private TinkerPopClient client;
    private TinkerPopService service;
    private final String TEST_SERVER_URL = "ws://test-server:8182";

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
    public void testServiceCreation() {
        assertNotNull(service);
        assertEquals(client, service.getClient());
        assertFalse(service.isConnected());
        assertNull(service.getCluster());
        assertNull(service.getG());
    }

    @Test
    public void testClientGetters() {
        assertEquals(TEST_SERVER_URL, service.getClient().getServerUrl());
        assertEquals(30000, service.getClient().getConnectionTimeout());
        assertEquals(60000, service.getClient().getRequestTimeout());
    }

    @Test
    public void testClientCreationWithCustomTimeouts() {
        TinkerPopClient customClient = new TinkerPopClient(TEST_SERVER_URL, 5000, 10000);
        assertEquals(TEST_SERVER_URL, customClient.getServerUrl());
        assertEquals(5000, customClient.getConnectionTimeout());
        assertEquals(10000, customClient.getRequestTimeout());
    }

    @Test
    public void testExtractHostFromUrl() {
        assertEquals("test-server", invokeExtractHost("ws://test-server:8182"));
        assertEquals("localhost", invokeExtractHost("wss://localhost:8183"));
        assertEquals("example.com", invokeExtractHost("example.com:9999"));
        assertEquals("invalid-url", invokeExtractHost("invalid-url")); // 没有冒号时返回原始 URL
    }

    @Test
    public void testExtractPortFromUrl() {
        assertEquals(8182, invokeExtractPort("ws://test-server:8182"));
        assertEquals(8183, invokeExtractPort("wss://localhost:8183"));
        assertEquals(9999, invokeExtractPort("example.com:9999"));
        assertEquals(8182, invokeExtractPort("invalid-url"));
        assertEquals(8182, invokeExtractPort("no-port-url"));
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
    public void testRequestsWithNullValues() {
        // 测试请求对象处理 null 值
        TinkerPopAddRequest requestWithNulls = new TinkerPopAddRequest();
        requestWithNulls.setCollectionName("test");
        requestWithNulls.setIds(null);
        requestWithNulls.setTexts(null);
        requestWithNulls.setEmbeddings(null);
        requestWithNulls.setMetadatas(null);

        assertNull(requestWithNulls.getIds());
        assertNull(requestWithNulls.getTexts());
        assertNull(requestWithNulls.getEmbeddings());
        assertNull(requestWithNulls.getMetadatas());

        TinkerPopQueryRequest queryWithNulls = new TinkerPopQueryRequest();
        queryWithNulls.setCollectionName("test");
        queryWithNulls.setQueryText(null);
        queryWithNulls.setQueryEmbedding(null);

        assertNull(queryWithNulls.getQueryText());
        assertNull(queryWithNulls.getQueryEmbedding());
    }

    @Test
    public void testEmptyListsHandling() {
        // 测试空列表处理
        TinkerPopAddRequest emptyRequest = new TinkerPopAddRequest(
            "test_collection",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );

        assertTrue(emptyRequest.getIds().isEmpty());
        assertTrue(emptyRequest.getTexts().isEmpty());
        assertTrue(emptyRequest.getEmbeddings().isEmpty());
        assertTrue(emptyRequest.getMetadatas().isEmpty());
    }

    @Test
    public void testLargeDataHandling() {
        // 测试大数据量处理
        List<String> largeIds = new ArrayList<>();
        List<String> largeTexts = new ArrayList<>();
        List<List<Double>> largeEmbeddings = new ArrayList<>();
        List<Map<String, Object>> largeMetadatas = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            largeIds.add("id" + i);
            largeTexts.add("text content " + i);
            largeEmbeddings.add(Arrays.asList((double) i, (double) i + 1));
            largeMetadatas.add(Map.of("index", String.valueOf(i)));
        }

        TinkerPopAddRequest largeRequest = new TinkerPopAddRequest(
            "large_collection", largeIds, largeTexts, largeEmbeddings, largeMetadatas);

        assertEquals(1000, largeRequest.getIds().size());
        assertEquals(1000, largeRequest.getTexts().size());
        assertEquals(1000, largeRequest.getEmbeddings().size());
        assertEquals(1000, largeRequest.getMetadatas().size());
    }

    @Test
    public void testCloseIsSafe() {
        // 测试关闭操作是安全的
        assertDoesNotThrow(() -> service.close());
        assertFalse(service.isConnected());

        // 多次关闭应该是安全的
        assertDoesNotThrow(() -> service.close());
    }

    @Test
    public void testResponseExecutionTime() {
        TinkerPopQueryResponse response = new TinkerPopQueryResponse();
        long currentTime = System.currentTimeMillis();
        response.setExecutionTime(currentTime);

        assertEquals(currentTime, response.getExecutionTime());
    }

    @Test
    public void testQueryRequestParameters() {
        TinkerPopQueryRequest request = new TinkerPopQueryRequest();
        request.setCollectionName("test");
        request.setQueryText("query");
        request.setK(10);
        request.setMaxDistance(0.5);
        request.setType(1);

        assertEquals("test", request.getCollectionName());
        assertEquals("query", request.getQueryText());
        assertEquals(10, request.getK());
        assertEquals(0.5, request.getMaxDistance());
        assertEquals(Integer.valueOf(1), request.getType());
    }

    // 使用反射调用私有方法进行测试
    private String invokeExtractHost(String serverUrl) {
        try {
            java.lang.reflect.Method method = TinkerPopService.class.getDeclaredMethod("extractHost", String.class);
            method.setAccessible(true);
            return (String) method.invoke(service, serverUrl);
        } catch (Exception e) {
            fail("Failed to invoke extractHost method");
            return null;
        }
    }

    private int invokeExtractPort(String serverUrl) {
        try {
            java.lang.reflect.Method method = TinkerPopService.class.getDeclaredMethod("extractPort", String.class);
            method.setAccessible(true);
            return (Integer) method.invoke(service, serverUrl);
        } catch (Exception e) {
            fail("Failed to invoke extractPort method");
            return -1;
        }
    }
}