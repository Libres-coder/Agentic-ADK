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
package com.alibaba.langengine.spotify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Properties;

/**
 * SpotifyConfiguration集成测试类
 * 测试配置类的系统属性读取、默认值设置和构造函数初始化
 * 
 * @author aihe.ah
 * @time 2024/12/19
 */
public class SpotifyConfigurationIntegrationTest {

    private Properties originalProperties;
    private SpotifyConfiguration spotifyConfiguration;

    @BeforeEach
    public void setUp() {
        // 保存原始系统属性
        originalProperties = new Properties();
        originalProperties.putAll(System.getProperties());
        
        // 创建配置实例
        spotifyConfiguration = new SpotifyConfiguration();
    }

    @AfterEach
    public void tearDown() {
        // 恢复原始系统属性
        System.setProperties(originalProperties);
    }

    @Test
    public void testDefaultConfigurationValues() {
        // 测试默认配置值
        Assertions.assertEquals("http://localhost:8080/callback", spotifyConfiguration.getRedirectUri());
        Assertions.assertEquals("https://api.spotify.com/v1", spotifyConfiguration.getApiBaseUrl());
        Assertions.assertEquals("https://accounts.spotify.com/api/token", spotifyConfiguration.getAuthUrl());
        Assertions.assertEquals(30, spotifyConfiguration.getTimeout());
        Assertions.assertEquals(20, spotifyConfiguration.getDefaultLimit());
        Assertions.assertEquals(50, spotifyConfiguration.getMaxLimit());
    }

    @Test
    public void testCustomConfigurationValues() {
        // 设置自定义系统属性
        System.setProperty("spotify.client.id", "test-client-id");
        System.setProperty("spotify.client.secret", "test-client-secret");
        System.setProperty("spotify.redirect.uri", "https://custom.redirect.com/callback");
        System.setProperty("spotify.api.base.url", "https://custom.api.com/v1");
        System.setProperty("spotify.auth.url", "https://custom.auth.com/token");
        System.setProperty("spotify.timeout", "60");
        System.setProperty("spotify.default.limit", "30");
        System.setProperty("spotify.max.limit", "100");

        // 创建新的配置实例
        SpotifyConfiguration customConfig = new SpotifyConfiguration();

        // 验证自定义值
        Assertions.assertEquals("test-client-id", customConfig.getClientId());
        Assertions.assertEquals("test-client-secret", customConfig.getClientSecret());
        Assertions.assertEquals("https://custom.redirect.com/callback", customConfig.getRedirectUri());
        Assertions.assertEquals("https://custom.api.com/v1", customConfig.getApiBaseUrl());
        Assertions.assertEquals("https://custom.auth.com/token", customConfig.getAuthUrl());
        Assertions.assertEquals(60, customConfig.getTimeout());
        Assertions.assertEquals(30, customConfig.getDefaultLimit());
        Assertions.assertEquals(100, customConfig.getMaxLimit());
    }

    @Test
    public void testPartialCustomConfiguration() {
        // 只设置部分自定义属性
        System.setProperty("spotify.timeout", "45");
        System.setProperty("spotify.default.limit", "25");

        SpotifyConfiguration partialConfig = new SpotifyConfiguration();

        // 验证自定义值
        Assertions.assertEquals(45, partialConfig.getTimeout());
        Assertions.assertEquals(25, partialConfig.getDefaultLimit());

        // 验证其他值保持默认
        Assertions.assertEquals("http://localhost:8080/callback", partialConfig.getRedirectUri());
        Assertions.assertEquals("https://api.spotify.com/v1", partialConfig.getApiBaseUrl());
        Assertions.assertEquals(50, partialConfig.getMaxLimit());
    }

    @Test
    public void testInvalidNumericProperties() {
        // 测试无效的数字属性值
        System.setProperty("spotify.timeout", "invalid");
        System.setProperty("spotify.default.limit", "not-a-number");
        System.setProperty("spotify.max.limit", "abc");

        // 应该抛出NumberFormatException
        Assertions.assertThrows(NumberFormatException.class, () -> {
            new SpotifyConfiguration();
        });
    }

