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
package com.alibaba.langengine.deeplake.vectorstore;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class DeepLakeParamTest {

    @Test
    public void testDefaultValues() {
        DeepLakeParam param = new DeepLakeParam();
        
        // 测试默认值
        assertNotNull(param.getInitParam());
        assertEquals("langengine_deeplake_collection", param.getInitParam().getDatasetName());
        assertEquals("cosine", param.getInitParam().getVectorDistance());
        assertEquals(Integer.valueOf(1536), param.getInitParam().getDimension());
        assertEquals("hnsw", param.getInitParam().getIndexType());
        assertEquals(Integer.valueOf(16), param.getInitParam().getMaxConnections());
        assertEquals(Integer.valueOf(200), param.getInitParam().getEfConstruction());
        assertEquals(Integer.valueOf(10), param.getInitParam().getEfSearch());
        assertEquals(Boolean.FALSE, param.getInitParam().getIsPublic());
        assertEquals("LangEngine Deep Lake Vector Store", param.getInitParam().getDescription());
        
        // 测试字段名默认值
        assertEquals("page_content", param.getFieldNamePageContent());
        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("meta_data", param.getFieldMeta());
        assertEquals("vector", param.getFieldNameVector());
    }

    @Test
    public void testBuilderPattern() {
        DeepLakeParam param = DeepLakeParam.builder()
                .datasetName("test_dataset")
                .vectorDistance("euclidean")
                .dimension(768)
                .indexType("flat")
                .maxConnections(32)
                .efConstruction(400)
                .efSearch(20)
                .isPublic(true)
                .description("Test dataset")
                .fieldNamePageContent("content")
                .fieldNameUniqueId("id")
                .fieldMeta("metadata")
                .fieldNameVector("embeddings")
                .build();
        
        // 测试设置的值
        assertEquals("test_dataset", param.getInitParam().getDatasetName());
        assertEquals("euclidean", param.getInitParam().getVectorDistance());
        assertEquals(Integer.valueOf(768), param.getInitParam().getDimension());
        assertEquals("flat", param.getInitParam().getIndexType());
        assertEquals(Integer.valueOf(32), param.getInitParam().getMaxConnections());
        assertEquals(Integer.valueOf(400), param.getInitParam().getEfConstruction());
        assertEquals(Integer.valueOf(20), param.getInitParam().getEfSearch());
        assertEquals(Boolean.TRUE, param.getInitParam().getIsPublic());
        assertEquals("Test dataset", param.getInitParam().getDescription());
        
        assertEquals("content", param.getFieldNamePageContent());
        assertEquals("id", param.getFieldNameUniqueId());
        assertEquals("metadata", param.getFieldMeta());
        assertEquals("embeddings", param.getFieldNameVector());
    }

    @Test
    public void testBuilderChaining() {
        DeepLakeParam.Builder builder = DeepLakeParam.builder();
        
        // 测试链式调用
        DeepLakeParam param = builder
                .datasetName("chained_dataset")
                .dimension(1024)
                .vectorDistance("manhattan")
                .build();
        
        assertEquals("chained_dataset", param.getInitParam().getDatasetName());
        assertEquals(Integer.valueOf(1024), param.getInitParam().getDimension());
        assertEquals("manhattan", param.getInitParam().getVectorDistance());
        
        // 其他值应该保持默认
        assertEquals("hnsw", param.getInitParam().getIndexType());
        assertEquals(Boolean.FALSE, param.getInitParam().getIsPublic());
    }

    @Test
    public void testInitParamIndependence() {
        DeepLakeParam param1 = DeepLakeParam.builder()
                .datasetName("dataset1")
                .build();
        
        DeepLakeParam param2 = DeepLakeParam.builder()
                .datasetName("dataset2")
                .build();
        
        // 确保不同实例的 InitParam 是独立的
        assertNotSame(param1.getInitParam(), param2.getInitParam());
        assertEquals("dataset1", param1.getInitParam().getDatasetName());
        assertEquals("dataset2", param2.getInitParam().getDatasetName());
    }
}
