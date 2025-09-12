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
import com.alibaba.langengine.lancedb.vectorstore.LanceDbParam;
import com.alibaba.langengine.lancedb.model.LanceDbVector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("LanceDB向量存储测试")
class LanceDbVectorStoreTest {

    private LanceDbParam param;

    @BeforeEach
    void setUp() {
        LanceDbConfiguration config = LanceDbConfiguration.builder()
                .baseUrl("http://localhost:8080")
                .build();
        
        param = LanceDbParam.builder().build();
    }

    @Test
    @DisplayName("测试参数初始化")
    void testParameterInitialization() {
        assertNotNull(param);
    }

    @Test
    @DisplayName("测试向量对象创建")
    void testVectorCreation() {
        LanceDbVector vector = LanceDbVector.builder()
                .id("test-vector-1")
                .vector(Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5))
                .metadata(new HashMap<String, Object>() {{
                    put("content", "test content");
                    put("category", "test");
                }})
                .build();

        assertNotNull(vector);
        assertEquals("test-vector-1", vector.getId());
        assertEquals(5, vector.getVector().size());
        assertNotNull(vector.getMetadata());
    }

    @Test
    @DisplayName("测试向量验证")
    void testVectorValidation() {
        // 测试向量创建
        LanceDbVector vector = LanceDbVector.builder()
                .id("valid-vector")
                .vector(Arrays.asList(1.0, 2.0, 3.0))
                .build();

        assertNotNull(vector);
        assertTrue(vector.getVector().size() > 0);
        
        // 测试空向量
        LanceDbVector emptyVector = LanceDbVector.builder()
                .id("empty-vector")
                .vector(Arrays.asList())
                .build();
                
        assertNotNull(emptyVector);
        assertEquals(0, emptyVector.getVector().size());
    }
}
