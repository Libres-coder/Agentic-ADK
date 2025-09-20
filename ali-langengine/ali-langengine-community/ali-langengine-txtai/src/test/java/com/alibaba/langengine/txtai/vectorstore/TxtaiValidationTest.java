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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class TxtaiValidationTest {

    @Test
    public void testConstructor_ValidParameters() {
        // 测试有效参数
        assertDoesNotThrow(() -> {
            new Txtai("http://localhost:8000", new FakeEmbeddings(), "valid_index");
        });

        assertDoesNotThrow(() -> {
            new Txtai("https://txtai.example.com", new FakeEmbeddings(), "test-index-123");
        });
    }

    @Test
    public void testConstructor_InvalidServerUrl() {
        // 测试空服务器URL
        TxtaiException exception1 = assertThrows(TxtaiException.class, () -> {
            new Txtai("", new FakeEmbeddings(), "test_index");
        });
        assertEquals("TXTAI_CONFIG_ERROR", exception1.getErrorCode());
        assertTrue(exception1.getMessage().contains("txtai服务器URL不能为空"));

        // 测试null服务器URL
        TxtaiException exception2 = assertThrows(TxtaiException.class, () -> {
            new Txtai(null, new FakeEmbeddings(), "test_index");
        });
        assertEquals("TXTAI_CONFIG_ERROR", exception2.getErrorCode());

        // 测试无效的URL格式
        TxtaiException exception3 = assertThrows(TxtaiException.class, () -> {
            new Txtai("ftp://invalid-protocol", new FakeEmbeddings(), "test_index");
        });
        assertEquals("TXTAI_CONFIG_ERROR", exception3.getErrorCode());
        assertTrue(exception3.getMessage().contains("URL格式不正确"));
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
    public void testValidateIndexName_Valid() {
        // 测试有效的索引名称
        Txtai txtai = new Txtai("http://localhost:8000", new FakeEmbeddings(), "validname123");
        assertEquals("validname123", txtai.getIndexName());

        Txtai txtai2 = new Txtai("http://localhost:8000", new FakeEmbeddings(), "test_index");
        assertEquals("test_index", txtai2.getIndexName());

        Txtai txtai3 = new Txtai("http://localhost:8000", new FakeEmbeddings(), "test-index-1");
        assertEquals("test-index-1", txtai3.getIndexName());

        // 测试空索引名称，应该使用默认值
        Txtai txtai4 = new Txtai("http://localhost:8000", new FakeEmbeddings(), null);
        assertEquals("default", txtai4.getIndexName());

        Txtai txtai5 = new Txtai("http://localhost:8000", new FakeEmbeddings(), "");
        assertEquals("default", txtai5.getIndexName());
    }

    @Test
    public void testValidateIndexName_Invalid() {
        // 索引名称太短
        TxtaiException exception1 = assertThrows(TxtaiException.class, () -> {
            new Txtai("http://localhost:8000", new FakeEmbeddings(), "ab");
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception1.getErrorCode());
        assertTrue(exception1.getMessage().contains("索引名称长度必须在3到63个字符之间"));

        // 索引名称太长
        String longName = "a".repeat(64);
        TxtaiException exception2 = assertThrows(TxtaiException.class, () -> {
            new Txtai("http://localhost:8000", new FakeEmbeddings(), longName);
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception2.getErrorCode());
        assertTrue(exception2.getMessage().contains("索引名称长度必须在3到63个字符之间"));

        // 包含连续的点
        TxtaiException exception3 = assertThrows(TxtaiException.class, () -> {
            new Txtai("http://localhost:8000", new FakeEmbeddings(), "test..name");
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception3.getErrorCode());
        assertTrue(exception3.getMessage().contains("索引名称不能包含连续的点"));

        // 不以字母数字开头
        TxtaiException exception4 = assertThrows(TxtaiException.class, () -> {
            new Txtai("http://localhost:8000", new FakeEmbeddings(), "-testname");
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception4.getErrorCode());
        assertTrue(exception4.getMessage().contains("索引名称格式不正确"));

        // 包含大写字母会被转换为小写
        Txtai txtai = new Txtai("http://localhost:8000", new FakeEmbeddings(), "TestIndex");
        assertEquals("testindex", txtai.getIndexName());
    }

    @Test
    public void testValidateDocuments() {
        Txtai txtai = new Txtai("http://localhost:8000", new FakeEmbeddings(), "test_index");

        // 测试空文档列表
        List<Document> emptyDocs = new ArrayList<>();
        TxtaiException exception1 = assertThrows(TxtaiException.class, () -> {
            txtai.addDocuments(emptyDocs);
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception1.getErrorCode());
        assertTrue(exception1.getMessage().contains("文档列表不能为空"));

        // 测试null文档列表
        TxtaiException exception2 = assertThrows(TxtaiException.class, () -> {
            txtai.addDocuments(null);
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception2.getErrorCode());
        assertTrue(exception2.getMessage().contains("文档列表不能为空"));
    }

    @Test
    public void testValidateQuery() {
        Txtai txtai = new Txtai("http://localhost:8000", new FakeEmbeddings(), "test_index");

        // 测试空查询文本
        TxtaiException exception1 = assertThrows(TxtaiException.class, () -> {
            txtai.similaritySearch("", 5);
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception1.getErrorCode());
        assertTrue(exception1.getMessage().contains("查询文本不能为空"));

        // 测试null查询文本
        TxtaiException exception2 = assertThrows(TxtaiException.class, () -> {
            txtai.similaritySearch(null, 5);
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception2.getErrorCode());
        assertTrue(exception2.getMessage().contains("查询文本不能为空"));

        // 测试只包含空白字符的查询
        TxtaiException exception3 = assertThrows(TxtaiException.class, () -> {
            txtai.similaritySearch("   ", 5);
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception3.getErrorCode());
        assertTrue(exception3.getMessage().contains("查询文本不能只包含空白字符"));
    }

    @Test
    public void testValidateK() {
        Txtai txtai = new Txtai("http://localhost:8000", new FakeEmbeddings(), "test_index");

        // 测试无效的k值 - 小于等于0
        TxtaiException exception1 = assertThrows(TxtaiException.class, () -> {
            txtai.similaritySearch("test query", 0);
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception1.getErrorCode());
        assertTrue(exception1.getMessage().contains("返回结果数量k必须大于0"));

        TxtaiException exception2 = assertThrows(TxtaiException.class, () -> {
            txtai.similaritySearch("test query", -1);
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception2.getErrorCode());
        assertTrue(exception2.getMessage().contains("返回结果数量k必须大于0"));

        // 测试k值过大
        TxtaiException exception3 = assertThrows(TxtaiException.class, () -> {
            txtai.similaritySearch("test query", 1001);
        });
        assertEquals("TXTAI_VALIDATION_ERROR", exception3.getErrorCode());
        assertTrue(exception3.getMessage().contains("返回结果数量k不能超过1000"));
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

        // 网络错误
        TxtaiException networkError = TxtaiException.networkError("网络错误测试", new RuntimeException());
        assertEquals("TXTAI_NETWORK_ERROR", networkError.getErrorCode());
        assertEquals(TxtaiException.ErrorType.NETWORK_ERROR, networkError.getErrorType());

        // API错误
        TxtaiException apiError = TxtaiException.apiError("API错误测试");
        assertEquals("TXTAI_API_ERROR", apiError.getErrorCode());
        assertEquals(TxtaiException.ErrorType.API_ERROR, apiError.getErrorType());

        // 带状态码的API错误
        TxtaiException apiErrorWithCode = TxtaiException.apiError(404, "资源未找到");
        assertEquals("TXTAI_API_ERROR_404", apiErrorWithCode.getErrorCode());
        assertTrue(apiErrorWithCode.getMessage().contains("HTTP 404"));

        // 处理错误
        TxtaiException processingError = TxtaiException.processingError("处理错误测试", new RuntimeException());
        assertEquals("TXTAI_PROCESSING_ERROR", processingError.getErrorCode());
        assertEquals(TxtaiException.ErrorType.PROCESSING_ERROR, processingError.getErrorType());
    }

    @Test
    public void testExceptionToString() {
        TxtaiException exception = TxtaiException.validationError("测试异常消息");
        String expectedFormat = "TxtaiException{errorCode='TXTAI_VALIDATION_ERROR', errorType=VALIDATION_ERROR, message='测试异常消息'}";
        assertEquals(expectedFormat, exception.toString());
    }

    @Test
    public void testDocumentWithValidContent() {
        // 测试包含有效内容的文档
        List<Document> documents = new ArrayList<>();

        Document doc = new Document();
        doc.setPageContent("This is a valid document content");
        doc.setMetadata(new HashMap<>());
        documents.add(doc);

        // 这应该不会抛出验证异常（但可能因为没有实际服务而抛出网络异常）
        Txtai txtai = new Txtai("http://localhost:8000", new FakeEmbeddings(), "test_index");

        // 我们期望这里不会有验证错误，但可能有网络/处理错误
        assertThrows(TxtaiException.class, () -> {
            txtai.addDocuments(documents);
        });
    }
}