    @Test
    public void testEmptyStringProperties() {
        // 测试空字符串属性
        System.setProperty("spotify.client.id", "");
        System.setProperty("spotify.client.secret", "");
        System.setProperty("spotify.redirect.uri", "");

        SpotifyConfiguration emptyConfig = new SpotifyConfiguration();

        // 验证空字符串值
        Assertions.assertEquals("", emptyConfig.getClientId());
        Assertions.assertEquals("", emptyConfig.getClientSecret());
        Assertions.assertEquals("", emptyConfig.getRedirectUri());
    }

    @Test
    public void testNullClientCredentials() {
        // 测试客户端凭据为null的情况
        System.clearProperty("spotify.client.id");
        System.clearProperty("spotify.client.secret");
        
        SpotifyConfiguration nullCredentialsConfig = new SpotifyConfiguration();
        
        // 客户端凭据应该为null
        Assertions.assertNull(nullCredentialsConfig.getClientId());
        Assertions.assertNull(nullCredentialsConfig.getClientSecret());
        
        // 其他配置应该正常
        Assertions.assertEquals("https://api.spotify.com/v1", nullCredentialsConfig.getApiBaseUrl());
        Assertions.assertEquals(30, nullCredentialsConfig.getTimeout());
    }

    @Test
    public void testConfigurationGettersAndSetters() {
        // 测试getter和setter方法
        spotifyConfiguration.setClientId("test-key");
        spotifyConfiguration.setClientSecret("test-secret");
        spotifyConfiguration.setTimeout(120);
        spotifyConfiguration.setDefaultLimit(40);
        spotifyConfiguration.setMaxLimit(80);

        Assertions.assertEquals("test-key", spotifyConfiguration.getClientId());
        Assertions.assertEquals("test-secret", spotifyConfiguration.getClientSecret());
        Assertions.assertEquals(120, spotifyConfiguration.getTimeout());
        Assertions.assertEquals(40, spotifyConfiguration.getDefaultLimit());
        Assertions.assertEquals(80, spotifyConfiguration.getMaxLimit());
    }

    @Test
    public void testStaticConstants() {
        // 测试静态常量
        Assertions.assertNotNull(SpotifyConfiguration.SPOTIFY_REDIRECT_URI);
        Assertions.assertNotNull(SpotifyConfiguration.SPOTIFY_API_BASE_URL);
        Assertions.assertNotNull(SpotifyConfiguration.SPOTIFY_AUTH_URL);
        Assertions.assertTrue(SpotifyConfiguration.SPOTIFY_TIMEOUT > 0);
        Assertions.assertTrue(SpotifyConfiguration.DEFAULT_LIMIT > 0);
        Assertions.assertTrue(SpotifyConfiguration.MAX_LIMIT > 0);
    }

    @Test
    public void testConfigurationConsistency() {
        // 测试配置一致性
        SpotifyConfiguration config1 = new SpotifyConfiguration();
        SpotifyConfiguration config2 = new SpotifyConfiguration();

        // 两个实例的默认值应该相同
        Assertions.assertEquals(config1.getRedirectUri(), config2.getRedirectUri());
        Assertions.assertEquals(config1.getApiBaseUrl(), config2.getApiBaseUrl());
        Assertions.assertEquals(config1.getAuthUrl(), config2.getAuthUrl());
        Assertions.assertEquals(config1.getTimeout(), config2.getTimeout());
        Assertions.assertEquals(config1.getDefaultLimit(), config2.getDefaultLimit());
        Assertions.assertEquals(config1.getMaxLimit(), config2.getMaxLimit());
    }

    @Test
    public void testLimitValidation() {
        // 测试限制值的合理性
        SpotifyConfiguration config = new SpotifyConfiguration();
        
        // 默认限制应该小于等于最大限制
        Assertions.assertTrue(config.getDefaultLimit() <= config.getMaxLimit());
        
        // 设置自定义值并验证
        config.setDefaultLimit(30);
        config.setMaxLimit(100);
        Assertions.assertTrue(config.getDefaultLimit() <= config.getMaxLimit());
    }

    @Test
    public void testUrlFormatValidation() {
        // 测试URL格式的合理性
        SpotifyConfiguration config = new SpotifyConfiguration();
        
        // API基础URL应该以https开头
        Assertions.assertTrue(config.getApiBaseUrl().startsWith("https://"));
        
        // 认证URL应该以https开头
        Assertions.assertTrue(config.getAuthUrl().startsWith("https://"));
        
        // 重定向URI应该是有效的URL格式
        Assertions.assertTrue(config.getRedirectUri().startsWith("http"));
    }
}
