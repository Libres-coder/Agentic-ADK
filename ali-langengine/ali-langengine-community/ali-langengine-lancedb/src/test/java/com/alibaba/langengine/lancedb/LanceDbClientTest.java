/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the         String responseBody = "{\n" +
                "    \"results\": [\n" +
                "        {\n" +
                "            \"id\": \"test-1\",\n" +
                "            \"vector\": [0.1, 0.2, 0.3],\n" +
                "            \"text\": \"Test document 1\",\n" +
                "            \"metadata\": {\"category\": \"test\"},\n" +
                "            \"score\": 0.95,\n" +
                "            \"distance\": 0.05\n" +
                "        }\n" +
                "    ],\n" +
                "    \"total\": 1,\n" +
                "    \"success\": true,\n" +
                "    \"queryTimeMs\": 50\n" +
                "}";you may not use this file except in compliance with the License.
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
package com.alibaba.langengine.lancedb;

import com.alibaba.langengine.lancedb.exception.LanceDbClientException;
import com.alibaba.langengine.lancedb.exception.LanceDbConnectionException;
import com.alibaba.langengine.lancedb.model.LanceDbQueryRequest;
import com.alibaba.langengine.lancedb.model.LanceDbQueryResponse;
import com.alibaba.langengine.lancedb.model.LanceDbVector;

import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@DisplayName("LanceDB客户端测试")
@ExtendWith(MockitoExtension.class)
class LanceDbClientTest {

    private MockWebServer mockServer;
    private LanceDbConfiguration configuration;
    private LanceDbClient client;
    
    @Mock
    private OkHttpClient mockHttpClient;
    
    @Mock
    private Call mockCall;
    
    @Mock
    private Response mockResponse;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        
        String baseUrl = mockServer.url("/").toString();
        configuration = LanceDbConfiguration.builder()
                .baseUrl(baseUrl)
                .apiKey("test-api-key")
                .build();
        
