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
package com.alibaba.langengine.deeplake.vectorstore;

import com.alibaba.langengine.deeplake.DeepLakeConfiguration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class DeepLakeConfigurationTest {

    @Test
    public void testConfigurationFieldsExist() {
        // 测试配置字段存在（可能为空，因为依赖外部配置文件）
        // 这些字段应该被初始化，即使值可能为空
        // 由于 WorkPropertiesUtils.get() 可能返回 null 或空字符串，我们只测试字段存在性
        
        // 通过反射验证字段存在
        try {
            java.lang.reflect.Field serverUrlField = DeepLakeConfiguration.class.getDeclaredField("DEEPLAKE_SERVER_URL");
            assertNotNull(serverUrlField);
            
            java.lang.reflect.Field apiTokenField = DeepLakeConfiguration.class.getDeclaredField("DEEPLAKE_API_TOKEN");
            assertNotNull(apiTokenField);
            
            java.lang.reflect.Field organizationField = DeepLakeConfiguration.class.getDeclaredField("DEEPLAKE_ORGANIZATION");
            assertNotNull(organizationField);
            
        } catch (NoSuchFieldException e) {
            fail("Configuration fields should exist: " + e.getMessage());
        }
    }

    @Test
    public void testConfigurationFieldModifiers() {
        // 确保配置字段是 static 和 public 的（通过反射检查）
        try {
            java.lang.reflect.Field serverUrlField = DeepLakeConfiguration.class.getDeclaredField("DEEPLAKE_SERVER_URL");
            assertTrue(java.lang.reflect.Modifier.isStatic(serverUrlField.getModifiers()));
            assertTrue(java.lang.reflect.Modifier.isPublic(serverUrlField.getModifiers()));
            
            java.lang.reflect.Field apiTokenField = DeepLakeConfiguration.class.getDeclaredField("DEEPLAKE_API_TOKEN");
            assertTrue(java.lang.reflect.Modifier.isStatic(apiTokenField.getModifiers()));
            assertTrue(java.lang.reflect.Modifier.isPublic(apiTokenField.getModifiers()));
            
            java.lang.reflect.Field organizationField = DeepLakeConfiguration.class.getDeclaredField("DEEPLAKE_ORGANIZATION");
            assertTrue(java.lang.reflect.Modifier.isStatic(organizationField.getModifiers()));
            assertTrue(java.lang.reflect.Modifier.isPublic(organizationField.getModifiers()));
            
        } catch (NoSuchFieldException e) {
            fail("Configuration fields should exist: " + e.getMessage());
        }
    }

    @Test
    public void testConfigurationClass() {
        // 测试配置类是否可以实例化（虽然通常不需要）
        DeepLakeConfiguration config = new DeepLakeConfiguration();
        assertNotNull(config);
    }
}
