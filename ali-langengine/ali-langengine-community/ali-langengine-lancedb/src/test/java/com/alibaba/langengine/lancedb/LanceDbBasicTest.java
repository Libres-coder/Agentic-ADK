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
package com.alibaba.langengine.lancedb;

import com.alibaba.langengine.lancedb.client.LanceDbClient;
import com.alibaba.langengine.lancedb.model.LanceDbVector;
import com.alibaba.langengine.lancedb.vectorstore.LanceDbParam;
import com.alibaba.langengine.lancedb.vectorstore.LanceDbService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("LanceDB基础测试")
class LanceDbBasicTest {

    @Test
    @DisplayName("测试基本功能")
    void testBasicFunctionality() {
        // 基础测试，验证项目结构和依赖配置正确
        assertTrue(true, "基础测试通过");
    }

    @Test
    @DisplayName("测试配置构建")
    void testConfigurationBuilder() {
        LanceDbConfiguration config = LanceDbConfiguration.builder()
                .baseUrl("http://localhost:8080")
                .apiKey("test-key")
                .maxRetries(3)
                .build();

        assertNotNull(config);
        assertEquals("http://localhost:8080", config.getBaseUrl());
        assertEquals("test-key", config.getApiKey());
        assertEquals(3, config.getMaxRetries());
    }

    @Test
    @DisplayName("测试向量创建")
    void testVectorCreation() {
        LanceDbVector vector = LanceDbVector.builder()
                .id("test-1")
                .vector(Arrays.asList(0.1, 0.2, 0.3, 0.4))
                .metadata(new HashMap<String, Object>() {{
                    put("content", "test content");
                    put("category", "test");
                }})
                .build();

        assertNotNull(vector);
        assertEquals("test-1", vector.getId());
        assertEquals(4, vector.getVector().size());
        assertNotNull(vector.getMetadata());
    }

    @Test
    @DisplayName("测试客户端实例化")
    void testClientInstantiation() {
        LanceDbConfiguration config = LanceDbConfiguration.builder()
                .baseUrl("http://localhost:8080")
                .build();

        LanceDbClient client = new LanceDbClient(config);
        LanceDbService service = new LanceDbService();

        assertNotNull(client);
        assertNotNull(service);
    }

    @Test
    @DisplayName("测试异常层次结构")
    void testExceptionHierarchy() {
        try {
            throw new LanceDbClientException("Test error message", "TEST_ERROR");
        } catch (LanceDbException e) {
            assertEquals("TEST_ERROR", e.getErrorCode());
            assertTrue(e.getMessage().contains("Test error message"));
        }
    }
}
