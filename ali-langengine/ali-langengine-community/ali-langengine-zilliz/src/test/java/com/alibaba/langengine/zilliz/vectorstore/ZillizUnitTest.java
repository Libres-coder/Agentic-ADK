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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ZillizUnitTest {

    @Test
    public void testZillizParamDefaults() {
        ZillizParam param = new ZillizParam();
        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("embeddings", param.getFieldNameEmbedding());
        assertEquals("row_content", param.getFieldNamePageContent());
        assertNotNull(param.getSearchParams());
        assertNotNull(param.getInitParam());
    }

    @Test
    public void testZillizParamSetters() {
        ZillizParam param = new ZillizParam();
        param.setFieldNameUniqueId("test_id");
        param.setFieldNameEmbedding("test_embedding");
        param.setFieldNamePageContent("test_content");
        
        assertEquals("test_id", param.getFieldNameUniqueId());
        assertEquals("test_embedding", param.getFieldNameEmbedding());
        assertEquals("test_content", param.getFieldNamePageContent());
    }

    @Test
    public void testInitParamDefaults() {
        ZillizParam.InitParam initParam = new ZillizParam.InitParam();
        assertEquals(8192, initParam.getFieldPageContentMaxLength());
        assertEquals(1536, initParam.getFieldEmbeddingsDimension());
        assertEquals(2, initParam.getShardsNum());
        assertEquals(io.milvus.common.clientenum.ConsistencyLevelEnum.BOUNDED, initParam.getConsistencyLevel());
    }

    @Test
    public void testInitParamSetters() {
        ZillizParam.InitParam initParam = new ZillizParam.InitParam();
        initParam.setFieldEmbeddingsDimension(768);
        initParam.setFieldPageContentMaxLength(4096);
        initParam.setShardsNum(4);
        
        assertEquals(768, initParam.getFieldEmbeddingsDimension());
        assertEquals(4096, initParam.getFieldPageContentMaxLength());
        assertEquals(4, initParam.getShardsNum());
    }

    @Test
    public void testSearchParamsNotNull() {
        ZillizParam param = new ZillizParam();
        assertNotNull(param.getSearchParams());
        assertTrue(param.getSearchParams().containsKey("nprobe"));
        assertTrue(param.getSearchParams().containsKey("offset"));
    }
}