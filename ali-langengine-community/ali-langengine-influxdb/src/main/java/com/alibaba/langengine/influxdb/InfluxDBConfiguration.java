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

import com.alibaba.langengine.influxdb.config.InfluxDBConfigurationLoader;


public class InfluxDBConfiguration {

    private String url;
    private String token;
    private String org;
    private String bucket;
    private int defaultVectorDimension;
    private int defaultTopK;
    private double defaultSimilarityThreshold;
    private int defaultBatchSize;
    private int connectionTimeoutMs;
    private int readTimeoutMs;
    private int writeTimeoutMs;
    private int cacheSize;
    private int cacheTtlMs;
    private boolean cacheEnabled;
    private boolean debugEnabled;

    /**
     * 默认构造函数
     * 创建一个空的配置对象，需要手动设置参数
     */
    public InfluxDBConfiguration() {
        // 设置默认值
        this.url = "http://localhost:8086";
        this.org = "default-org";
        this.bucket = "default-bucket";
        this.defaultVectorDimension = 1536;
        this.defaultTopK = 10;
        this.defaultSimilarityThreshold = 0.7;
        this.defaultBatchSize = 100;
        this.connectionTimeoutMs = 60000;
        this.readTimeoutMs = 60000;
        this.writeTimeoutMs = 60000;
        this.cacheSize = 1000;
        this.cacheTtlMs = 300000;
        this.cacheEnabled = true;
        this.debugEnabled = false;
    }

    /**
     * 从环境变量和系统属性创建配置
     * 优先级：系统属性 > 环境变量 > 默认值
     * 
     * @return 配置实例
     */
    public static InfluxDBConfiguration fromEnvironment() {
        return InfluxDBConfigurationLoader.fromEnvironment();
    }

    /**
     * 验证配置参数
     * 
     * @throws IllegalArgumentException 如果配置参数无效
     */
    public void validate() {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("InfluxDB URL cannot be null or empty");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("InfluxDB token cannot be null or empty");
        }
        if (org == null || org.trim().isEmpty()) {
            throw new IllegalArgumentException("InfluxDB organization cannot be null or empty");
        }
        if (bucket == null || bucket.trim().isEmpty()) {
            throw new IllegalArgumentException("InfluxDB bucket cannot be null or empty");
        }
        if (defaultVectorDimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive");
        }
        if (defaultTopK <= 0) {
            throw new IllegalArgumentException("Top K must be positive");
        }
        if (defaultSimilarityThreshold < 0.0 || defaultSimilarityThreshold > 1.0) {
            throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
        }
        if (defaultBatchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        if (connectionTimeoutMs <= 0) {
            throw new IllegalArgumentException("Connection timeout must be positive");
        }
        if (readTimeoutMs <= 0) {
            throw new IllegalArgumentException("Read timeout must be positive");
        }
        if (writeTimeoutMs <= 0) {
            throw new IllegalArgumentException("Write timeout must be positive");
        }
        if (cacheSize < 0) {
            throw new IllegalArgumentException("Cache size cannot be negative");
        }
        if (cacheTtlMs <= 0) {
            throw new IllegalArgumentException("Cache TTL must be positive");
        }
    }

    // Getter 和 Setter 方法
    public String getUrl() {
        return url;
    }

    public InfluxDBConfiguration setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getToken() {
        return token;
    }

    public InfluxDBConfiguration setToken(String token) {
        this.token = token;
        return this;
    }

    public String getOrg() {
        return org;
    }

    public InfluxDBConfiguration setOrg(String org) {
        this.org = org;
        return this;
    }

    public String getBucket() {
        return bucket;
    }

    public InfluxDBConfiguration setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public int getDefaultVectorDimension() {
        return defaultVectorDimension;
    }

    public InfluxDBConfiguration setDefaultVectorDimension(int defaultVectorDimension) {
        if (defaultVectorDimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive");
        }
        this.defaultVectorDimension = defaultVectorDimension;
        return this;
    }

    public int getDefaultTopK() {
        return defaultTopK;
    }

    public InfluxDBConfiguration setDefaultTopK(int defaultTopK) {
        if (defaultTopK <= 0) {
            throw new IllegalArgumentException("Top K must be positive");
        }
        this.defaultTopK = defaultTopK;
        return this;
    }

    public double getDefaultSimilarityThreshold() {
        return defaultSimilarityThreshold;
    }

    public InfluxDBConfiguration setDefaultSimilarityThreshold(double defaultSimilarityThreshold) {
        if (defaultSimilarityThreshold < 0.0 || defaultSimilarityThreshold > 1.0) {
            throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
        }
        this.defaultSimilarityThreshold = defaultSimilarityThreshold;
        return this;
    }

