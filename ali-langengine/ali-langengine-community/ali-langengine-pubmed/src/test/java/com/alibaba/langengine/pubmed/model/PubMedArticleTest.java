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
package com.alibaba.langengine.pubmed.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("PubMed Article Model Tests")
class PubMedArticleTest {

    private PubMedArticle article;
    private PubMedArticle.Author author1;
    private PubMedArticle.Author author2;

    @BeforeEach
    void setUp() {
        author1 = PubMedArticle.Author.builder()
                .firstName("John")
                .lastName("Doe")
                .initials("JD")
                .affiliation("University of Test")
                .build();

        author2 = PubMedArticle.Author.builder()
                .firstName("Jane")
                .lastName("Smith")
                .initials("JS")
                .affiliation("Test Institute")
                .build();

        article = PubMedArticle.builder()
                .pmid("12345678")
                .title("Test Article Title")
                .abstractText("This is a test abstract for the article.")
                .authors(Arrays.asList(author1, author2))
                .journal("Test Journal")
                .publishDate("2023-01-15")
                .doi("10.1000/test.doi")
                .keywords(Arrays.asList("test", "article", "pubmed"))
                .meshTerms(Arrays.asList("Test Term 1", "Test Term 2"))
                .articleType("Research Article")
                .language("eng")
                .country("United States")
                .build();
    }

    @Test
    @DisplayName("测试作者全名生成")
    void testAuthorFullName() {
        assertEquals("John Doe", author1.getFullName());
        assertEquals("Jane Smith", author2.getFullName());
        
        // 测试只有姓氏的情况
        PubMedArticle.Author onlyLastName = PubMedArticle.Author.builder()
                .lastName("Johnson")
                .build();
        assertEquals("Johnson", onlyLastName.getFullName());
        
        // 测试只有姓氏和缩写的情况
        PubMedArticle.Author withInitials = PubMedArticle.Author.builder()
                .lastName("Brown")
                .initials("AB")
                .build();
        assertEquals("AB Brown", withInitials.getFullName());
        
        // 测试空作者
        PubMedArticle.Author emptyAuthor = new PubMedArticle.Author();
        assertEquals("Unknown Author", emptyAuthor.getFullName());
    }

    @Test
    @DisplayName("测试发表日期解析")
    void testPublishDateParsing() {
        // 测试标准日期格式
        assertEquals(LocalDate.of(2023, 1, 15), article.getParsedPublishDate());
        
        // 测试不同日期格式
        article.setPublishDate("2023/01/15");
        assertEquals(LocalDate.of(2023, 1, 15), article.getParsedPublishDate());
        
        article.setPublishDate("2023-01");
        assertEquals(LocalDate.of(2023, 1, 1), article.getParsedPublishDate());
        
        article.setPublishDate("2023");
        assertEquals(LocalDate.of(2023, 1, 1), article.getParsedPublishDate());
        
        // 测试无效日期
        article.setPublishDate("invalid-date");
        assertNull(article.getParsedPublishDate());
        
        // 测试空日期
        article.setPublishDate(null);
        assertNull(article.getParsedPublishDate());
        
        article.setPublishDate("");
        assertNull(article.getParsedPublishDate());
    }

    @Test
    @DisplayName("测试作者名称列表")
    void testAuthorNames() {
        List<String> authorNames = article.getAuthorNames();
        assertEquals(2, authorNames.size());
        assertEquals("John Doe", authorNames.get(0));
        assertEquals("Jane Smith", authorNames.get(1));
        
        // 测试空作者列表
        article.setAuthors(null);
        assertTrue(article.getAuthorNames().isEmpty());
    }

    @Test
    @DisplayName("测试PubMed URL生成")
    void testPubMedUrl() {
        assertEquals("https://pubmed.ncbi.nlm.nih.gov/12345678/", article.getPubMedUrl());
        
        // 测试已设置URL的情况
        article.setUrl("https://custom.url/article");
        assertEquals("https://custom.url/article", article.getPubMedUrl());
        
        // 测试无PMID的情况
        article.setUrl(null);
        article.setPmid(null);
        assertNull(article.getPubMedUrl());
        
        // 测试空PMID的情况
        article.setPmid("");
        assertNull(article.getPubMedUrl());
    }

