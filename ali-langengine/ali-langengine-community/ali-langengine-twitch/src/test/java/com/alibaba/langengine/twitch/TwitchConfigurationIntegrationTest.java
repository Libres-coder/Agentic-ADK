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
package com.alibaba.langengine.twitch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Properties;

/**
 * TwitchConfiguration集成测试类
 * 测试配置类的系统属性读取、默认值设置和构造函数初始化
 * 
 * @author aihe.ah
 * @time 2024/12/19
 */
public class TwitchConfigurationIntegrationTest {

    private Properties originalProperties;
    private TwitchConfiguration twitchConfiguration;

    @BeforeEach
    public void setUp() {
        // 保存原始系统属性
        originalProperties = new Properties();
        originalProperties.putAll(System.getProperties());
        
        // 创建配置实例
        twitchConfiguration = new TwitchConfiguration();
    }

    @AfterEach
    public void tearDown() {
        // 恢复原始系统属性
        System.setProperties(originalProperties);
    }

    @Test
    public void testDefaultConfigurationValues() {
        // 测试默认配置值
        Assertions.assertEquals("https://api.twitch.tv/helix", twitchConfiguration.getApiBaseUrl());
        Assertions.assertEquals("https://id.twitch.tv/oauth2/token", twitchConfiguration.getAuthUrl());
        Assertions.assertEquals(30, twitchConfiguration.getTimeout());
        Assertions.assertEquals(20, twitchConfiguration.getDefaultLimit());
        Assertions.assertEquals(100, twitchConfiguration.getMaxLimit());
        Assertions.assertEquals("zh", twitchConfiguration.getDefaultLanguage());
        Assertions.assertEquals("Asia/Shanghai", twitchConfiguration.getDefaultTimezone());
    }

    @Test
    public void testCustomConfigurationValues() {
        // 设置自定义系统属性
        System.setProperty("twitch.client.id", "test-client-id");
        System.setProperty("twitch.client.secret", "test-client-secret");
        System.setProperty("twitch.api.base.url", "https://custom.api.com/helix");
        System.setProperty("twitch.auth.url", "https://custom.auth.com/token");
        System.setProperty("twitch.timeout", "60");
        System.setProperty("twitch.default.limit", "30");
        System.setProperty("twitch.max.limit", "150");
        System.setProperty("twitch.default.language", "en");
        System.setProperty("twitch.default.timezone", "America/New_York");

        // 创建新的配置实例
        TwitchConfiguration customConfig = new TwitchConfiguration();

        // 验证自定义值
        Assertions.assertEquals("test-client-id", customConfig.getClientId());
        Assertions.assertEquals("test-client-secret", customConfig.getClientSecret());
        Assertions.assertEquals("https://custom.api.com/helix", customConfig.getApiBaseUrl());
        Assertions.assertEquals("https://custom.auth.com/token", customConfig.getAuthUrl());
        Assertions.assertEquals(60, customConfig.getTimeout());
        Assertions.assertEquals(30, customConfig.getDefaultLimit());
        Assertions.assertEquals(150, customConfig.getMaxLimit());
        Assertions.assertEquals("en", customConfig.getDefaultLanguage());
        Assertions.assertEquals("America/New_York", customConfig.getDefaultTimezone());
    }

    @Test
    public void testPartialCustomConfiguration() {
        // 只设置部分自定义属性
        System.setProperty("twitch.timeout", "45");
        System.setProperty("twitch.default.language", "ja");
        System.setProperty("twitch.default.timezone", "Asia/Tokyo");

        TwitchConfiguration partialConfig = new TwitchConfiguration();

        // 验证自定义值
        Assertions.assertEquals(45, partialConfig.getTimeout());
        Assertions.assertEquals("ja", partialConfig.getDefaultLanguage());
        Assertions.assertEquals("Asia/Tokyo", partialConfig.getDefaultTimezone());

        // 验证其他值保持默认
        Assertions.assertEquals("https://api.twitch.tv/helix", partialConfig.getApiBaseUrl());
        Assertions.assertEquals(20, partialConfig.getDefaultLimit());
        Assertions.assertEquals(100, partialConfig.getMaxLimit());
    }

