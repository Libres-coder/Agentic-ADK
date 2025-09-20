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

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("PubMed Search Request Tests")
class PubMedSearchRequestTest {

    private PubMedSearchRequest request;

    @BeforeEach
    void setUp() {
        request = PubMedSearchRequest.builder()
                .query("cancer treatment")
                .limit(20)
                .offset(0)
                .build();
    }

    @Test
    @DisplayName("测试基本请求验证")
    void testBasicRequestValidation() {
        assertTrue(request.isValid());
        
        // 测试空查询
        request.setQuery(null);
        assertFalse(request.isValid());
        
        request.setQuery("");
        assertFalse(request.isValid());
        
        request.setQuery("   ");
        assertFalse(request.isValid());
        
        // 测试无效限制
        request.setQuery("valid query");
        request.setLimit(0);
        assertFalse(request.isValid());
        
        request.setLimit(-1);
        assertFalse(request.isValid());
        
        request.setLimit(PubMedSearchRequest.MAX_LIMIT + 1);
        assertFalse(request.isValid());
        
        // 测试无效偏移量
        request.setLimit(20);
        request.setOffset(-1);
        assertFalse(request.isValid());
    }

    @Test
    @DisplayName("测试日期验证")
    void testDateValidation() {
        // 测试有效日期格式
        request.setStartDate("2023-01-01");
        request.setEndDate("2023-12-31");
        assertTrue(request.isValid());
        
        request.setStartDate("2023/01/01");
        request.setEndDate("2023/12/31");
        assertTrue(request.isValid());
        
        request.setStartDate("2023-01");
        request.setEndDate("2023-12");
        assertTrue(request.isValid());
        
        request.setStartDate("2023");
        request.setEndDate("2024");
        assertTrue(request.isValid());
        
        // 测试无效日期格式
        request.setStartDate("invalid-date");
        assertFalse(request.isValid());
        
        request.setStartDate("2023-01-01");
        request.setEndDate("invalid-date");
        assertFalse(request.isValid());
    }

    @Test
    @DisplayName("测试完整查询构建")
    void testFullQueryBuilding() {
        String fullQuery = request.buildFullQuery();
        assertEquals("cancer treatment", fullQuery);
        
        // 测试带字段的查询
        request.setField("Title");
        fullQuery = request.buildFullQuery();
        assertEquals("(cancer treatment)[Title]", fullQuery);
        
        // 测试添加作者过滤
        request.setAuthor("Smith J");
        fullQuery = request.buildFullQuery();
        assertTrue(fullQuery.contains("(Smith J)[Author]"));
        
        // 测试添加期刊过滤
        request.setJournal("Nature");
        fullQuery = request.buildFullQuery();
        assertTrue(fullQuery.contains("(Nature)[Journal]"));
        
        // 测试添加文章类型过滤
        request.setArticleType("Review");
        fullQuery = request.buildFullQuery();
        assertTrue(fullQuery.contains("(Review)[Publication Type]"));
        
        // 测试添加语言过滤
        request.setLanguage("eng");
        fullQuery = request.buildFullQuery();
        assertTrue(fullQuery.contains("(eng)[Language]"));
        
        // 测试日期范围过滤
        request.setStartDate("2020-01-01");
        request.setEndDate("2023-12-31");
        fullQuery = request.buildFullQuery();
        assertTrue(fullQuery.contains("\"2020-01-01\"[Date - Publication] : \"2023-12-31\"[Date - Publication]"));
    }

    @Test
    @DisplayName("测试日期范围查询")
    void testDateRangeQuery() {
        // 测试只有开始日期
        request.setStartDate("2020-01-01");
        String fullQuery = request.buildFullQuery();
        assertTrue(fullQuery.contains("\"2020-01-01\"[Date - Publication] : \"3000/12/31\"[Date - Publication]"));
        
        // 测试只有结束日期
        request.setStartDate(null);
        request.setEndDate("2023-12-31");
        fullQuery = request.buildFullQuery();
        assertTrue(fullQuery.contains("\"1900/01/01\"[Date - Publication] : \"2023-12-31\"[Date - Publication]"));
        
        // 测试完整日期范围
        request.setStartDate("2020-01-01");
        request.setEndDate("2023-12-31");
        fullQuery = request.buildFullQuery();
        assertTrue(fullQuery.contains("\"2020-01-01\"[Date - Publication] : \"2023-12-31\"[Date - Publication]"));
    }

    @Test
    @DisplayName("测试ESearch参数生成")
    void testESearchParams() {
        String params = request.getESearchParams();
        
        assertTrue(params.contains("db=pubmed"));
        assertTrue(params.contains("retstart=0"));
        assertTrue(params.contains("retmax=20"));
        assertTrue(params.contains("retmode=xml"));
        assertTrue(params.contains("sort=relevance"));
        
        // 测试自定义参数
        request.setOffset(10);
        request.setLimit(50);
        request.setSort("date");
        request.setDatabase("pmc");
        
        params = request.getESearchParams();
        assertTrue(params.contains("db=pmc"));
        assertTrue(params.contains("retstart=10"));
        assertTrue(params.contains("retmax=50"));
        assertTrue(params.contains("sort=date"));
    }

