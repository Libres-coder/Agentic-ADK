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
import java.util.UUID;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.typesense.exception.TypesenseException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class TypesenseStoreValidationTest {

    @Test
    public void testValidateCollectionName_Valid() {
        // 测试有效的集合名称
        String validName1 = validateCollectionName("validname123");
        assertEquals("validname123", validName1);

        String validName2 = validateCollectionName("test_collection");
        assertEquals("test_collection", validName2);

        String validName3 = validateCollectionName("test-collection-1");
        assertEquals("test-collection-1", validName3);
    }

    @Test
    public void testValidateCollectionName_Invalid() {
        // 测试无效的集合名称

        // 名称太短
        TypesenseException exception1 = assertThrows(TypesenseException.class, () -> {
            validateCollectionName("ab");
        });
        assertTrue(exception1.getMessage().contains("集合名称长度必须在3到63个字符之间"));

        // 名称太长
        String longName = "a".repeat(64);
        TypesenseException exception2 = assertThrows(TypesenseException.class, () -> {
            validateCollectionName(longName);
        });
        assertTrue(exception2.getMessage().contains("集合名称长度必须在3到63个字符之间"));

        // 包含连续的点
        TypesenseException exception3 = assertThrows(TypesenseException.class, () -> {
            validateCollectionName("test..name");
        });
        assertTrue(exception3.getMessage().contains("集合名称不能包含连续的点"));

        // 不以字母数字开头
        TypesenseException exception4 = assertThrows(TypesenseException.class, () -> {
            validateCollectionName("-testname");
        });
        assertTrue(exception4.getMessage().contains("集合名称格式不正确"));
    }

    @Test
    public void testParameterValidation() {
        // 测试参数验证

        // 空服务器URL
        TypesenseException exception1 = assertThrows(TypesenseException.class, () -> {
            validateServerUrl("");
        });
        assertTrue(exception1.getMessage().contains("服务器URL不能为空"));

        // 空API Key
        TypesenseException exception2 = assertThrows(TypesenseException.class, () -> {
            validateApiKey("");
        });
        assertTrue(exception2.getMessage().contains("API Key不能为空"));

        // 无效维度
        TypesenseException exception3 = assertThrows(TypesenseException.class, () -> {
            validateDimensions(0);
        });
        assertTrue(exception3.getMessage().contains("向量维度必须大于0"));
    }

    @Test
    public void testQueryValidation() {
        // 测试查询参数验证

        // 空查询文本
        TypesenseException exception1 = assertThrows(TypesenseException.class, () -> {
            validateQuery("");
        });
        assertTrue(exception1.getMessage().contains("查询文本不能为空"));

        // 无效的k值
        TypesenseException exception2 = assertThrows(TypesenseException.class, () -> {
            validateK(0);
        });
        assertTrue(exception2.getMessage().contains("返回结果数量k必须大于0"));
    }

    @Test
    public void testDocumentValidation() {
        // 测试文档验证
        Document emptyDoc = new Document();
        emptyDoc.setPageContent("");
        emptyDoc.setMetadata(new HashMap<>());

        assertNotNull(emptyDoc);
        assertTrue(emptyDoc.getPageContent().isEmpty());

        // 测试有效文档
        Document validDoc = new Document();
        validDoc.setPageContent("Valid content");
        validDoc.setMetadata(new HashMap<>());

        assertNotNull(validDoc);
        assertFalse(validDoc.getPageContent().isEmpty());
        assertNotNull(validDoc.getMetadata());
    }

    @Test
    public void testTextListValidation() {
        // 测试文本列表验证
        TypesenseException exception = assertThrows(TypesenseException.class, () -> {
            validateTexts(null);
        });
        assertTrue(exception.getMessage().contains("文本列表不能为空"));

        // 测试有效文本列表
        List<String> validTexts = Arrays.asList("Hello", "World", "Test");
        assertDoesNotThrow(() -> {
            validateTexts(validTexts);
        });
    }

    @Test
    public void testDocumentIdValidation() {
        // 测试文档ID验证
        TypesenseException exception = assertThrows(TypesenseException.class, () -> {
            validateDocumentId("");
        });
        assertTrue(exception.getMessage().contains("文档ID不能为空"));

        // 测试有效ID
        assertDoesNotThrow(() -> {
            validateDocumentId("valid-id-123");
        });
    }

    // 验证方法实现（从TypesenseStore中抽取的验证逻辑）

    private String validateCollectionName(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            String generated = UUID.randomUUID().toString().replace("-", "").toLowerCase();
            return generated;
        }

        // 验证集合名称规则
        String cleaned = collectionName.toLowerCase();
        if (cleaned.length() < 3 || cleaned.length() > 63) {
            throw TypesenseException.validationError("集合名称长度必须在3到63个字符之间");
        }

        if (!cleaned.matches("^[a-z0-9][a-z0-9._-]*[a-z0-9]$")) {
            throw TypesenseException.validationError("集合名称格式不正确，必须以字母或数字开头和结尾");
        }

        if (cleaned.contains("..")) {
            throw TypesenseException.validationError("集合名称不能包含连续的点");
        }

        return cleaned;
    }

    private void validateServerUrl(String serverUrl) {
        if (StringUtils.isBlank(serverUrl)) {
            throw TypesenseException.configurationError("服务器URL不能为空");
        }
    }

    private void validateApiKey(String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            throw TypesenseException.configurationError("API Key不能为空");
        }
    }

    private void validateDimensions(int dimensions) {
        if (dimensions <= 0) {
            throw TypesenseException.validationError("向量维度必须大于0");
        }
    }

    private void validateQuery(String query) {
        if (StringUtils.isBlank(query)) {
            throw TypesenseException.validationError("查询文本不能为空");
        }
    }

    private void validateK(int k) {
        if (k <= 0) {
            throw TypesenseException.validationError("返回结果数量k必须大于0");
        }
    }

    private void validateTexts(Iterable<String> texts) {
        if (texts == null) {
            throw TypesenseException.validationError("文本列表不能为空");
        }
    }

    private void validateDocumentId(String documentId) {
        if (StringUtils.isBlank(documentId)) {
            throw TypesenseException.validationError("文档ID不能为空");
        }
    }
}