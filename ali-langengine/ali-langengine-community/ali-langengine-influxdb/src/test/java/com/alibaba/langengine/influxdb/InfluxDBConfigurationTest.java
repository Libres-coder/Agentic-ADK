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
package com.alibaba.langengine.influxdb;

import com.alibaba.langengine.influxdb.InfluxDBConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("InfluxDB配置测试")
class InfluxDBConfigurationTest {

    @Test
    @DisplayName("测试默认配置值")
    void testDefaultConfiguration() {
        InfluxDBConfiguration config = new InfluxDBConfiguration();

        assertEquals("http://localhost:8086", config.getUrl());
        assertEquals("default-org", config.getOrg());
        assertEquals("default-bucket", config.getBucket());
        assertEquals(1536, config.getDefaultVectorDimension());
        assertEquals(10, config.getDefaultTopK());
        assertEquals(0.7, config.getDefaultSimilarityThreshold());
        assertEquals(100, config.getDefaultBatchSize());
        assertEquals(60000, config.getConnectionTimeoutMs());
        assertEquals(60000, config.getReadTimeoutMs());
        assertEquals(60000, config.getWriteTimeoutMs());
        assertEquals(1000, config.getCacheSize());
        assertEquals(300000, config.getCacheTtlMs());
        assertTrue(config.isCacheEnabled());
        assertFalse(config.isDebugEnabled());
    }

    @Test
    @DisplayName("测试配置构建器")
    void testConfigurationBuilder() {
        InfluxDBConfiguration config = InfluxDBConfiguration.builder()
                .url("http://localhost:8086")
                .token("test-token")
                .org("test-org")
                .bucket("test-bucket")
                .defaultVectorDimension(768)
                .defaultTopK(20)
                .defaultSimilarityThreshold(0.8)
                .defaultBatchSize(50)
                .connectionTimeoutMs(30000)
                .readTimeoutMs(30000)
                .writeTimeoutMs(30000)
                .cacheSize(500)
                .cacheTtlMs(600000)
                .cacheEnabled(false)
                .debugEnabled(true)
                .build();

        assertEquals("http://localhost:8086", config.getUrl());
        assertEquals("test-token", config.getToken());
        assertEquals("test-org", config.getOrg());
        assertEquals("test-bucket", config.getBucket());
        assertEquals(768, config.getDefaultVectorDimension());
        assertEquals(20, config.getDefaultTopK());
        assertEquals(0.8, config.getDefaultSimilarityThreshold());
        assertEquals(50, config.getDefaultBatchSize());
        assertEquals(30000, config.getConnectionTimeoutMs());
        assertEquals(30000, config.getReadTimeoutMs());
        assertEquals(30000, config.getWriteTimeoutMs());
        assertEquals(500, config.getCacheSize());
        assertEquals(600000, config.getCacheTtlMs());
        assertFalse(config.isCacheEnabled());
        assertTrue(config.isDebugEnabled());
    }

    @Test
    @DisplayName("测试URL验证")
    void testUrlValidation() {
        InfluxDBConfiguration config = new InfluxDBConfiguration();

        // 测试有效的URL
        config.setUrl("http://localhost:8086");
        assertEquals("http://localhost:8086", config.getUrl());

        config.setUrl("https://my-influxdb.com:8086");
        assertEquals("https://my-influxdb.com:8086", config.getUrl());

        // 测试默认协议
        config.setUrl("localhost:8086");
        assertEquals("localhost:8086", config.getUrl());
    }

    @Test
    @DisplayName("测试参数验证")
    void testParameterValidation() {
        InfluxDBConfiguration config = new InfluxDBConfiguration();

        // 测试向量维度验证
        assertThrows(IllegalArgumentException.class, () -> 
                config.setDefaultVectorDimension(0));
        assertThrows(IllegalArgumentException.class, () -> 
                config.setDefaultVectorDimension(-1));

        // 测试TopK验证
        assertThrows(IllegalArgumentException.class, () -> 
                config.setDefaultTopK(0));
        assertThrows(IllegalArgumentException.class, () -> 
                config.setDefaultTopK(-1));

        // 测试相似度阈值验证
        assertThrows(IllegalArgumentException.class, () -> 
                config.setDefaultSimilarityThreshold(-0.1));
        assertThrows(IllegalArgumentException.class, () -> 
                config.setDefaultSimilarityThreshold(1.1));

        // 测试批次大小验证
        assertThrows(IllegalArgumentException.class, () -> 
                config.setDefaultBatchSize(0));
        assertThrows(IllegalArgumentException.class, () -> 
                config.setDefaultBatchSize(-1));

        // 测试超时时间验证
        assertThrows(IllegalArgumentException.class, () -> 
                config.setConnectionTimeoutMs(-1));
        assertThrows(IllegalArgumentException.class, () -> 
                config.setReadTimeoutMs(-1));
        assertThrows(IllegalArgumentException.class, () -> 
                config.setWriteTimeoutMs(-1));

        // 测试缓存配置验证
        assertThrows(IllegalArgumentException.class, () -> 
                config.setCacheSize(-1));
        assertThrows(IllegalArgumentException.class, () -> 
                config.setCacheTtlMs(-1));
    }

