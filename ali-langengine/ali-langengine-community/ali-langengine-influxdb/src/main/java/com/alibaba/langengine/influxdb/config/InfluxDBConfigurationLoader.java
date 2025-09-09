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
package com.alibaba.langengine.influxdb.config;

import com.alibaba.langengine.influxdb.InfluxDBConfiguration;


public class InfluxDBConfigurationLoader {

    /**
     * 从系统属性和环境变量加载配置
     * 优先级：系统属性 > 环境变量 > 默认值
     */
    public static InfluxDBConfiguration fromEnvironment() {
        InfluxDBConfiguration config = new InfluxDBConfiguration();
        
        // 基础连接配置
        config.setUrl(getConfigValue("influxdb.url", "INFLUXDB_URL", "http://localhost:8086"));
        config.setToken(getConfigValue("influxdb.token", "INFLUXDB_TOKEN", null));
        config.setOrg(getConfigValue("influxdb.org", "INFLUXDB_ORG", "default-org"));
        config.setBucket(getConfigValue("influxdb.bucket", "INFLUXDB_BUCKET", "default-bucket"));
        
        // 向量相关配置
        config.setDefaultVectorDimension(getIntConfigValue("influxdb.vector.dimension", "INFLUXDB_VECTOR_DIMENSION", 1536));
        config.setDefaultTopK(getIntConfigValue("influxdb.default.topk", "INFLUXDB_DEFAULT_TOPK", 10));
        config.setDefaultSimilarityThreshold(getDoubleConfigValue("influxdb.similarity.threshold", "INFLUXDB_SIMILARITY_THRESHOLD", 0.7));
        config.setDefaultBatchSize(getIntConfigValue("influxdb.batch.size", "INFLUXDB_BATCH_SIZE", 100));
        
        // 连接超时配置
        config.setConnectionTimeoutMs(getIntConfigValue("influxdb.connection.timeout", "INFLUXDB_CONNECTION_TIMEOUT", 60000));
        config.setReadTimeoutMs(getIntConfigValue("influxdb.read.timeout", "INFLUXDB_READ_TIMEOUT", 60000));
        config.setWriteTimeoutMs(getIntConfigValue("influxdb.write.timeout", "INFLUXDB_WRITE_TIMEOUT", 60000));
        
        // 缓存配置
        config.setCacheSize(getIntConfigValue("influxdb.cache.size", "INFLUXDB_CACHE_SIZE", 1000));
        config.setCacheTtlMs(getIntConfigValue("influxdb.cache.ttl", "INFLUXDB_CACHE_TTL", 300000));
        config.setCacheEnabled(getBooleanConfigValue("influxdb.cache.enabled", "INFLUXDB_CACHE_ENABLED", true));
        config.setDebugEnabled(getBooleanConfigValue("influxdb.debug.enabled", "INFLUXDB_DEBUG_ENABLED", false));
        
        return config;
    }
    
    /**
     * 获取字符串配置值
     * 优先级：系统属性 > 环境变量 > 默认值
     */
    private static String getConfigValue(String systemPropertyKey, String envKey, String defaultValue) {
        String value = System.getProperty(systemPropertyKey);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        
        value = System.getenv(envKey);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        
        return defaultValue;
    }
    
    /**
     * 获取整数配置值
     */
    private static int getIntConfigValue(String systemPropertyKey, String envKey, int defaultValue) {
        String stringValue = getConfigValue(systemPropertyKey, envKey, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取双精度浮点数配置值
     */
    private static double getDoubleConfigValue(String systemPropertyKey, String envKey, double defaultValue) {
        String stringValue = getConfigValue(systemPropertyKey, envKey, String.valueOf(defaultValue));
        try {
            return Double.parseDouble(stringValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔配置值
     */
    private static boolean getBooleanConfigValue(String systemPropertyKey, String envKey, boolean defaultValue) {
        String stringValue = getConfigValue(systemPropertyKey, envKey, String.valueOf(defaultValue));
        return Boolean.parseBoolean(stringValue);
    }
}
