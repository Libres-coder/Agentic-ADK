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
package com.alibaba.langengine.baidu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 百度配置类测试
 *
 * @author aihe.ah
 */
public class BaiduConfigurationTest {

    @Test
    void testBaiduUserAgent() {
        // 测试默认用户代理是否不为空
        assertNotNull(BaiduConfiguration.BAIDU_USER_AGENT);
        assertFalse(BaiduConfiguration.BAIDU_USER_AGENT.trim().isEmpty());
        
        // 测试用户代理是否包含常见的浏览器标识
        String userAgent = BaiduConfiguration.BAIDU_USER_AGENT;
        assertTrue(userAgent.contains("Mozilla") || userAgent.contains("Chrome") || userAgent.contains("Safari"));
    }

    @Test
    void testUserAgentFormat() {
        String userAgent = BaiduConfiguration.BAIDU_USER_AGENT;
        
        // 测试用户代理格式是否合理
        assertTrue(userAgent.length() > 50, "用户代理应该足够长");
        assertTrue(userAgent.contains(" "), "用户代理应该包含空格分隔符");
    }
} 