    @Test
    @DisplayName("测试环境变量支持")
    void testEnvironmentVariableSupport() {
        // 注意：这个测试需要模拟环境变量
        // 在实际使用中，可以通过System.setProperty来模拟
        System.setProperty("influxdb.url", "http://env-influxdb:8086");
        System.setProperty("influxdb.token", "env-token");
        System.setProperty("influxdb.org", "env-org");
        System.setProperty("influxdb.bucket", "env-bucket");

        InfluxDBConfiguration config = InfluxDBConfiguration.fromEnvironment();

        assertEquals("http://env-influxdb:8086", config.getUrl());
        assertEquals("env-token", config.getToken());
        assertEquals("env-org", config.getOrg());
        assertEquals("env-bucket", config.getBucket());

        // 清理环境变量
        System.clearProperty("influxdb.url");
        System.clearProperty("influxdb.token");
        System.clearProperty("influxdb.org");
        System.clearProperty("influxdb.bucket");
    }

    @Test
    @DisplayName("测试配置复制")
    void testConfigurationCopy() {
        InfluxDBConfiguration original = InfluxDBConfiguration.builder()
                .url("http://original:8086")
                .token("original-token")
                .org("original-org")
                .bucket("original-bucket")
                .defaultVectorDimension(768)
                .cacheEnabled(true)
                .build();

        InfluxDBConfiguration copy = original.copy();

        assertEquals(original.getUrl(), copy.getUrl());
        assertEquals(original.getToken(), copy.getToken());
        assertEquals(original.getOrg(), copy.getOrg());
        assertEquals(original.getBucket(), copy.getBucket());
        assertEquals(original.getDefaultVectorDimension(), copy.getDefaultVectorDimension());
        assertEquals(original.isCacheEnabled(), copy.isCacheEnabled());

        // 验证是深拷贝
        copy.setUrl("http://modified:8086");
        assertNotEquals(original.getUrl(), copy.getUrl());
    }

    @Test
    @DisplayName("测试配置equals和hashCode")
    void testConfigurationEqualsAndHashCode() {
        InfluxDBConfiguration config1 = InfluxDBConfiguration.builder()
                .url("http://test:8086")
                .token("test-token")
                .org("test-org")
                .bucket("test-bucket")
                .build();

        InfluxDBConfiguration config2 = InfluxDBConfiguration.builder()
                .url("http://test:8086")
                .token("test-token")
                .org("test-org")
                .bucket("test-bucket")
                .build();

        InfluxDBConfiguration config3 = InfluxDBConfiguration.builder()
                .url("http://different:8086")
                .token("test-token")
                .org("test-org")
                .bucket("test-bucket")
                .build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertNotEquals(config1, config3);
        assertNotEquals(config1.hashCode(), config3.hashCode());
    }

    @Test
    @DisplayName("测试配置toString")
    void testConfigurationToString() {
        InfluxDBConfiguration config = InfluxDBConfiguration.builder()
                .url("http://test:8086")
                .token("test-token")
                .org("test-org")
                .bucket("test-bucket")
                .build();

        String toString = config.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("url=http://test:8086"));
        assertTrue(toString.contains("org=test-org"));
        assertTrue(toString.contains("bucket=test-bucket"));
        // token不应该在toString中显示完整内容
        assertFalse(toString.contains("test-token"));
    }

    @Test
    @DisplayName("测试配置验证方法")
    void testConfigurationValidation() {
        InfluxDBConfiguration config = new InfluxDBConfiguration();

        // 测试无效配置
        config.setUrl("");
        config.setToken("");
        config.setOrg("");
        config.setBucket("");

        assertFalse(config.isValid());

        // 测试有效配置
        config.setUrl("http://localhost:8086");
        config.setToken("valid-token");
        config.setOrg("valid-org");
        config.setBucket("valid-bucket");

        assertTrue(config.isValid());
    }

    @Test
    @DisplayName("测试性能配置")
    void testPerformanceConfiguration() {
        InfluxDBConfiguration config = new InfluxDBConfiguration();

        // 测试高性能配置
        config.setDefaultBatchSize(1000);
        config.setCacheSize(10000);
        config.setCacheTtlMs(600000);
        config.setCacheEnabled(true);

        assertEquals(1000, config.getDefaultBatchSize());
        assertEquals(10000, config.getCacheSize());
        assertEquals(600000, config.getCacheTtlMs());
        assertTrue(config.isCacheEnabled());

        // 测试低延迟配置
        config.setConnectionTimeoutMs(5000);
        config.setReadTimeoutMs(5000);
        config.setWriteTimeoutMs(5000);
        config.setDefaultBatchSize(10);

        assertEquals(5000, config.getConnectionTimeoutMs());
        assertEquals(5000, config.getReadTimeoutMs());
        assertEquals(5000, config.getWriteTimeoutMs());
        assertEquals(10, config.getDefaultBatchSize());
    }

    @Test
    @DisplayName("测试配置继承")
    void testConfigurationInheritance() {
        InfluxDBConfiguration baseConfig = InfluxDBConfiguration.builder()
                .url("http://base:8086")
                .token("base-token")
                .org("base-org")
                .bucket("base-bucket")
                .defaultVectorDimension(768)
                .build();

        InfluxDBConfiguration derivedConfig = baseConfig.copy()
                .setBucket("derived-bucket")
                .setDefaultVectorDimension(1536);

        assertEquals("http://base:8086", derivedConfig.getUrl());
        assertEquals("base-token", derivedConfig.getToken());
        assertEquals("base-org", derivedConfig.getOrg());
        assertEquals("derived-bucket", derivedConfig.getBucket());
        assertEquals(1536, derivedConfig.getDefaultVectorDimension());
    }
}
