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
package com.alibaba.langengine.txtai.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.txtai.exception.TxtaiException;
import com.alibaba.langengine.txtai.vectorstore.service.TxtaiSearchResponse;
import com.alibaba.langengine.txtai.vectorstore.service.TxtaiService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class TxtaiMockTest {

    private Txtai txtai;

    @BeforeEach
    public void setUp() {
        txtai = new Txtai("http://localhost:8000", new FakeEmbeddings(), "test_index");
    }

    @Test
    public void testValidation_EmptyDocuments() {
        // 测试空文档列表验证
        List<Document> emptyDocuments = new ArrayList<>();

        TxtaiException exception = assertThrows(TxtaiException.class, () -> {
            txtai.addDocuments(emptyDocuments);
        });

        assertEquals("TXTAI_VALIDATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("文档列表不能为空"));
    }

    @Test
    public void testValidation_NullDocuments() {
        // 测试null文档列表验证
        TxtaiException exception = assertThrows(TxtaiException.class, () -> {
            txtai.addDocuments(null);
        });

        assertEquals("TXTAI_VALIDATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("文档列表不能为空"));
    }

    @Test
    public void testValidation_EmptyQuery() {
        // 测试空查询验证
        TxtaiException exception = assertThrows(TxtaiException.class, () -> {
            txtai.similaritySearch("", 5);
        });

        assertEquals("TXTAI_VALIDATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("查询文本不能为空"));
    }

    @Test
    public void testValidation_NullQuery() {
        // 测试null查询验证
        TxtaiException exception = assertThrows(TxtaiException.class, () -> {
            txtai.similaritySearch(null, 5);
        });

        assertEquals("TXTAI_VALIDATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("查询文本不能为空"));
    }

    @Test
    public void testValidation_InvalidK() {
        // 测试无效的k值
        TxtaiException exception = assertThrows(TxtaiException.class, () -> {
            txtai.similaritySearch("test", 0);
        });

        assertEquals("TXTAI_VALIDATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("返回结果数量k必须大于0"));
    }

    @Test
    public void testValidation_LargeK() {
        // 测试k值过大
        TxtaiException exception = assertThrows(TxtaiException.class, () -> {
            txtai.similaritySearch("test", 1001);
        });

        assertEquals("TXTAI_VALIDATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("返回结果数量k不能超过1000"));
    }

    @Test
    public void testConstructor_ValidParameters() {
        // 测试有效参数构造
        assertDoesNotThrow(() -> {
            new Txtai("http://localhost:8000", new FakeEmbeddings(), "valid_index");
        });
    }

    @Test
    public void testConstructor_InvalidServerUrl() {
        // 测试无效服务器URL
        TxtaiException exception = assertThrows(TxtaiException.class, () -> {
            new Txtai("", new FakeEmbeddings(), "test_index");
        });

        assertEquals("TXTAI_CONFIG_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("txtai服务器URL不能为空"));
    }

    @Test
    public void testConstructor_InvalidEmbedding() {
        // 测试null Embedding
        TxtaiException exception = assertThrows(TxtaiException.class, () -> {
            new Txtai("http://localhost:8000", null, "test_index");
        });

        assertEquals("TXTAI_CONFIG_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Embedding对象不能为空"));
    }

    @Test
    public void testIndexNameValidation() {
        // 测试索引名称验证
        Txtai txtai1 = new Txtai("http://localhost:8000", new FakeEmbeddings(), "valid123");
        assertEquals("valid123", txtai1.getIndexName());

        // 测试默认索引名称
        Txtai txtai2 = new Txtai("http://localhost:8000", new FakeEmbeddings(), null);
        assertEquals("default", txtai2.getIndexName());

        // 测试无效索引名称
        TxtaiException exception = assertThrows(TxtaiException.class, () -> {
            new Txtai("http://localhost:8000", new FakeEmbeddings(), "ab");
        });

        assertEquals("TXTAI_VALIDATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("索引名称长度必须在3到63个字符之间"));
    }

    @Test
    public void testExceptionTypes() {
        // 测试不同类型的异常

        // 配置错误
        TxtaiException configError = TxtaiException.configurationError("配置错误测试");
        assertEquals("TXTAI_CONFIG_ERROR", configError.getErrorCode());
        assertEquals(TxtaiException.ErrorType.CONFIGURATION_ERROR, configError.getErrorType());

        // 验证错误
        TxtaiException validationError = TxtaiException.validationError("验证错误测试");
        assertEquals("TXTAI_VALIDATION_ERROR", validationError.getErrorCode());
        assertEquals(TxtaiException.ErrorType.VALIDATION_ERROR, validationError.getErrorType());

        // API错误
        TxtaiException apiError = TxtaiException.apiError(404, "资源未找到");
        assertEquals("TXTAI_API_ERROR_404", apiError.getErrorCode());
        assertTrue(apiError.getMessage().contains("HTTP 404"));
    }

    @Test
    public void testServiceOperations_ExpectNetworkErrors() {
        // 测试实际的服务操作（期望网络错误，因为没有真实的txtai服务）
        List<Document> documents = createTestDocuments();

        // 由于没有真实的服务，这应该抛出网络错误
        assertThrows(TxtaiException.class, () -> {
            txtai.addDocuments(documents);
        });

        // 搜索也应该抛出网络错误
        assertThrows(TxtaiException.class, () -> {
            txtai.similaritySearch("test query", 5);
        });
    }

    // 辅助方法
    private List<Document> createTestDocuments() {
        List<Document> documents = new ArrayList<>();

        Document doc1 = new Document();
        doc1.setUniqueId("doc1");
        doc1.setPageContent("This is document 1");
        doc1.setMetadata(new HashMap<>());
        documents.add(doc1);

        Document doc2 = new Document();
        doc2.setUniqueId("doc2");
        doc2.setPageContent("This is document 2");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", "test");
        doc2.setMetadata(metadata);
        documents.add(doc2);

        return documents;
    }
}