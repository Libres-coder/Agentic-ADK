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
package com.alibaba.langengine.scann.vectorstore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;


public class ScannParamDebugTest {

    @Test
    @DisplayName("调试构造函数问题")
    public void testConstructorDebug() {
        // 测试默认构造函数
        ScannParam defaultParam = new ScannParam();
        System.out.println("Default constructor - serverUrl: " + defaultParam.getServerUrl());
        System.out.println("Default constructor - dimensions: " + defaultParam.getDimensions());
        
        // 测试带参数的构造函数
        ScannParam customParam = new ScannParam("http://test:8080", 512);
        System.out.println("Custom constructor - serverUrl: " + customParam.getServerUrl());
        System.out.println("Custom constructor - dimensions: " + customParam.getDimensions());
        
        // 验证值
        assertEquals("http://test:8080", customParam.getServerUrl());
        assertEquals(512, customParam.getDimensions());
    }
}
