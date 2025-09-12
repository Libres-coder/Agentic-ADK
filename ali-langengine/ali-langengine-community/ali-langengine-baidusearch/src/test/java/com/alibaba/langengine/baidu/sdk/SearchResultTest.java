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
package com.alibaba.langengine.baidu.sdk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 搜索结果类测试
 *
 * @author aihe.ah
 */
public class SearchResultTest {

    private SearchResult searchResult;

    @BeforeEach
    void setUp() {
        searchResult = new SearchResult();
    }

    @Test
    void testDefaultValues() {
        // 测试默认值
        assertNull(searchResult.getTitle());
        assertNull(searchResult.getUrl());
        assertNull(searchResult.getDescription());
    }

    @Test
    void testSetAndGetTitle() {
        String title = "测试标题";
        searchResult.setTitle(title);
        assertEquals(title, searchResult.getTitle());
    }

    @Test
    void testSetAndGetUrl() {
        String url = "http://test.com";
        searchResult.setUrl(url);
        assertEquals(url, searchResult.getUrl());
    }

    @Test
    void testSetAndGetDescription() {
        String description = "这是一个测试描述";
        searchResult.setDescription(description);
        assertEquals(description, searchResult.getDescription());
    }

    @Test
    void testSetAndGetTitleWithNull() {
        searchResult.setTitle(null);
        assertNull(searchResult.getTitle());
    }

    @Test
    void testSetAndGetUrlWithNull() {
        searchResult.setUrl(null);
        assertNull(searchResult.getUrl());
    }

    @Test
    void testSetAndGetDescriptionWithNull() {
        searchResult.setDescription(null);
        assertNull(searchResult.getDescription());
    }

    @Test
    void testSetAndGetTitleWithEmptyString() {
        searchResult.setTitle("");
        assertEquals("", searchResult.getTitle());
    }

    @Test
    void testSetAndGetUrlWithEmptyString() {
        searchResult.setUrl("");
        assertEquals("", searchResult.getUrl());
    }

    @Test
    void testSetAndGetDescriptionWithEmptyString() {
        searchResult.setDescription("");
        assertEquals("", searchResult.getDescription());
    }

    @Test
    void testSetAndGetTitleWithWhitespace() {
        searchResult.setTitle("   ");
        assertEquals("   ", searchResult.getTitle());
    }

    @Test
    void testSetAndGetUrlWithSpecialCharacters() {
        String url = "https://test.com/path?param=value&param2=value2";
        searchResult.setUrl(url);
        assertEquals(url, searchResult.getUrl());
    }

    @Test
    void testSetAndGetDescriptionWithLongText() {
        String longDescription = "这是一个非常长的描述文本，包含了多个句子。" +
                "这个描述可能会很长，用来测试长文本的处理能力。" +
                "确保系统能够正确处理各种长度的描述内容。";
        searchResult.setDescription(longDescription);
        assertEquals(longDescription, searchResult.getDescription());
    }
} 