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
package com.alibaba.langengine.netflix;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Properties;

/**
 * NetflixConfiguration集成测试类
 * 测试配置类的系统属性读取、默认值设置和构造函数初始化
 * 
 * @author aihe.ah
 * @time 2024/12/19
 */
public class NetflixConfigurationIntegrationTest {

    private Properties originalProperties;
    private NetflixConfiguration netflixConfiguration;

    @BeforeEach
    public void setUp() {
        // 保存原始系统属性
        originalProperties = new Properties();
        originalProperties.putAll(System.getProperties());
        
        // 创建配置实例
        netflixConfiguration = new NetflixConfiguration();
    }

    @AfterEach
    public void tearDown() {
        // 恢复原始系统属性
        System.setProperties(originalProperties);
    }

    @Test
    public void testDefaultConfigurationValues() {
        // 测试默认配置值
        Assertions.assertEquals("https://api.themoviedb.org/3", netflixConfiguration.getApiBaseUrl());
        Assertions.assertEquals("https://api.themoviedb.org/3/search/multi", netflixConfiguration.getSearchUrl());
        Assertions.assertEquals("https://api.themoviedb.org/3/trending/all/day", netflixConfiguration.getTrendingUrl());
        Assertions.assertEquals("https://api.themoviedb.org/3/discover/movie", netflixConfiguration.getDiscoverUrl());
        Assertions.assertEquals(30, netflixConfiguration.getTimeout());
        Assertions.assertEquals(1, netflixConfiguration.getDefaultPage());
        Assertions.assertEquals(20, netflixConfiguration.getDefaultPageSize());
        Assertions.assertEquals(100, netflixConfiguration.getMaxPageSize());
        Assertions.assertEquals("zh-CN", netflixConfiguration.getDefaultLanguage());
        Assertions.assertEquals("CN", netflixConfiguration.getDefaultRegion());
    }

    @Test
    public void testCustomConfigurationValues() {
        // 设置自定义系统属性
        System.setProperty("netflix.api.key", "test-api-key");
        System.setProperty("netflix.api.base.url", "https://custom.api.com");
        System.setProperty("netflix.search.url", "https://custom.api.com/search");
        System.setProperty("netflix.trending.url", "https://custom.api.com/trending");
        System.setProperty("netflix.discover.url", "https://custom.api.com/discover");
        System.setProperty("netflix.timeout", "60");
        System.setProperty("netflix.default.page", "2");
        System.setProperty("netflix.default.page.size", "50");
        System.setProperty("netflix.max.page.size", "200");
        System.setProperty("netflix.default.language", "en-US");
        System.setProperty("netflix.default.region", "US");

        // 创建新的配置实例
        NetflixConfiguration customConfig = new NetflixConfiguration();

        // 验证自定义值
        Assertions.assertEquals("test-api-key", customConfig.getApiKey());
        Assertions.assertEquals("https://custom.api.com", customConfig.getApiBaseUrl());
        Assertions.assertEquals("https://custom.api.com/search", customConfig.getSearchUrl());
        Assertions.assertEquals("https://custom.api.com/trending", customConfig.getTrendingUrl());
        Assertions.assertEquals("https://custom.api.com/discover", customConfig.getDiscoverUrl());
        Assertions.assertEquals(60, customConfig.getTimeout());
        Assertions.assertEquals(2, customConfig.getDefaultPage());
        Assertions.assertEquals(50, customConfig.getDefaultPageSize());
        Assertions.assertEquals(200, customConfig.getMaxPageSize());
        Assertions.assertEquals("en-US", customConfig.getDefaultLanguage());
        Assertions.assertEquals("US", customConfig.getDefaultRegion());
    }

    @Test
    public void testPartialCustomConfiguration() {
        // 只设置部分自定义属性
        System.setProperty("netflix.timeout", "45");
        System.setProperty("netflix.default.language", "ja-JP");

        NetflixConfiguration partialConfig = new NetflixConfiguration();

        // 验证自定义值
        Assertions.assertEquals(45, partialConfig.getTimeout());
        Assertions.assertEquals("ja-JP", partialConfig.getDefaultLanguage());

        // 验证其他值保持默认
        Assertions.assertEquals("https://api.themoviedb.org/3", partialConfig.getApiBaseUrl());
        Assertions.assertEquals(1, partialConfig.getDefaultPage());
        Assertions.assertEquals(20, partialConfig.getDefaultPageSize());
        Assertions.assertEquals("CN", partialConfig.getDefaultRegion());
    }