    @Test
    public void testInvalidNumericProperties() {
        // 测试无效的数字属性值
        System.setProperty("twitch.timeout", "invalid");
        System.setProperty("twitch.default.limit", "not-a-number");
        System.setProperty("twitch.max.limit", "abc");

        // 应该抛出NumberFormatException
        Assertions.assertThrows(NumberFormatException.class, () -> {
            new TwitchConfiguration();
        });
    }

    @Test
    public void testEmptyStringProperties() {
        // 测试空字符串属性
        System.setProperty("twitch.client.id", "");
        System.setProperty("twitch.client.secret", "");
        System.setProperty("twitch.default.language", "");
        System.setProperty("twitch.default.timezone", "");

        TwitchConfiguration emptyConfig = new TwitchConfiguration();

        // 验证空字符串值
        Assertions.assertEquals("", emptyConfig.getClientId());
        Assertions.assertEquals("", emptyConfig.getClientSecret());
        Assertions.assertEquals("", emptyConfig.getDefaultLanguage());
        Assertions.assertEquals("", emptyConfig.getDefaultTimezone());
    }

    @Test
    public void testNullClientCredentials() {
        // 测试客户端凭据为null的情况
        System.clearProperty("twitch.client.id");
        System.clearProperty("twitch.client.secret");
        
        TwitchConfiguration nullCredentialsConfig = new TwitchConfiguration();
        
        // 客户端凭据应该为null
        Assertions.assertNull(nullCredentialsConfig.getClientId());
        Assertions.assertNull(nullCredentialsConfig.getClientSecret());
        
        // 其他配置应该正常
        Assertions.assertEquals("https://api.twitch.tv/helix", nullCredentialsConfig.getApiBaseUrl());
        Assertions.assertEquals(30, nullCredentialsConfig.getTimeout());
        Assertions.assertEquals("zh", nullCredentialsConfig.getDefaultLanguage());
    }

    @Test
    public void testConfigurationGettersAndSetters() {
        // 测试getter和setter方法
        twitchConfiguration.setClientId("test-key");
        twitchConfiguration.setClientSecret("test-secret");
        twitchConfiguration.setTimeout(120);
        twitchConfiguration.setDefaultLimit(40);
        twitchConfiguration.setMaxLimit(80);
        twitchConfiguration.setDefaultLanguage("fr");
        twitchConfiguration.setDefaultTimezone("Europe/Paris");

        Assertions.assertEquals("test-key", twitchConfiguration.getClientId());
        Assertions.assertEquals("test-secret", twitchConfiguration.getClientSecret());
        Assertions.assertEquals(120, twitchConfiguration.getTimeout());
        Assertions.assertEquals(40, twitchConfiguration.getDefaultLimit());
        Assertions.assertEquals(80, twitchConfiguration.getMaxLimit());
        Assertions.assertEquals("fr", twitchConfiguration.getDefaultLanguage());
        Assertions.assertEquals("Europe/Paris", twitchConfiguration.getDefaultTimezone());
    }

    @Test
    public void testStaticConstants() {
        // 测试静态常量
        Assertions.assertNotNull(TwitchConfiguration.TWITCH_API_BASE_URL);
        Assertions.assertNotNull(TwitchConfiguration.TWITCH_AUTH_URL);
        Assertions.assertTrue(TwitchConfiguration.TWITCH_TIMEOUT > 0);
        Assertions.assertTrue(TwitchConfiguration.DEFAULT_LIMIT > 0);
        Assertions.assertTrue(TwitchConfiguration.MAX_LIMIT > 0);
        Assertions.assertNotNull(TwitchConfiguration.DEFAULT_LANGUAGE);
        Assertions.assertNotNull(TwitchConfiguration.DEFAULT_TIMEZONE);
    }

