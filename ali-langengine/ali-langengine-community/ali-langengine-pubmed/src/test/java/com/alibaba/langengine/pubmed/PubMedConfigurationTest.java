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
package com.alibaba.langengine.pubmed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("PubMed Configuration Tests")
class PubMedConfigurationTest {

    private PubMedConfiguration configuration;

    @BeforeEach
    void setUp() {
        // 清除环境变量影响
        System.clearProperty("PUBMED_ESEARCH_BASE_URL");
        System.clearProperty("PUBMED_EFETCH_BASE_URL");
        configuration = new PubMedConfiguration();
    }

    @Test
    @DisplayName("默认配置应该有效")
    void testDefaultConfiguration() {
        assertTrue(configuration.validateConfiguration());
        assertNotNull(configuration.getESearchBaseUrl());
        assertNotNull(configuration.getEFetchBaseUrl());
        assertNotNull(configuration.getUserAgent());
        assertEquals(PubMedConfiguration.DEFAULT_USER_AGENT, configuration.getUserAgent());
        assertEquals(30, configuration.getTimeoutSeconds());
        assertEquals(3, configuration.getMaxRetries());
        assertEquals(1000, configuration.getRequestIntervalMs());
    }

    @Test
    @DisplayName("测试环境变量配置")
    void testEnvironmentVariableConfiguration() {
        // 测试自定义URL
        configuration.setESearchBaseUrl("https://custom.search.url");
        configuration.setEFetchBaseUrl("https://custom.fetch.url");
        
        assertTrue(configuration.validateConfiguration());
        assertEquals("https://custom.search.url", configuration.getESearchBaseUrl());
        assertEquals("https://custom.fetch.url", configuration.getEFetchBaseUrl());
    }

    @Test
    @DisplayName("测试邮箱和API Key配置")
    void testEmailAndApiKeyConfiguration() {
        configuration.setEmail("test@example.com");
        configuration.setApiKey("test-api-key");
        
        assertTrue(configuration.hasEmail());
        assertTrue(configuration.hasApiKey());
        assertEquals("test@example.com", configuration.getEmail());
        assertEquals("test-api-key", configuration.getApiKey());
    }

    @Test
    @DisplayName("测试自定义User Agent")
    void testCustomUserAgent() {
        configuration.setUserAgent("custom-user-agent/1.0");
        
        assertTrue(configuration.hasValidUserAgent());
        assertEquals("custom-user-agent/1.0", configuration.getUserAgent());
    }

    @Test
    @DisplayName("测试超时配置")
    void testTimeoutConfiguration() {
        configuration.setTimeoutSeconds(60);
        configuration.setMaxRetries(5);
        configuration.setRequestIntervalMs(2000);
        
        assertTrue(configuration.validateConfiguration());
        assertEquals(60, configuration.getTimeoutSeconds());
        assertEquals(5, configuration.getMaxRetries());
        assertEquals(2000, configuration.getRequestIntervalMs());
    }

    @Test
    @DisplayName("测试无效配置")
    void testInvalidConfiguration() {
        // 测试空URL
        configuration.setESearchBaseUrl("");
        assertFalse(configuration.validateConfiguration());
        
        configuration.setESearchBaseUrl("https://valid.url");
        configuration.setEFetchBaseUrl("");
        assertFalse(configuration.validateConfiguration());
        
        // 测试空User Agent
        configuration.setEFetchBaseUrl("https://valid.url");
        configuration.setUserAgent("");
        assertFalse(configuration.validateConfiguration());
        
        // 测试负数超时
        configuration.setUserAgent("valid-agent");
        configuration.setTimeoutSeconds(-1);
        assertFalse(configuration.validateConfiguration());
        
        // 测试负数重试次数
        configuration.setTimeoutSeconds(30);
        configuration.setMaxRetries(-1);
        assertFalse(configuration.validateConfiguration());
        
        // 测试负数请求间隔
        configuration.setMaxRetries(3);
        configuration.setRequestIntervalMs(-1);
        assertFalse(configuration.validateConfiguration());
    }

    @Test
    @DisplayName("测试配置摘要")
    void testConfigurationSummary() {
        configuration.setEmail("test@example.com");
        String summary = configuration.getConfigurationSummary();
        
        assertNotNull(summary);
        assertTrue(summary.contains("PubMed Configuration"));
        assertTrue(summary.contains("test@example.com"));
    }

    @Test
    @DisplayName("测试没有邮箱的配置摘要")
    void testConfigurationSummaryWithoutEmail() {
        String summary = configuration.getConfigurationSummary();
        
        assertNotNull(summary);
        assertTrue(summary.contains("PubMed Configuration"));
        assertTrue(summary.contains("not set"));
    }

    @Test
    @DisplayName("测试边界值")
    void testBoundaryValues() {
        // 测试最小有效值
        configuration.setTimeoutSeconds(1);
        configuration.setMaxRetries(0);
        configuration.setRequestIntervalMs(0);
        
        assertTrue(configuration.validateConfiguration());
        
        // 测试大值
        configuration.setTimeoutSeconds(Integer.MAX_VALUE);
        configuration.setMaxRetries(Integer.MAX_VALUE);
        configuration.setRequestIntervalMs(Integer.MAX_VALUE);
        
        assertTrue(configuration.validateConfiguration());
    }

    @Test
    @DisplayName("测试null值处理")
    void testNullValues() {
        configuration.setESearchBaseUrl(null);
        assertFalse(configuration.validateConfiguration());
        
        configuration.setESearchBaseUrl("https://valid.url");
        configuration.setEFetchBaseUrl(null);
        assertFalse(configuration.validateConfiguration());
        
        configuration.setEFetchBaseUrl("https://valid.url");
        configuration.setUserAgent(null);
        assertFalse(configuration.validateConfiguration());
    }

    @Test
    @DisplayName("测试默认数据库配置")
    void testDefaultDatabaseConfiguration() {
        assertEquals("pubmed", configuration.getDefaultDatabase());
        assertEquals("xml", configuration.getDefaultReturnType());
        assertEquals("xml", configuration.getDefaultReturnMode());
    }

    @Test
    @DisplayName("测试布尔检查方法")
    void testBooleanMethods() {
        // 默认状态
        assertFalse(configuration.hasValidUserAgent());
        assertFalse(configuration.hasApiKey());
        assertFalse(configuration.hasEmail());
        
        // 设置值后
        configuration.setUserAgent("custom-agent");
        configuration.setApiKey("test-key");
        configuration.setEmail("test@example.com");
        
        assertTrue(configuration.hasValidUserAgent());
        assertTrue(configuration.hasApiKey());
        assertTrue(configuration.hasEmail());
    }
}