    public int getDefaultBatchSize() {
        return defaultBatchSize;
    }

    public InfluxDBConfiguration setDefaultBatchSize(int defaultBatchSize) {
        if (defaultBatchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        this.defaultBatchSize = defaultBatchSize;
        return this;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public InfluxDBConfiguration setConnectionTimeoutMs(int connectionTimeoutMs) {
        if (connectionTimeoutMs <= 0) {
            throw new IllegalArgumentException("Connection timeout must be positive");
        }
        this.connectionTimeoutMs = connectionTimeoutMs;
        return this;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public InfluxDBConfiguration setReadTimeoutMs(int readTimeoutMs) {
        if (readTimeoutMs < 0) {
            throw new IllegalArgumentException("Read timeout must be non-negative");
        }
        this.readTimeoutMs = readTimeoutMs;
        return this;
    }

    public int getWriteTimeoutMs() {
        return writeTimeoutMs;
    }

    public InfluxDBConfiguration setWriteTimeoutMs(int writeTimeoutMs) {
        if (writeTimeoutMs < 0) {
            throw new IllegalArgumentException("Write timeout must be non-negative");
        }
        this.writeTimeoutMs = writeTimeoutMs;
        return this;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public InfluxDBConfiguration setCacheSize(int cacheSize) {
        if (cacheSize < 0) {
            throw new IllegalArgumentException("Cache size must be non-negative");
        }
        this.cacheSize = cacheSize;
        return this;
    }

    public int getCacheTtlMs() {
        return cacheTtlMs;
    }

    public InfluxDBConfiguration setCacheTtlMs(int cacheTtlMs) {
        if (cacheTtlMs < 0) {
            throw new IllegalArgumentException("Cache TTL must be non-negative");
        }
        this.cacheTtlMs = cacheTtlMs;
        return this;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public InfluxDBConfiguration setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        return this;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public InfluxDBConfiguration setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
        return this;
    }

    /**
     * 构建器模式
     */
    public static InfluxDBConfigurationBuilder builder() {
        return new InfluxDBConfigurationBuilder();
    }

    /**
     * 复制配置
     */
    public InfluxDBConfiguration copy() {
        InfluxDBConfiguration copy = new InfluxDBConfiguration();
        copy.url = this.url;
        copy.token = this.token;
        copy.org = this.org;
        copy.bucket = this.bucket;
        copy.defaultVectorDimension = this.defaultVectorDimension;
        copy.defaultTopK = this.defaultTopK;
        copy.defaultSimilarityThreshold = this.defaultSimilarityThreshold;
        copy.defaultBatchSize = this.defaultBatchSize;
        copy.connectionTimeoutMs = this.connectionTimeoutMs;
        copy.readTimeoutMs = this.readTimeoutMs;
        copy.writeTimeoutMs = this.writeTimeoutMs;
        copy.cacheSize = this.cacheSize;
        copy.cacheTtlMs = this.cacheTtlMs;
        copy.cacheEnabled = this.cacheEnabled;
        copy.debugEnabled = this.debugEnabled;
        return copy;
    }

    /**
     * 验证配置是否有效
     */
    public boolean isValid() {
        return url != null && !url.trim().isEmpty() &&
               token != null && !token.trim().isEmpty() &&
               org != null && !org.trim().isEmpty() &&
               bucket != null && !bucket.trim().isEmpty();
    }

    /**
     * 构建器类
     */
    public static class InfluxDBConfigurationBuilder {
        private String url;
        private String token;
        private String org;
        private String bucket;
        private int defaultVectorDimension = 1536;
        private int defaultTopK = 10;
        private double defaultSimilarityThreshold = 0.7;
        private int defaultBatchSize = 1000;
        private int connectionTimeoutMs = 30000;
        private int readTimeoutMs = 30000;
        private int writeTimeoutMs = 30000;
        private int cacheSize = 1000;
        private int cacheTtlMs = 300000;
        private boolean cacheEnabled = true;
        private boolean debugEnabled = false;

        public InfluxDBConfigurationBuilder url(String url) {
            this.url = url;
            return this;
        }

        public InfluxDBConfigurationBuilder token(String token) {
            this.token = token;
            return this;
        }

        public InfluxDBConfigurationBuilder org(String org) {
            this.org = org;
            return this;
        }

        public InfluxDBConfigurationBuilder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public InfluxDBConfigurationBuilder defaultVectorDimension(int defaultVectorDimension) {
            this.defaultVectorDimension = defaultVectorDimension;
            return this;
        }

        public InfluxDBConfigurationBuilder defaultTopK(int defaultTopK) {
            this.defaultTopK = defaultTopK;
            return this;
        }

        public InfluxDBConfigurationBuilder defaultSimilarityThreshold(double defaultSimilarityThreshold) {
            this.defaultSimilarityThreshold = defaultSimilarityThreshold;
            return this;
        }

        public InfluxDBConfigurationBuilder defaultBatchSize(int defaultBatchSize) {
            this.defaultBatchSize = defaultBatchSize;
            return this;
        }

        public InfluxDBConfigurationBuilder connectionTimeoutMs(int connectionTimeoutMs) {
            this.connectionTimeoutMs = connectionTimeoutMs;
            return this;
        }

        public InfluxDBConfigurationBuilder readTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }

        public InfluxDBConfigurationBuilder writeTimeoutMs(int writeTimeoutMs) {
            this.writeTimeoutMs = writeTimeoutMs;
            return this;
        }

        public InfluxDBConfigurationBuilder cacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        public InfluxDBConfigurationBuilder cacheTtlMs(int cacheTtlMs) {
            this.cacheTtlMs = cacheTtlMs;
            return this;
        }

        public InfluxDBConfigurationBuilder cacheEnabled(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
            return this;
        }

        public InfluxDBConfigurationBuilder debugEnabled(boolean debugEnabled) {
            this.debugEnabled = debugEnabled;
            return this;
        }

        public InfluxDBConfiguration build() {
            InfluxDBConfiguration config = new InfluxDBConfiguration();
            config.url = this.url;
            config.token = this.token;
            config.org = this.org;
            config.bucket = this.bucket;
            config.defaultVectorDimension = this.defaultVectorDimension;
            config.defaultTopK = this.defaultTopK;
            config.defaultSimilarityThreshold = this.defaultSimilarityThreshold;
            config.defaultBatchSize = this.defaultBatchSize;
            config.connectionTimeoutMs = this.connectionTimeoutMs;
            config.readTimeoutMs = this.readTimeoutMs;
            config.writeTimeoutMs = this.writeTimeoutMs;
            config.cacheSize = this.cacheSize;
            config.cacheTtlMs = this.cacheTtlMs;
            config.cacheEnabled = this.cacheEnabled;
            config.debugEnabled = this.debugEnabled;
            
            // 验证配置
            if (config.url != null && config.token != null && config.org != null && config.bucket != null) {
                config.validate();
            }
            
            return config;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        InfluxDBConfiguration that = (InfluxDBConfiguration) obj;
        
        return defaultVectorDimension == that.defaultVectorDimension &&
               defaultTopK == that.defaultTopK &&
               Double.compare(that.defaultSimilarityThreshold, defaultSimilarityThreshold) == 0 &&
               defaultBatchSize == that.defaultBatchSize &&
               connectionTimeoutMs == that.connectionTimeoutMs &&
               readTimeoutMs == that.readTimeoutMs &&
               writeTimeoutMs == that.writeTimeoutMs &&
               cacheSize == that.cacheSize &&
               cacheTtlMs == that.cacheTtlMs &&
               cacheEnabled == that.cacheEnabled &&
               debugEnabled == that.debugEnabled &&
               java.util.Objects.equals(url, that.url) &&
               java.util.Objects.equals(token, that.token) &&
               java.util.Objects.equals(org, that.org) &&
               java.util.Objects.equals(bucket, that.bucket);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(url, token, org, bucket, defaultVectorDimension, 
                                     defaultTopK, defaultSimilarityThreshold, defaultBatchSize,
                                     connectionTimeoutMs, readTimeoutMs, writeTimeoutMs,
                                     cacheSize, cacheTtlMs, cacheEnabled, debugEnabled);
    }

    @Override
    public String toString() {
        return "InfluxDBConfiguration{" +
                "url=" + url +
                ", token=" + (token != null ? "***" : null) +
                ", org=" + org +
                ", bucket=" + bucket +
                ", defaultVectorDimension=" + defaultVectorDimension +
                ", defaultTopK=" + defaultTopK +
                ", defaultSimilarityThreshold=" + defaultSimilarityThreshold +
                ", defaultBatchSize=" + defaultBatchSize +
                ", connectionTimeoutMs=" + connectionTimeoutMs +
                ", readTimeoutMs=" + readTimeoutMs +
                ", writeTimeoutMs=" + writeTimeoutMs +
                ", cacheSize=" + cacheSize +
                ", cacheTtlMs=" + cacheTtlMs +
                ", cacheEnabled=" + cacheEnabled +
                ", debugEnabled=" + debugEnabled +
                '}';
    }
}