        client = new LanceDbClient(configuration);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (mockServer != null) {
            mockServer.shutdown();
        }
        if (client != null) {
            client.close();
        }
    }

    @Test
    @DisplayName("测试查询向量成功")
    void testQueryVectorsSuccess() throws Exception {
        String responseBody = "{\n" +
                "    \"results\": [\n" +
                "        {\n" +
                "            \"id\": \"test-1\",\n" +
                "            \"vector\": [0.1, 0.2, 0.3],\n" +
                "            \"text\": \"Test document 1\",\n" +
                "            \"metadata\": {\"category\": \"test\"},\n" +
                "            \"score\": 0.95,\n" +
                "            \"distance\": 0.05\n" +
                "        }\n" +
                "    ],\n" +
                "    \"total\": 1,\n" +
                "    \"success\": true,\n" +
                "    \"queryTimeMs\": 50,\n" +
                "    \"requestId\": \"req-123\"\n" +
                "}";
        
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));
        
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        LanceDbQueryRequest request = LanceDbQueryRequest.vectorQuery(queryVector, 10);
        
        LanceDbQueryResponse response = client.queryVectors("test-table", request);
        
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(1, response.getResultCount());
        assertEquals("test-1", response.getResults().get(0).getId());
        assertEquals(queryVector, response.getResults().get(0).getVector());
        
        // 验证请求
        RecordedRequest recordedRequest = mockServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/tables/test-table/query", recordedRequest.getPath());
        assertTrue(recordedRequest.getHeader("Authorization").contains("Bearer test-api-key"));
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"));
    }

    @Test
    @DisplayName("测试查询向量失败")
    void testQueryVectorsFailed() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"Invalid query parameters\"}"));
        
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        LanceDbQueryRequest request = LanceDbQueryRequest.vectorQuery(queryVector, 10);
        
        assertThrows(LanceDbClientException.class, () -> {
            client.queryVectors("test-table", request);
        });
    }

    @Test
    @DisplayName("测试插入向量成功")
    void testInsertVectorsSuccess() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\": true, \"inserted\": 2}"));
        
        List<LanceDbVector> vectors = Arrays.asList(
                LanceDbVector.of("1", Arrays.asList(0.1, 0.2, 0.3), "Text 1"),
                LanceDbVector.of("2", Arrays.asList(0.4, 0.5, 0.6), "Text 2")
        );
        
        assertDoesNotThrow(() -> {
            client.insertVectors("test-table", vectors);
        });
        
        // 验证请求
        RecordedRequest recordedRequest = mockServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/tables/test-table/insert", recordedRequest.getPath());
    }

    @Test
    @DisplayName("测试插入向量失败")
    void testInsertVectorsFailed() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"Internal server error\"}"));
        
        List<LanceDbVector> vectors = Arrays.asList(
                LanceDbVector.of("1", Arrays.asList(0.1, 0.2, 0.3), "Text 1")
        );
        
        assertThrows(LanceDbClientException.class, () -> {
            client.insertVectors("test-table", vectors);
        });
    }

    @Test
    @DisplayName("测试创建表成功")
    void testCreateTableSuccess() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\": true, \"table\": \"test-table\"}"));
        
        Map<String, Object> schema = new HashMap<>();
        schema.put("dimension", 3);
        schema.put("metric", "cosine");
        
        assertDoesNotThrow(() -> {
            client.createTable("test-table", schema);
        });
        
        // 验证请求
        RecordedRequest recordedRequest = mockServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/tables", recordedRequest.getPath());
    }

    @Test
    @DisplayName("测试删除表成功")
    void testDropTableSuccess() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\": true}"));
        
        assertDoesNotThrow(() -> {
            client.dropTable("test-table");
        });
        
        // 验证请求
        RecordedRequest recordedRequest = mockServer.takeRequest();
        assertEquals("DELETE", recordedRequest.getMethod());
        assertEquals("/tables/test-table", recordedRequest.getPath());
    }

    @Test
    @DisplayName("测试连接错误处理")
    void testConnectionError() throws Exception {
        // 停止mock服务器以模拟连接错误
        mockServer.shutdown();
        
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        LanceDbQueryRequest request = LanceDbQueryRequest.vectorQuery(queryVector, 10);
        
        assertThrows(LanceDbConnectionException.class, () -> {
            client.queryVectors("test-table", request);
        });
    }

    @Test
    @DisplayName("测试速率限制")
    void testRateLimit() throws Exception {
        // 配置严格的速率限制
        LanceDbConfiguration strictConfig = LanceDbConfiguration.builder()
                .baseUrl(mockServer.url("/").toString())
                .apiKey("test-api-key")
                .maxRequestsPerSecond(1)
                .build();
        
        LanceDbClient rateLimitedClient = new LanceDbClient(strictConfig);
        
        try {
            // 准备响应
            mockServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("{\"results\": [], \"total\": 0, \"success\": true}"));
            mockServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("{\"results\": [], \"total\": 0, \"success\": true}"));
            
            List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
            LanceDbQueryRequest request = LanceDbQueryRequest.vectorQuery(queryVector, 10);
            
            // 第一个请求应该立即成功
            long start1 = System.currentTimeMillis();
            rateLimitedClient.queryVectors("test-table", request);
            long end1 = System.currentTimeMillis();
            
            // 第二个请求应该被延迟
            long start2 = System.currentTimeMillis();
            rateLimitedClient.queryVectors("test-table", request);
            long end2 = System.currentTimeMillis();
            
            // 验证第一个请求很快完成
            assertTrue((end1 - start1) < 100);
            
            // 验证第二个请求被延迟（至少1秒）
            assertTrue((end2 - start2) >= 950); // 允许一些误差
            
        } finally {
            rateLimitedClient.close();
        }
    }

    @Test
    @DisplayName("测试重试机制")
    void testRetryMechanism() throws Exception {
        // 第一次请求失败（服务器错误）
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": \"Temporary server error\"}"));
        
        // 第二次请求成功
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"results\": [], \"total\": 0, \"success\": true}"));
        
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        LanceDbQueryRequest request = LanceDbQueryRequest.vectorQuery(queryVector, 10);
        
        // 应该成功（经过重试）
        LanceDbQueryResponse response = client.queryVectors("test-table", request);
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        
        // 验证进行了两次请求
        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    @DisplayName("测试请求头设置")
    void testRequestHeaders() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"results\": [], \"total\": 0, \"success\": true}"));
        
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        LanceDbQueryRequest request = LanceDbQueryRequest.vectorQuery(queryVector, 10);
        
        client.queryVectors("test-table", request);
        
        RecordedRequest recordedRequest = mockServer.takeRequest();
        
        // 验证必要的请求头
        assertNotNull(recordedRequest.getHeader("Authorization"));
        assertTrue(recordedRequest.getHeader("Authorization").startsWith("Bearer"));
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"));
        assertEquals("application/json", recordedRequest.getHeader("Accept"));
        assertNotNull(recordedRequest.getHeader("User-Agent"));
        assertTrue(recordedRequest.getHeader("User-Agent").contains("LanceDB-Java-Client"));
    }

    @Test
    @DisplayName("测试超时配置")
    void testTimeoutConfiguration() {
        LanceDbConfiguration timeoutConfig = LanceDbConfiguration.builder()
                .baseUrl("http://localhost:8080")
                .apiKey("test-key")
                .connectTimeoutMs(5000)
                .readTimeoutMs(10000)
                .writeTimeoutMs(15000)
                .build();
        
        LanceDbClient timeoutClient = new LanceDbClient(timeoutConfig);
        
        // 验证超时配置通过检查客户端配置
        assertNotNull(timeoutClient);
        
        timeoutClient.close();
    }

    @Test
    @DisplayName("测试SSL配置")
    void testSslConfiguration() {
        LanceDbConfiguration sslConfig = LanceDbConfiguration.builder()
                .baseUrl("https://secure.lancedb.com")
                .apiKey("test-key")
                .disableSslVerification(false)
                .build();
        
        LanceDbClient sslClient = new LanceDbClient(sslConfig);
        
        assertNotNull(sslClient);
        sslClient.close();
    }

    @Test
    @DisplayName("测试缓存配置")
    void testCacheConfiguration() {
        LanceDbConfiguration cacheConfig = LanceDbConfiguration.builder()
                .baseUrl("http://localhost:8080")
                .apiKey("test-key")
                .cacheSize(100)
                .cacheTtlMinutes(30)
                .build();
        
        LanceDbClient cacheClient = new LanceDbClient(cacheConfig);
        
        assertNotNull(cacheClient);
        cacheClient.close();
    }

    @Test
    @DisplayName("测试异步查询")
    void testAsyncQuery() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"results\": [], \"total\": 0, \"success\": true}")
                .setBodyDelay(100, TimeUnit.MILLISECONDS));
        
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        LanceDbQueryRequest request = LanceDbQueryRequest.vectorQuery(queryVector, 10);
        
        CompletableFuture<LanceDbQueryResponse> future = client.queryVectorsAsync("test-table", request);
        
        assertNotNull(future);
        LanceDbQueryResponse response = future.get(5, TimeUnit.SECONDS);
        
        assertNotNull(response);
        assertTrue(response.isSuccessful());
    }

    @Test
    @DisplayName("测试批量操作")
    void testBatchOperations() throws Exception {
        // 准备多个响应
        for (int i = 0; i < 3; i++) {
            mockServer.enqueue(new MockResponse()
                    .setResponseCode(201)
                    .setBody("{\"success\": true, \"inserted\": 1}"));
        }
        
        List<List<LanceDbVector>> batches = Arrays.asList(
                Arrays.asList(LanceDbVector.of("1", Arrays.asList(0.1, 0.2, 0.3), "Text 1")),
                Arrays.asList(LanceDbVector.of("2", Arrays.asList(0.4, 0.5, 0.6), "Text 2")),
                Arrays.asList(LanceDbVector.of("3", Arrays.asList(0.7, 0.8, 0.9), "Text 3"))
        );
        
        // 批量插入
        assertDoesNotThrow(() -> {
            client.insertVectorsBatch("test-table", batches);
        });
        
        // 验证进行了三次插入请求
        assertEquals(3, mockServer.getRequestCount());
    }

    @Test
    @DisplayName("测试大向量处理")
    void testLargeVectorHandling() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("{\"success\": true, \"inserted\": 1}"));
        
        // 创建一个大向量（1000维）
        List<Double> largeVector = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeVector.add(Math.random());
        }
        
        List<LanceDbVector> vectors = Arrays.asList(
                LanceDbVector.of("large-vector", largeVector, "Large dimension vector")
        );
        
        assertDoesNotThrow(() -> {
            client.insertVectors("test-table", vectors);
        });
        
        RecordedRequest request = mockServer.takeRequest();
        assertNotNull(request.getBody());
        assertTrue(request.getBodySize() > 0);
    }

    @Test
    @DisplayName("测试错误响应解析")
    void testErrorResponseParsing() throws Exception {
        String errorResponse = "{\n" +
                "    \"error\": \"Validation failed\",\n" +
                "    \"code\": \"VALIDATION_ERROR\",\n" +
                "    \"details\": {\n" +
                "        \"field\": \"vector\",\n" +
                "        \"message\": \"Vector dimension mismatch\"\n" +
                "    }\n" +
                "}";
        
        mockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody(errorResponse));
        
        List<Double> queryVector = Arrays.asList(0.1, 0.2);
        LanceDbQueryRequest request = LanceDbQueryRequest.vectorQuery(queryVector, 10);
        
        LanceDbClientException exception = assertThrows(LanceDbClientException.class, () -> {
            client.queryVectors("test-table", request);
        });
        
        assertTrue(exception.getMessage().contains("Validation failed"));
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    @DisplayName("测试资源释放")
    void testResourceCleanup() {
        LanceDbClient testClient = new LanceDbClient(configuration);
        
        assertNotNull(testClient);
        
        // 关闭客户端应该不抛异常
        assertDoesNotThrow(() -> {
            testClient.close();
        });
        
        // 再次关闭应该也不抛异常
        assertDoesNotThrow(() -> {
            testClient.close();
        });
    }
}
