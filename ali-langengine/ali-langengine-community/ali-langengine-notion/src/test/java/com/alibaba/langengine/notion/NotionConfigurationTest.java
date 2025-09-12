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
package com.alibaba.langengine.notion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Notion配置测试")
class NotionConfigurationTest {
    
    private NotionConfiguration configuration;
    
    @BeforeEach
    void setUp() {
        configuration = new NotionConfiguration();
    }
    
    @Test
    @DisplayName("默认构造函数测试")
    void testDefaultConstructor() {
        assertNotNull(configuration);
        assertNull(configuration.getToken());
        assertEquals("2022-06-28", configuration.getVersion());
        assertEquals(30000, configuration.getTimeout());
        assertFalse(configuration.isDebug());
    }
    
    @Test
    @DisplayName("带token构造函数测试")
    void testTokenConstructor() {
        String token = "secret_test_token";
        NotionConfiguration config = new NotionConfiguration(token);
        
        assertEquals(token, config.getToken());
        assertEquals("2022-06-28", config.getVersion());
        assertEquals(30000, config.getTimeout());
        assertFalse(config.isDebug());
    }
    
    @Test
    @DisplayName("带token和版本构造函数测试")
    void testTokenAndVersionConstructor() {
        String token = "secret_test_token";
        String version = "2022-02-22";
        NotionConfiguration config = new NotionConfiguration(token, version);
        
        assertEquals(token, config.getToken());
        assertEquals(version, config.getVersion());
        assertEquals(30000, config.getTimeout());
        assertFalse(config.isDebug());
    }
    
    @Test
    @DisplayName("配置验证 - 空token")
    void testValidationEmptyToken() {
        configuration.setToken(null);
        assertFalse(configuration.isValid());
        
        configuration.setToken("");
        assertFalse(configuration.isValid());
        
        configuration.setToken("   ");
        assertFalse(configuration.isValid());
    }
    
    @Test
    @DisplayName("配置验证 - 有效token")
    void testValidationValidToken() {
        configuration.setToken("secret_valid_token");
        assertTrue(configuration.isValid());
    }
    
    @Test
    @DisplayName("配置验证 - 不规范token格式")
    void testValidationInvalidTokenFormat() {
        configuration.setToken("invalid_token_format");
        // 虽然格式不规范，但仍然有效（只是会有警告）
        assertTrue(configuration.isValid());
    }
    
    @Test
    @DisplayName("配置属性设置测试")
    void testConfigurationProperties() {
        String token = "secret_test_token";
        String version = "2022-01-01";
        int timeout = 60000;
        boolean debug = true;
        
        configuration.setToken(token);
        configuration.setVersion(version);
        configuration.setTimeout(timeout);
        configuration.setDebug(debug);
        
        assertEquals(token, configuration.getToken());
        assertEquals(version, configuration.getVersion());
        assertEquals(timeout, configuration.getTimeout());
        assertEquals(debug, configuration.isDebug());
        assertTrue(configuration.isValid());
    }
    
    @Test
    @DisplayName("边界值测试")
    void testBoundaryValues() {
        // 最小超时时间
        configuration.setTimeout(1);
        assertEquals(1, configuration.getTimeout());
        
        // 大超时时间
        configuration.setTimeout(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, configuration.getTimeout());
        
        // 空版本
        configuration.setVersion("");
        assertEquals("", configuration.getVersion());
        
        // null版本
        configuration.setVersion(null);
        assertNull(configuration.getVersion());
    }
}
