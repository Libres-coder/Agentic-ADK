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
package com.alibaba.langengine.vectra.vectorstore;

import com.alibaba.langengine.vectra.exception.VectraException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class VectraServiceTest {

    @Test
    void testVectraParamDefaults() {
        VectraParam param = new VectraParam();
        
        assertEquals("page_content", param.getFieldNamePageContent());
        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("vector", param.getFieldNameVector());
        assertEquals("metadata", param.getFieldNameMetadata());
        assertEquals(30, param.getConnectTimeout());
        assertEquals(60, param.getReadTimeout());
        assertEquals(60, param.getWriteTimeout());
        
        VectraParam.CollectionParam collectionParam = param.getCollectionParam();
        assertEquals(1536, collectionParam.getVectorDimension());
        assertEquals("cosine", collectionParam.getMetricType());
        assertTrue(collectionParam.isAutoCreateCollection());
        assertEquals("hnsw", collectionParam.getIndexType());
    }

    @Test
    void testVectraExceptionHandling() {
        VectraException exception1 = new VectraException("Test error");
        assertEquals("VECTRA_ERROR", exception1.getErrorCode());
        
        VectraException exception2 = new VectraException("CUSTOM_CODE", "Custom error");
        assertEquals("CUSTOM_CODE", exception2.getErrorCode());
        
        Exception cause = new RuntimeException("Root cause");
        VectraException exception3 = new VectraException("Error with cause", cause);
        assertEquals(cause, exception3.getCause());
    }
}