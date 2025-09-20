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
package com.alibaba.langengine.singlestore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SingleStore配置测试")
class SingleStoreConfigurationTest {

    @Test
    @DisplayName("测试配置类实例化")
    void testConfigurationInstantiation() {
        // 测试配置类可以被实例化
        assertDoesNotThrow(() -> {
            new SingleStoreConfiguration();
        });
    }

    @Test
    @DisplayName("测试配置常量不为null（当设置了系统属性时）")
    void testConfigurationConstants() throws Exception {
        // 测试配置常量存在且可访问
        assertNotNull(SingleStoreConfiguration.class.getField("SINGLESTORE_SERVER_URL"));
        assertNotNull(SingleStoreConfiguration.class.getField("SINGLESTORE_DATABASE"));
        assertNotNull(SingleStoreConfiguration.class.getField("SINGLESTORE_USERNAME"));
        assertNotNull(SingleStoreConfiguration.class.getField("SINGLESTORE_PASSWORD"));
    }

    @Test
    @DisplayName("测试配置字段类型")
    void testConfigurationFieldTypes() throws Exception {
        // 验证字段类型
        assertEquals(String.class, SingleStoreConfiguration.class.getField("SINGLESTORE_SERVER_URL").getType());
        assertEquals(String.class, SingleStoreConfiguration.class.getField("SINGLESTORE_DATABASE").getType());
        assertEquals(String.class, SingleStoreConfiguration.class.getField("SINGLESTORE_USERNAME").getType());
        assertEquals(String.class, SingleStoreConfiguration.class.getField("SINGLESTORE_PASSWORD").getType());
    }

    @Test
    @DisplayName("测试配置字段为静态字段")
    void testConfigurationFieldsAreStatic() throws Exception {
        // 验证字段是静态的
        assertTrue(java.lang.reflect.Modifier.isStatic(
                SingleStoreConfiguration.class.getField("SINGLESTORE_SERVER_URL").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(
                SingleStoreConfiguration.class.getField("SINGLESTORE_DATABASE").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(
                SingleStoreConfiguration.class.getField("SINGLESTORE_USERNAME").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(
                SingleStoreConfiguration.class.getField("SINGLESTORE_PASSWORD").getModifiers()));
    }

    @Test
    @DisplayName("测试配置字段为公共字段")
    void testConfigurationFieldsArePublic() throws Exception {
        // 验证字段是公共的
        assertTrue(java.lang.reflect.Modifier.isPublic(
                SingleStoreConfiguration.class.getField("SINGLESTORE_SERVER_URL").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPublic(
                SingleStoreConfiguration.class.getField("SINGLESTORE_DATABASE").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPublic(
                SingleStoreConfiguration.class.getField("SINGLESTORE_USERNAME").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPublic(
                SingleStoreConfiguration.class.getField("SINGLESTORE_PASSWORD").getModifiers()));
    }
}