    @Test
    @DisplayName("测试完整信息检查")
    void testHasCompleteInformation() {
        assertTrue(article.hasCompleteInformation());
        
        // 测试缺少标题
        article.setTitle(null);
        assertFalse(article.hasCompleteInformation());
        
        article.setTitle("Test Title");
        article.setAbstractText(null);
        assertFalse(article.hasCompleteInformation());
        
        article.setAbstractText("Test Abstract");
        article.setPmid(null);
        assertFalse(article.hasCompleteInformation());
        
        // 测试空字符串
        article.setPmid("");
        assertFalse(article.hasCompleteInformation());
    }

    @Test
    @DisplayName("测试格式化引用")
    void testFormattedCitation() {
        String citation = article.getFormattedCitation();
        
        assertNotNull(citation);
        assertTrue(citation.contains("John Doe, Jane Smith"));
        assertTrue(citation.contains("Test Article Title"));
        assertTrue(citation.contains("Test Journal"));
        assertTrue(citation.contains("2023-01-15"));
        assertTrue(citation.contains("PMID: 12345678"));
        
        // 测试单个作者
        article.setAuthors(Arrays.asList(author1));
        citation = article.getFormattedCitation();
        assertTrue(citation.contains("John Doe."));
        assertFalse(citation.contains("Jane Smith"));
        
        // 测试多于3个作者
        PubMedArticle.Author author3 = PubMedArticle.Author.builder()
                .firstName("Bob")
                .lastName("Wilson")
                .build();
        PubMedArticle.Author author4 = PubMedArticle.Author.builder()
                .firstName("Alice")
                .lastName("Brown")
                .build();
        article.setAuthors(Arrays.asList(author1, author2, author3, author4));
        citation = article.getFormattedCitation();
        assertTrue(citation.contains("John Doe et al."));
        
        // 测试无作者
        article.setAuthors(null);
        citation = article.getFormattedCitation();
        assertFalse(citation.contains("John Doe"));
    }

    @Test
    @DisplayName("测试搜索文本生成")
    void testSearchableText() {
        String searchText = article.getSearchableText();
        
        assertNotNull(searchText);
        assertTrue(searchText.contains("Test Article Title"));
        assertTrue(searchText.contains("This is a test abstract"));
        assertTrue(searchText.contains("test article pubmed"));
        assertTrue(searchText.contains("Test Term 1 Test Term 2"));
        assertTrue(searchText.contains("John Doe Jane Smith"));
        assertTrue(searchText.contains("Test Journal"));
        
        // 测试空字段
        article.setTitle(null);
        article.setAbstractText(null);
        article.setKeywords(null);
        article.setMeshTerms(null);
        article.setAuthors(null);
        article.setJournal(null);
        
        searchText = article.getSearchableText();
        assertEquals("", searchText);
    }

    @Test
    @DisplayName("测试Builder模式")
    void testBuilderPattern() {
        PubMedArticle builtArticle = PubMedArticle.builder()
                .pmid("87654321")
                .title("Built Article")
                .abstractText("Built abstract")
                .build();
        
        assertEquals("87654321", builtArticle.getPmid());
        assertEquals("Built Article", builtArticle.getTitle());
        assertEquals("Built abstract", builtArticle.getAbstractText());
        assertNotNull(builtArticle.getAuthors());
        assertTrue(builtArticle.getAuthors().isEmpty());
    }

    @Test
    @DisplayName("测试默认值")
    void testDefaultValues() {
        PubMedArticle emptyArticle = new PubMedArticle();
        
        assertNotNull(emptyArticle.getAuthors());
        assertTrue(emptyArticle.getAuthors().isEmpty());
        assertNotNull(emptyArticle.getKeywords());
        assertTrue(emptyArticle.getKeywords().isEmpty());
        assertNotNull(emptyArticle.getMeshTerms());
        assertTrue(emptyArticle.getMeshTerms().isEmpty());
    }

    @Test
    @DisplayName("测试边界情况")
    void testEdgeCases() {
        // 测试空白字符串
        article.setTitle("   ");
        article.setAbstractText("   ");
        article.setPmid("   ");
        
        assertFalse(article.hasCompleteInformation());
        
        // 测试标题不以句号结尾的引用格式
        article.setTitle("Title without period");
        String citation = article.getFormattedCitation();
        assertTrue(citation.contains("Title without period."));
        
        // 测试已有句号的标题
        article.setTitle("Title with period.");
        citation = article.getFormattedCitation();
        assertTrue(citation.contains("Title with period."));
        assertFalse(citation.contains("Title with period.."));
    }
}