    @Test
    @DisplayName("测试静态工厂方法")
    void testStaticFactoryMethods() {
        // 测试简单搜索
        PubMedSearchRequest simpleRequest = PubMedSearchRequest.simple("diabetes");
        assertEquals("diabetes", simpleRequest.getQuery());
        assertEquals(PubMedSearchRequest.DEFAULT_LIMIT, simpleRequest.getLimit());
        
        // 测试带限制的搜索
        PubMedSearchRequest limitedRequest = PubMedSearchRequest.withLimit("cancer", 50);
        assertEquals("cancer", limitedRequest.getQuery());
        assertEquals(50, limitedRequest.getLimit());
        
        // 测试超过最大限制
        PubMedSearchRequest maxLimitRequest = PubMedSearchRequest.withLimit("test", 300);
        assertEquals(PubMedSearchRequest.MAX_LIMIT, maxLimitRequest.getLimit());
        
        // 测试日期范围搜索
        LocalDate start = LocalDate.of(2020, 1, 1);
        LocalDate end = LocalDate.of(2023, 12, 31);
        PubMedSearchRequest dateRequest = PubMedSearchRequest.withDateRange("covid", start, end);
        assertEquals("covid", dateRequest.getQuery());
        assertEquals("2020/01/01", dateRequest.getStartDate());
        assertEquals("2023/12/31", dateRequest.getEndDate());
        
        // 测试作者搜索
        PubMedSearchRequest authorRequest = PubMedSearchRequest.withAuthor("machine learning", "Smith J");
        assertEquals("machine learning", authorRequest.getQuery());
        assertEquals("Smith J", authorRequest.getAuthor());
        
        // 测试期刊搜索
        PubMedSearchRequest journalRequest = PubMedSearchRequest.withJournal("artificial intelligence", "Nature");
        assertEquals("artificial intelligence", journalRequest.getQuery());
        assertEquals("Nature", journalRequest.getJournal());
    }

    @Test
    @DisplayName("测试日期范围工厂方法边界情况")
    void testDateRangeFactoryEdgeCases() {
        // 测试null开始日期
        PubMedSearchRequest request1 = PubMedSearchRequest.withDateRange("test", null, LocalDate.of(2023, 12, 31));
        assertEquals("test", request1.getQuery());
        assertNull(request1.getStartDate());
        assertEquals("2023/12/31", request1.getEndDate());
        
        // 测试null结束日期
        PubMedSearchRequest request2 = PubMedSearchRequest.withDateRange("test", LocalDate.of(2020, 1, 1), null);
        assertEquals("test", request2.getQuery());
        assertEquals("2020/01/01", request2.getStartDate());
        assertNull(request2.getEndDate());
        
        // 测试都为null
        PubMedSearchRequest request3 = PubMedSearchRequest.withDateRange("test", null, null);
        assertEquals("test", request3.getQuery());
        assertNull(request3.getStartDate());
        assertNull(request3.getEndDate());
    }

    @Test
    @DisplayName("测试默认值")
    void testDefaultValues() {
        PubMedSearchRequest defaultRequest = PubMedSearchRequest.builder()
                .query("test")
                .build();
        
        assertEquals(PubMedSearchRequest.DEFAULT_LIMIT, defaultRequest.getLimit());
        assertEquals(0, defaultRequest.getOffset());
        assertEquals("relevance", defaultRequest.getSort());
        assertEquals("pubmed", defaultRequest.getDatabase());
    }

    @Test
    @DisplayName("测试边界值")
    void testBoundaryValues() {
        // 测试最小有效限制
        request.setLimit(1);
        assertTrue(request.isValid());
        
        // 测试最大有效限制
        request.setLimit(PubMedSearchRequest.MAX_LIMIT);
        assertTrue(request.isValid());
        
        // 测试最小有效偏移量
        request.setOffset(0);
        assertTrue(request.isValid());
        
        // 测试大偏移量
        request.setOffset(Integer.MAX_VALUE);
        assertTrue(request.isValid());
    }

    @Test
    @DisplayName("测试复杂查询构建")
    void testComplexQueryBuilding() {
        request = PubMedSearchRequest.builder()
                .query("machine learning")
                .field("Title/Abstract")
                .author("Smith J")
                .journal("Nature")
                .articleType("Review")
                .language("eng")
                .startDate("2020-01-01")
                .endDate("2023-12-31")
                .build();
        
        String fullQuery = request.buildFullQuery();
        
        assertTrue(fullQuery.contains("(machine learning)[Title/Abstract]"));
        assertTrue(fullQuery.contains("(Smith J)[Author]"));
        assertTrue(fullQuery.contains("(Nature)[Journal]"));
        assertTrue(fullQuery.contains("(Review)[Publication Type]"));
        assertTrue(fullQuery.contains("(eng)[Language]"));
        assertTrue(fullQuery.contains("\"2020-01-01\"[Date - Publication] : \"2023-12-31\"[Date - Publication]"));
        
        // 验证AND连接符
        long andCount = fullQuery.chars().filter(ch -> ch == '&').count() + 
                       (fullQuery.split(" AND ").length - 1);
        assertTrue(andCount >= 5); // 应该有多个AND连接符
    }

    @Test
    @DisplayName("测试URL编码处理")
    void testUrlEncoding() {
        request.setQuery("cancer & treatment");
        String params = request.getESearchParams();
        
        // 参数应该包含URL编码后的查询
        assertTrue(params.contains("term="));
        assertFalse(params.contains("cancer & treatment")); // 原始字符应该被编码
    }
}