    @Test
    public void testConfigurationConsistency() {
        // 测试配置一致性
        TwitchConfiguration config1 = new TwitchConfiguration();
        TwitchConfiguration config2 = new TwitchConfiguration();

        // 两个实例的默认值应该相同
        Assertions.assertEquals(config1.getApiBaseUrl(), config2.getApiBaseUrl());
        Assertions.assertEquals(config1.getAuthUrl(), config2.getAuthUrl());
        Assertions.assertEquals(config1.getTimeout(), config2.getTimeout());
        Assertions.assertEquals(config1.getDefaultLimit(), config2.getDefaultLimit());
        Assertions.assertEquals(config1.getMaxLimit(), config2.getMaxLimit());
        Assertions.assertEquals(config1.getDefaultLanguage(), config2.getDefaultLanguage());
        Assertions.assertEquals(config1.getDefaultTimezone(), config2.getDefaultTimezone());
    }

    @Test
    public void testLimitValidation() {
        // 测试限制值的合理性
        TwitchConfiguration config = new TwitchConfiguration();
        
        // 默认限制应该小于等于最大限制
        Assertions.assertTrue(config.getDefaultLimit() <= config.getMaxLimit());
        
        // 设置自定义值并验证
        config.setDefaultLimit(50);
        config.setMaxLimit(200);
        Assertions.assertTrue(config.getDefaultLimit() <= config.getMaxLimit());
    }

    @Test
    public void testUrlFormatValidation() {
        // 测试URL格式的合理性
        TwitchConfiguration config = new TwitchConfiguration();
        
        // API基础URL应该以https开头
        Assertions.assertTrue(config.getApiBaseUrl().startsWith("https://"));
        
        // 认证URL应该以https开头
        Assertions.assertTrue(config.getAuthUrl().startsWith("https://"));
        
        // API基础URL应该包含helix路径
        Assertions.assertTrue(config.getApiBaseUrl().contains("/helix"));
    }

    @Test
    public void testLanguageCodeValidation() {
        // 测试语言代码的合理性
        TwitchConfiguration config = new TwitchConfiguration();
        
        // 默认语言代码应该是有效的
        Assertions.assertTrue(config.getDefaultLanguage().length() >= 2);
        
        // 设置不同的语言代码
        config.setDefaultLanguage("en");
        Assertions.assertEquals("en", config.getDefaultLanguage());
        
        config.setDefaultLanguage("zh");
        Assertions.assertEquals("zh", config.getDefaultLanguage());
        
        config.setDefaultLanguage("ja");
        Assertions.assertEquals("ja", config.getDefaultLanguage());
    }

    @Test
    public void testTimezoneValidation() {
        // 测试时区的合理性
        TwitchConfiguration config = new TwitchConfiguration();
        
        // 默认时区应该是有效的
        Assertions.assertTrue(config.getDefaultTimezone().contains("/"));
        
        // 设置不同的时区
        config.setDefaultTimezone("America/New_York");
        Assertions.assertEquals("America/New_York", config.getDefaultTimezone());
        
        config.setDefaultTimezone("Europe/London");
        Assertions.assertEquals("Europe/London", config.getDefaultTimezone());
        
        config.setDefaultTimezone("Asia/Tokyo");
        Assertions.assertEquals("Asia/Tokyo", config.getDefaultTimezone());
    }

    @Test
    public void testNegativeTimeoutHandling() {
        // 测试负超时值的处理
        System.setProperty("twitch.timeout", "-1");
        
        // 应该抛出NumberFormatException或处理为无效值
        Assertions.assertThrows(NumberFormatException.class, () -> {
            new TwitchConfiguration();
        });
    }

    @Test
    public void testZeroLimitHandling() {
        // 测试零限制值的处理
        System.setProperty("twitch.default.limit", "0");
        System.setProperty("twitch.max.limit", "0");
        
        TwitchConfiguration zeroLimitConfig = new TwitchConfiguration();
        
        // 零值应该被接受
        Assertions.assertEquals(0, zeroLimitConfig.getDefaultLimit());
        Assertions.assertEquals(0, zeroLimitConfig.getMaxLimit());
    }
}