    @Test
    public void testInvalidNumericProperties() {
        // 测试无效的数字属性值
        System.setProperty("netflix.timeout", "invalid");
        System.setProperty("netflix.default.page", "not-a-number");
        System.setProperty("netflix.default.page.size", "abc");
        System.setProperty("netflix.max.page.size", "xyz");

        // 应该抛出NumberFormatException
        Assertions.assertThrows(NumberFormatException.class, () -> {
            new NetflixConfiguration();
        });
    }

    @Test
    public void testEmptyStringProperties() {
        // 测试空字符串属性
        System.setProperty("netflix.api.key", "");
        System.setProperty("netflix.default.language", "");
        System.setProperty("netflix.default.region", "");

        NetflixConfiguration emptyConfig = new NetflixConfiguration();

        // 验证空字符串值
        Assertions.assertEquals("", emptyConfig.getApiKey());
        Assertions.assertEquals("", emptyConfig.getDefaultLanguage());
        Assertions.assertEquals("", emptyConfig.getDefaultRegion());
    }

    @Test
    public void testNullApiKey() {
        // 测试API Key为null的情况
        System.clearProperty("netflix.api.key");
        
        NetflixConfiguration nullKeyConfig = new NetflixConfiguration();
        
        // API Key应该为null
        Assertions.assertNull(nullKeyConfig.getApiKey());
        
        // 其他配置应该正常
        Assertions.assertEquals("https://api.themoviedb.org/3", nullKeyConfig.getApiBaseUrl());
        Assertions.assertEquals(30, nullKeyConfig.getTimeout());
    }

    @Test
    public void testConfigurationGettersAndSetters() {
        // 测试getter和setter方法
        netflixConfiguration.setApiKey("test-key");
        netflixConfiguration.setTimeout(120);
        netflixConfiguration.setDefaultPageSize(30);
        netflixConfiguration.setDefaultLanguage("fr-FR");

        Assertions.assertEquals("test-key", netflixConfiguration.getApiKey());
        Assertions.assertEquals(120, netflixConfiguration.getTimeout());
        Assertions.assertEquals(30, netflixConfiguration.getDefaultPageSize());
        Assertions.assertEquals("fr-FR", netflixConfiguration.getDefaultLanguage());
    }

    @Test
    public void testStaticConstants() {
        // 测试静态常量
        Assertions.assertNotNull(NetflixConfiguration.NETFLIX_API_BASE_URL);
        Assertions.assertNotNull(NetflixConfiguration.NETFLIX_SEARCH_URL);
        Assertions.assertNotNull(NetflixConfiguration.NETFLIX_TRENDING_URL);
        Assertions.assertNotNull(NetflixConfiguration.NETFLIX_DISCOVER_URL);
        Assertions.assertTrue(NetflixConfiguration.NETFLIX_TIMEOUT > 0);
        Assertions.assertTrue(NetflixConfiguration.DEFAULT_PAGE > 0);
        Assertions.assertTrue(NetflixConfiguration.DEFAULT_PAGE_SIZE > 0);
        Assertions.assertTrue(NetflixConfiguration.MAX_PAGE_SIZE > 0);
        Assertions.assertNotNull(NetflixConfiguration.DEFAULT_LANGUAGE);
        Assertions.assertNotNull(NetflixConfiguration.DEFAULT_REGION);
    }

    @Test
    public void testConfigurationConsistency() {
        // 测试配置一致性
        NetflixConfiguration config1 = new NetflixConfiguration();
        NetflixConfiguration config2 = new NetflixConfiguration();

        // 两个实例的默认值应该相同
        Assertions.assertEquals(config1.getApiBaseUrl(), config2.getApiBaseUrl());
        Assertions.assertEquals(config1.getSearchUrl(), config2.getSearchUrl());
        Assertions.assertEquals(config1.getTrendingUrl(), config2.getTrendingUrl());
        Assertions.assertEquals(config1.getDiscoverUrl(), config2.getDiscoverUrl());
        Assertions.assertEquals(config1.getTimeout(), config2.getTimeout());
        Assertions.assertEquals(config1.getDefaultPage(), config2.getDefaultPage());
        Assertions.assertEquals(config1.getDefaultPageSize(), config2.getDefaultPageSize());
        Assertions.assertEquals(config1.getMaxPageSize(), config2.getMaxPageSize());
        Assertions.assertEquals(config1.getDefaultLanguage(), config2.getDefaultLanguage());
        Assertions.assertEquals(config1.getDefaultRegion(), config2.getDefaultRegion());
    }
}
