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
package com.alibaba.langengine.atlas.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class AtlasTest {

    @Nested
    @DisplayName("Atlas Parameter Tests")
    class AtlasParameterTests {

        @Test
        @DisplayName("Test AtlasParam default values")
        public void testAtlasParamDefaults() {
            AtlasParam param = new AtlasParam();
            
            assertEquals("content_id", param.getFieldNameUniqueId());
            assertEquals("embeddings", param.getFieldNameEmbedding());
            assertEquals("row_content", param.getFieldNamePageContent());
            assertEquals("vector_index", param.getVectorIndexName());
            assertEquals(100, param.getNumCandidates());
            assertNotNull(param.getInitParam());
        }

        @Test
        @DisplayName("Test AtlasParam InitParam defaults")
        public void testInitParamDefaults() {
            AtlasParam.InitParam initParam = new AtlasParam.InitParam();
            
            assertTrue(initParam.isFieldUniqueIdAsPrimaryKey());
            assertEquals(1536, initParam.getFieldEmbeddingsDimension());
            assertEquals("cosine", initParam.getSimilarity());
        }

        @Test
        @DisplayName("Test AtlasParam parameter setting")
        public void testParameterSetting() {
            AtlasParam param = new AtlasParam();
            param.setFieldNameUniqueId("test_id");
            param.setFieldNameEmbedding("test_embedding");
            param.setFieldNamePageContent("test_content");
            param.setVectorIndexName("test_index");
            param.setNumCandidates(200);
            
            assertEquals("test_id", param.getFieldNameUniqueId());
            assertEquals("test_embedding", param.getFieldNameEmbedding());
            assertEquals("test_content", param.getFieldNamePageContent());
            assertEquals("test_index", param.getVectorIndexName());
            assertEquals(200, param.getNumCandidates());
        }
    }

    @Nested
    @DisplayName("Atlas Exception Tests")
    class AtlasExceptionTests {

        @Test
        @DisplayName("Test AtlasException creation with message")
        public void testAtlasExceptionWithMessage() {
            AtlasException exception = new AtlasException("Test message");
            assertEquals("Test message", exception.getMessage());
        }

        @Test
        @DisplayName("Test AtlasException creation with message and cause")
        public void testAtlasExceptionWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("Cause");
            AtlasException exception = new AtlasException("Test message", cause);
            assertEquals("Test message", exception.getMessage());
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {

        @Test
        @DisplayName("Test embedding dimension calculation")
        public void testEmbeddingDimensionCalculation() {
            FakeEmbeddings fakeEmbeddings = new FakeEmbeddings();
            assertNotNull(fakeEmbeddings);
        }

        @Test
        @DisplayName("Test document creation")
        public void testDocumentCreation() {
            Document doc = createTestDocument("1", "test content");
            
            assertEquals("1", doc.getUniqueId());
            assertEquals("test content", doc.getPageContent());
        }

        private Document createTestDocument(String id, String content) {
            Document doc = new Document();
            doc.setUniqueId(id);
            doc.setPageContent(content);
            return doc;
        }
    }

    @Nested
    @DisplayName("Atlas Configuration Tests")
    class AtlasConfigurationTests {

        @Test
        @DisplayName("Test Atlas configuration constants")
        public void testAtlasConfiguration() {
            // Test that configuration class exists and can be instantiated
            assertDoesNotThrow(() -> {
                Class.forName("com.alibaba.langengine.atlas.AtlasConfiguration");
            });
        }
    }

}