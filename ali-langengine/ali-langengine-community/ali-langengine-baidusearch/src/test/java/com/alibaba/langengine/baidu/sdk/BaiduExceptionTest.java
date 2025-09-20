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
 * 百度异常类测试
 *
 * @author aihe.ah
 */
public class BaiduExceptionTest {

    @Test
    void testBaiduExceptionWithMessage() {
        String message = "测试异常消息";
        BaiduException exception = new BaiduException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testBaiduExceptionWithMessageAndCause() {
        String message = "测试异常消息";
        Throwable cause = new RuntimeException("原始异常");
        BaiduException exception = new BaiduException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testBaiduExceptionWithNullMessage() {
        BaiduException exception = new BaiduException(null);
        
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testBaiduExceptionWithEmptyMessage() {
        String message = "";
        BaiduException exception = new BaiduException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testBaiduExceptionWithWhitespaceMessage() {
        String message = "   ";
        BaiduException exception = new BaiduException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testBaiduExceptionWithNullCause() {
        String message = "测试异常消息";
        BaiduException exception = new BaiduException(message, null);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testBaiduExceptionInheritance() {
        String message = "测试异常消息";
        BaiduException exception = new BaiduException(message);
        
        // 测试异常继承关系
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testBaiduExceptionStackTrace() {
        String message = "测试异常消息";
        BaiduException exception = new BaiduException(message);
        
        // 测试堆栈跟踪
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }
} 