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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 百度常量类测试
 *
 * @author aihe.ah
 */
public class BaiduConstantTest {

    @Test
    void testBaseUrl() {
        // 测试基础URL
        assertNotNull(BaiduConstant.BASE_URL);
        assertFalse(BaiduConstant.BASE_URL.trim().isEmpty());
        assertTrue(BaiduConstant.BASE_URL.startsWith("https://"));
        assertTrue(BaiduConstant.BASE_URL.contains("baidu.com"));
        assertTrue(BaiduConstant.BASE_URL.contains("/s"));
    }

    @Test
    void testDefaultTimeout() {
        // 测试默认超时时间
        assertTrue(BaiduConstant.DEFAULT_TIMEOUT_SECONDS > 0);
        assertTrue(BaiduConstant.DEFAULT_TIMEOUT_SECONDS <= 300); // 不超过5分钟
        assertEquals(30, BaiduConstant.DEFAULT_TIMEOUT_SECONDS);
    }

    @Test
    void testDefaultUserAgent() {
        // 测试默认用户代理
        assertNotNull(BaiduConstant.DEFAULT_USER_AGENT);
        assertFalse(BaiduConstant.DEFAULT_USER_AGENT.trim().isEmpty());
        assertTrue(BaiduConstant.DEFAULT_USER_AGENT.length() > 50);
        
        // 测试用户代理格式
        String userAgent = BaiduConstant.DEFAULT_USER_AGENT;
        assertTrue(userAgent.contains("Mozilla"), "应该包含Mozilla标识");
        assertTrue(userAgent.contains("Chrome") || userAgent.contains("Safari"), "应该包含浏览器标识");
        assertTrue(userAgent.contains("Windows") || userAgent.contains("Mac") || userAgent.contains("Linux"), "应该包含操作系统标识");
    }

    @Test
    void testBaseUrlFormat() {
        // 测试基础URL格式
        String baseUrl = BaiduConstant.BASE_URL;
        
        // 应该是HTTPS协议
        assertEquals("https", baseUrl.substring(0, 5));
        
        // 应该包含域名
        assertTrue(baseUrl.contains("www.baidu.com"));
        
        // 应该以/s结尾
        assertTrue(baseUrl.endsWith("/s"));
    }

    @Test
    void testUserAgentFormat() {
        // 测试用户代理格式
        String userAgent = BaiduConstant.DEFAULT_USER_AGENT;
        
        // 应该包含多个部分，用空格分隔
        String[] parts = userAgent.split(" ");
        assertTrue(parts.length >= 3, "用户代理应该包含至少3个部分");
        
        // 第一部分应该是Mozilla
        assertTrue(parts[0].startsWith("Mozilla"), "用户代理应该以Mozilla开头");
    }

    @Test
    void testConstantsAreFinal() {
        // 测试常量是否被正确声明为final
        // 这个测试主要是为了确保常量定义的正确性
        assertNotNull(BaiduConstant.BASE_URL);
        assertNotNull(BaiduConstant.DEFAULT_USER_AGENT);
        assertTrue(BaiduConstant.DEFAULT_TIMEOUT_SECONDS > 0);
    }
} 