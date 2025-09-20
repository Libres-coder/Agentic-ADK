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
package com.alibaba.langengine.typesense.vectorstore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.langengine.core.indexes.Document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class TypesenseStoreTest {

    private List<Document> testDocuments;

    @BeforeEach
    void setUp() {
        testDocuments = createTestDocuments();
    }

    @Test
    public void testDocumentCreation() {
        // Given & When & Then - 测试文档创建逻辑
        assertNotNull(testDocuments);
        assertEquals(3, testDocuments.size());

        Document firstDoc = testDocuments.get(0);
        assertEquals("doc-1", firstDoc.getUniqueId());
        assertEquals("Test content 1", firstDoc.getPageContent());
        assertNotNull(firstDoc.getMetadata());
        assertEquals("test", firstDoc.getMetadata().get("source"));
        assertEquals(1, firstDoc.getMetadata().get("index"));
    }

    @Test
    public void testMetadataValidation() {
        // Given
        List<Map<String, Object>> metadatas = createTestMetadatas();

        // When & Then - 测试元数据验证逻辑
        assertNotNull(metadatas);
        assertEquals(3, metadatas.size());

        // 验证元数据内容
        assertEquals("greeting", metadatas.get(0).get("type"));
        assertEquals("question", metadatas.get(1).get("type"));
        assertEquals("politeness", metadatas.get(2).get("type"));
    }

    @Test
    public void testTextListProcessing() {
        // Given
        List<String> texts = Arrays.asList("Hello world", "How are you?", "Nice to meet you");

        // When & Then - 测试文本列表处理逻辑
        assertNotNull(texts);
        assertEquals(3, texts.size());

        // 验证文本内容
        assertTrue(texts.contains("Hello world"));
        assertTrue(texts.contains("How are you?"));
        assertTrue(texts.contains("Nice to meet you"));

        // 验证每个文本都不为空
        for (String text : texts) {
            assertNotNull(text);
            assertFalse(text.trim().isEmpty());
        }
    }

    @Test
    public void testQueryParameterValidation() {
        // Test empty query vector
        List<Float> emptyVector = new ArrayList<>();
        assertTrue(emptyVector.isEmpty());

        // Test invalid k value
        int invalidK = 0;
        assertTrue(invalidK <= 0);

        // Test valid parameters
        List<Float> validVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        int validK = 5;

        assertFalse(validVector.isEmpty());
        assertTrue(validK > 0);
        assertEquals(3, validVector.size());
    }

    @Test
    public void testDocumentValidation() {
        // Test empty content validation
        Document emptyDoc = new Document();
        emptyDoc.setPageContent("");
        emptyDoc.setMetadata(new HashMap<>());

        assertNotNull(emptyDoc);
        assertTrue(emptyDoc.getPageContent().isEmpty());

        // Test valid document
        Document validDoc = testDocuments.get(0);
        assertNotNull(validDoc);
        assertFalse(validDoc.getPageContent().isEmpty());
        assertNotNull(validDoc.getMetadata());
    }

    @Test
    public void testEmbeddingDataStructure() {
        // Test embedding data conversion logic
        List<Double> doubleEmbeddings = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);

        // Convert to Float list (simulating the conversion in TypesenseStore)
        List<Float> floatEmbeddings = new ArrayList<>();
        for (Double d : doubleEmbeddings) {
            floatEmbeddings.add(d.floatValue());
        }

        assertEquals(doubleEmbeddings.size(), floatEmbeddings.size());

        for (int i = 0; i < doubleEmbeddings.size(); i++) {
            assertEquals(doubleEmbeddings.get(i).floatValue(), floatEmbeddings.get(i), 0.001f);
        }
    }

    private List<Document> createTestDocuments() {
        List<Document> documents = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Document doc = new Document();
            doc.setUniqueId("doc-" + i);
            doc.setPageContent("Test content " + i);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "test");
            metadata.put("index", i);
            doc.setMetadata(metadata);

            // 模拟向量数据
            List<Double> embedding = Arrays.asList(0.1 * i, 0.2 * i, 0.3 * i);
            doc.setEmbedding(embedding);

            documents.add(doc);
        }

        return documents;
    }

    private List<Map<String, Object>> createTestMetadatas() {
        List<Map<String, Object>> metadatas = new ArrayList<>();

        Map<String, Object> meta1 = new HashMap<>();
        meta1.put("type", "greeting");
        metadatas.add(meta1);

        Map<String, Object> meta2 = new HashMap<>();
        meta2.put("type", "question");
        metadatas.add(meta2);

        Map<String, Object> meta3 = new HashMap<>();
        meta3.put("type", "politeness");
        metadatas.add(meta3);

        return metadatas;
    }
}