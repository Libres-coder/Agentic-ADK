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

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class VectraTest {

    @Test
    void testConstructorWithCollectionName() {
        Vectra vectraInstance = new Vectra("test_collection");
        assertNotNull(vectraInstance);
        assertEquals("test_collection", vectraInstance.getCollectionName());
    }

    @Test
    void testVectraParamBuilder() {
        VectraParam param = VectraParam.builder()
                .vectorDimension(512)
                .metricType("cosine")
                .build();

        assertNotNull(param);
        assertEquals(512, param.getCollectionParam().getVectorDimension());
        assertEquals("cosine", param.getCollectionParam().getMetricType());
    }

    @Test
    void testAddEmptyDocuments() {
        Vectra vectra = new Vectra("test_collection");
        vectra.setEmbedding(new FakeEmbeddings());
        
        assertDoesNotThrow(() -> vectra.addDocuments(new ArrayList<>()));
        assertDoesNotThrow(() -> vectra.addDocuments(null));
    }

    @Test
    void testSimilaritySearchWithEmptyQuery() {
        Vectra vectra = new Vectra("test_collection");
        vectra.setEmbedding(new FakeEmbeddings());
        
        List<Document> results = vectra.similaritySearch("", 5);
        assertTrue(results.isEmpty());
        
        results = vectra.similaritySearch(null, 5);
        assertTrue(results.isEmpty());
    }
}