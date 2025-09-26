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
package com.alibaba.langengine.rockset.vectorstore;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class RocksetParamTest {

    @Test
    public void testDefaultValues() {
        RocksetParam param = new RocksetParam();
        
        // 测试默认值
        assertNotNull(param.getInitParam());
        assertEquals("commons", param.getInitParam().getWorkspace());
        assertEquals("langengine_rockset_collection", param.getInitParam().getCollectionName());
        assertEquals("cosine", param.getInitParam().getVectorDistance());
        assertEquals(Integer.valueOf(1536), param.getInitParam().getDimension());
        assertEquals("hnsw", param.getInitParam().getIndexType());
        assertEquals(Integer.valueOf(16), param.getInitParam().getMaxConnections());
        assertEquals(Integer.valueOf(200), param.getInitParam().getEfConstruction());
        assertEquals(Integer.valueOf(10), param.getInitParam().getEfSearch());
        assertEquals("LangEngine Rockset Vector Store", param.getInitParam().getDescription());
        
        // 测试字段名默认值
        assertEquals("page_content", param.getFieldNamePageContent());
        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("meta_data", param.getFieldMeta());
        assertEquals("vector", param.getFieldNameVector());
    }

    @Test
    public void testBuilderPattern() {
        RocksetParam param = RocksetParam.builder()
                .workspace("test_workspace")
                .collectionName("test_collection")
                .vectorDistance("euclidean")
                .dimension(768)
                .indexType("flat")
                .maxConnections(32)
                .efConstruction(400)
                .efSearch(20)
                .description("Test collection")
                .fieldNamePageContent("content")
                .fieldNameUniqueId("id")
                .fieldMeta("metadata")
                .fieldNameVector("embeddings")
                .build();
        
        // 测试设置的值
        assertEquals("test_workspace", param.getInitParam().getWorkspace());
        assertEquals("test_collection", param.getInitParam().getCollectionName());
        assertEquals("euclidean", param.getInitParam().getVectorDistance());
        assertEquals(Integer.valueOf(768), param.getInitParam().getDimension());
        assertEquals("flat", param.getInitParam().getIndexType());
        assertEquals(Integer.valueOf(32), param.getInitParam().getMaxConnections());
        assertEquals(Integer.valueOf(400), param.getInitParam().getEfConstruction());
        assertEquals(Integer.valueOf(20), param.getInitParam().getEfSearch());
        assertEquals("Test collection", param.getInitParam().getDescription());
        
        assertEquals("content", param.getFieldNamePageContent());
        assertEquals("id", param.getFieldNameUniqueId());
        assertEquals("metadata", param.getFieldMeta());
        assertEquals("embeddings", param.getFieldNameVector());
    }

    @Test
    public void testBuilderChaining() {
        RocksetParam.Builder builder = RocksetParam.builder();
        
        // 测试链式调用
        RocksetParam param = builder
                .workspace("chained_workspace")
                .collectionName("chained_collection")
                .dimension(1024)
                .vectorDistance("manhattan")
                .build();
        
        assertEquals("chained_workspace", param.getInitParam().getWorkspace());
        assertEquals("chained_collection", param.getInitParam().getCollectionName());
        assertEquals(Integer.valueOf(1024), param.getInitParam().getDimension());
        assertEquals("manhattan", param.getInitParam().getVectorDistance());
        
        // 其他值应该保持默认
        assertEquals("hnsw", param.getInitParam().getIndexType());
    }

    @Test
    public void testInitParamIndependence() {
        RocksetParam param1 = RocksetParam.builder()
                .workspace("workspace1")
                .collectionName("collection1")
                .build();
        
        RocksetParam param2 = RocksetParam.builder()
                .workspace("workspace2")
                .collectionName("collection2")
                .build();
        
        // 确保不同实例的 InitParam 是独立的
        assertNotSame(param1.getInitParam(), param2.getInitParam());
        assertEquals("workspace1", param1.getInitParam().getWorkspace());
        assertEquals("workspace2", param2.getInitParam().getWorkspace());
        assertEquals("collection1", param1.getInitParam().getCollectionName());
        assertEquals("collection2", param2.getInitParam().getCollectionName());
    }

    @Test
    public void testFieldNameCustomization() {
        RocksetParam param = RocksetParam.builder()
                .fieldNamePageContent("custom_content")
                .fieldNameUniqueId("custom_id")
                .fieldMeta("custom_metadata")
                .fieldNameVector("custom_vector")
                .build();
        
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_metadata", param.getFieldMeta());
        assertEquals("custom_vector", param.getFieldNameVector());
        
        // InitParam 应该保持默认值
        assertEquals("commons", param.getInitParam().getWorkspace());
        assertEquals("langengine_rockset_collection", param.getInitParam().getCollectionName());
    }

    @Test
    public void testBasicSettings() {
        RocksetParam param = RocksetParam.builder()
                .build();
        
        // 其他值应该保持默认
        assertEquals("commons", param.getInitParam().getWorkspace());
        assertEquals("LangEngine Rockset Vector Store", param.getInitParam().getDescription());
    }
}
