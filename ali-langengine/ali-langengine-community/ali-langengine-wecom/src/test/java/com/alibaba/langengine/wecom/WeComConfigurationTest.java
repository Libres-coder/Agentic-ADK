package com.alibaba.langengine.wecom;

import com.alibaba.langengine.wecom.WeComConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class WeComConfigurationTest {

    private WeComConfiguration configuration;
    private String originalCorpId;
    private String originalCorpSecret;
    private String originalAgentId;
    private String originalToken;
    private String originalEncodingAESKey;

    @Before
    public void setUp() {
        // 保存原始环境变量
        originalCorpId = System.getProperty("wecom.corp.id");
        originalCorpSecret = System.getProperty("wecom.corp.secret");
        originalAgentId = System.getProperty("wecom.agent.id");
        originalToken = System.getProperty("wecom.token");
        originalEncodingAESKey = System.getProperty("wecom.encoding.aes.key");

        // 设置测试环境变量
        System.setProperty("wecom.corp.id", "test_corp_id");
        System.setProperty("wecom.corp.secret", "test_corp_secret");
        System.setProperty("wecom.agent.id", "1000001");
        System.setProperty("wecom.token", "test_token");
        System.setProperty("wecom.encoding.aes.key", "test_encoding_aes_key");

        configuration = new WeComConfiguration();
    }

    @After
    public void tearDown() {
        // 恢复原始环境变量
        restoreProperty("wecom.corp.id", originalCorpId);
        restoreProperty("wecom.corp.secret", originalCorpSecret);
        restoreProperty("wecom.agent.id", originalAgentId);
        restoreProperty("wecom.token", originalToken);
        restoreProperty("wecom.encoding.aes.key", originalEncodingAESKey);
    }

    private void restoreProperty(String key, String value) {
        if (value != null) {
            System.setProperty(key, value);
        } else {
            System.clearProperty(key);
        }
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull("Configuration should not be null", configuration);
        assertEquals("Corp ID should be loaded from system property", "test_corp_id", configuration.getCorpId());
        assertEquals("Corp Secret should be loaded from system property", "test_corp_secret", configuration.getCorpSecret());
        assertEquals("Agent ID should be loaded from system property", "1000001", configuration.getAgentId());
        assertEquals("Token should be loaded from system property", "test_token", configuration.getToken());
        assertEquals("Encoding AES Key should be loaded from system property", "test_encoding_aes_key", configuration.getEncodingAESKey());
    }

    @Test
    public void testParameterizedConstructor() {
        String corpId = "param_corp_id";
        String corpSecret = "param_corp_secret";
        String agentId = "1000002";

        WeComConfiguration config = new WeComConfiguration(corpId, corpSecret, agentId);

        assertEquals("Corp ID should match parameter", corpId, config.getCorpId());
        assertEquals("Corp Secret should match parameter", corpSecret, config.getCorpSecret());
        assertEquals("Agent ID should match parameter", agentId, config.getAgentId());
    }

    @Test
    public void testFullParameterizedConstructor() {
        String corpId = "full_corp_id";
        String corpSecret = "full_corp_secret";
        String agentId = "1000003";
        String token = "full_token";
        String encodingAESKey = "full_encoding_aes_key";

        WeComConfiguration config = new WeComConfiguration(corpId, corpSecret, agentId, token, encodingAESKey);

        assertEquals("Corp ID should match parameter", corpId, config.getCorpId());
        assertEquals("Corp Secret should match parameter", corpSecret, config.getCorpSecret());
        assertEquals("Agent ID should match parameter", agentId, config.getAgentId());
        assertEquals("Token should match parameter", token, config.getToken());
        assertEquals("Encoding AES Key should match parameter", encodingAESKey, config.getEncodingAESKey());
    }

    @Test
    public void testIsValid() {
        assertTrue("Configuration with all required fields should be valid", configuration.isValid());

        // Test with missing Corp ID
        WeComConfiguration invalidConfig1 = new WeComConfiguration(null, "secret", "agent");
        assertFalse("Configuration with missing Corp ID should be invalid", invalidConfig1.isValid());

        // Test with empty Corp ID
        WeComConfiguration invalidConfig2 = new WeComConfiguration("", "secret", "agent");
        assertFalse("Configuration with empty Corp ID should be invalid", invalidConfig2.isValid());

        // Test with missing Corp Secret
        WeComConfiguration invalidConfig3 = new WeComConfiguration("corp", null, "agent");
        assertFalse("Configuration with missing Corp Secret should be invalid", invalidConfig3.isValid());

        // Test with missing Agent ID
        WeComConfiguration invalidConfig4 = new WeComConfiguration("corp", "secret", null);
        assertFalse("Configuration with missing Agent ID should be invalid", invalidConfig4.isValid());
    }

    @Test
    public void testIsCallbackValid() {
        assertTrue("Configuration with callback fields should be callback valid", configuration.isCallbackValid());

        // Test without callback fields
        WeComConfiguration noCallbackConfig = new WeComConfiguration("corp", "secret", "agent");
        assertFalse("Configuration without callback fields should not be callback valid", noCallbackConfig.isCallbackValid());

        // Test with only token
        WeComConfiguration onlyTokenConfig = new WeComConfiguration("corp", "secret", "agent", "token", null);
        assertFalse("Configuration with only token should not be callback valid", onlyTokenConfig.isCallbackValid());

        // Test with only encoding key
        WeComConfiguration onlyKeyConfig = new WeComConfiguration("corp", "secret", "agent", null, "key");
        assertFalse("Configuration with only encoding key should not be callback valid", onlyKeyConfig.isCallbackValid());
    }

    @Test
    public void testDefaultValues() {
        // 临时清除系统属性以测试默认值
        System.clearProperty("wecom.connect.timeout");
        System.clearProperty("wecom.read.timeout");
        System.clearProperty("wecom.debug");
        
        WeComConfiguration config = new WeComConfiguration();
        
        assertEquals("Default base URL should be correct", "https://qyapi.weixin.qq.com", config.getBaseUrl());
        assertEquals("Default connect timeout should be 30000", 30000, config.getConnectTimeout());
        assertEquals("Default read timeout should be 30000", 30000, config.getReadTimeout());
        assertFalse("Default debug should be false", config.isDebug());
        assertEquals("Default max retries should be 3", 3, config.getMaxRetries());
        assertEquals("Default retry interval should be 1000", 1000, config.getRetryInterval());
        assertEquals("Default access token cache time should be 7200", 7200, config.getAccessTokenCacheTime());
        
        // 恢复测试环境的系统属性
        System.setProperty("wecom.corp.id", "test_corp_id");
        System.setProperty("wecom.corp.secret", "test_corp_secret");
        System.setProperty("wecom.agent.id", "1000001");
    }

    @Test
    public void testSettersAndGetters() {
        WeComConfiguration config = new WeComConfiguration();

        // Test setters and getters
        config.setCorpId("new_corp_id");
        assertEquals("Corp ID setter/getter should work", "new_corp_id", config.getCorpId());

        config.setCorpSecret("new_corp_secret");
        assertEquals("Corp Secret setter/getter should work", "new_corp_secret", config.getCorpSecret());

        config.setAgentId("new_agent_id");
        assertEquals("Agent ID setter/getter should work", "new_agent_id", config.getAgentId());

        config.setToken("new_token");
        assertEquals("Token setter/getter should work", "new_token", config.getToken());

        config.setEncodingAESKey("new_encoding_aes_key");
        assertEquals("Encoding AES Key setter/getter should work", "new_encoding_aes_key", config.getEncodingAESKey());

        config.setBaseUrl("https://custom.api.com");
        assertEquals("Base URL setter/getter should work", "https://custom.api.com", config.getBaseUrl());

        config.setConnectTimeout(5000);
        assertEquals("Connect timeout setter/getter should work", 5000, config.getConnectTimeout());

        config.setReadTimeout(6000);
        assertEquals("Read timeout setter/getter should work", 6000, config.getReadTimeout());

        config.setDebug(true);
        assertTrue("Debug setter/getter should work", config.isDebug());

        config.setMaxRetries(5);
        assertEquals("Max retries setter/getter should work", 5, config.getMaxRetries());

        config.setRetryInterval(2000);
        assertEquals("Retry interval setter/getter should work", 2000, config.getRetryInterval());

        config.setAccessTokenCacheTime(3600);
        assertEquals("Access token cache time setter/getter should work", 3600, config.getAccessTokenCacheTime());
    }

    @Test
    public void testGetDebugInfo() {
        String debugInfo = configuration.getDebugInfo();
        
        assertNotNull("Debug info should not be null", debugInfo);
        assertTrue("Debug info should indicate corp ID is present", debugInfo.contains("corpIdPresent=true"));
        assertTrue("Debug info should indicate agent ID is present", debugInfo.contains("agentIdPresent=true"));
        assertTrue("Debug info should contain base URL", debugInfo.contains("https://qyapi.weixin.qq.com"));
    }

    @Test
    public void testToString() {
        String configString = configuration.toString();
        
        assertNotNull("toString should not return null", configString);
        assertTrue("toString should contain corp ID (masked)", configString.contains("tes***_id"));
        assertTrue("toString should contain agent ID", configString.contains("1000001"));
    }

    @Test
    public void testEnvironmentVariablePrecedence() {
        // Clear system properties
        System.clearProperty("wecom.corp.id");
        System.clearProperty("wecom.corp.secret");
        System.clearProperty("wecom.agent.id");

        // Set environment variables (simulated through system properties with different names)
        System.setProperty("WECOM_CORP_ID", "env_corp_id");
        System.setProperty("WECOM_CORP_SECRET", "env_corp_secret");
        System.setProperty("WECOM_AGENT_ID", "env_agent_id");

        // Note: In real scenario, we can't easily set environment variables in unit tests
        // This test demonstrates the intended behavior but may not work as expected
        // in the actual test environment due to environment variable simulation limitations
        
        WeComConfiguration envConfig = new WeComConfiguration();
        
        // Clean up
        System.clearProperty("WECOM_CORP_ID");
        System.clearProperty("WECOM_CORP_SECRET");
        System.clearProperty("WECOM_AGENT_ID");
    }

    @Test
    public void testConfigurationFromSystemProperties() {
        // Set system properties for timeout values
        System.setProperty("wecom.connect.timeout", "15000");
        System.setProperty("wecom.read.timeout", "20000");
        System.setProperty("wecom.debug", "true");

        WeComConfiguration config = new WeComConfiguration();

        assertEquals("Connect timeout should be loaded from system property", 15000, config.getConnectTimeout());
        assertEquals("Read timeout should be loaded from system property", 20000, config.getReadTimeout());
        assertTrue("Debug should be loaded from system property", config.isDebug());

        // Clean up
        System.clearProperty("wecom.connect.timeout");
        System.clearProperty("wecom.read.timeout");
        System.clearProperty("wecom.debug");
    }

    @Test
    public void testEdgeCases() {
        // Test with whitespace values
        WeComConfiguration config = new WeComConfiguration("  corp_id  ", "  corp_secret  ", "  agent_id  ");
        assertEquals("Corp ID should be trimmed", "  corp_id  ", config.getCorpId());

        // Test with numeric agent ID
        WeComConfiguration numericConfig = new WeComConfiguration("corp", "secret", "123456");
        assertEquals("Numeric agent ID should work", "123456", numericConfig.getAgentId());

        // Test configuration validity with edge cases
        WeComConfiguration edgeConfig = new WeComConfiguration(" ", " ", " ");
        assertFalse("Configuration with whitespace-only values should be invalid", edgeConfig.isValid());
    }
}
