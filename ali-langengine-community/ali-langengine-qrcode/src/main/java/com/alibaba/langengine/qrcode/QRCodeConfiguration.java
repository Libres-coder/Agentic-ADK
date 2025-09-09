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
package com.alibaba.langengine.qrcode;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class QRCodeConfiguration {
    
    /**
     * Default QR code size
     */
    public static final int DEFAULT_SIZE = 300;
    
    /**
     * Default output directory
     */
    public static final String DEFAULT_OUTPUT_DIR = "./qr_output";
    
    /**
     * Supported image formats
     */
    public static final String[] SUPPORTED_FORMATS = {"PNG", "JPG", "JPEG", "BMP", "GIF"};
    
    /**
     * Maximum QR code size
     */
    public static final int MAX_SIZE = 2000;
    
    /**
     * Minimum QR code size
     */
    public static final int MIN_SIZE = 100;
    
    /**
     * Default logo size ratio (logo size / qr code size)
     */
    public static final double DEFAULT_LOGO_RATIO = 0.2;
    
    /**
     * Maximum logo size ratio
     */
    public static final double MAX_LOGO_RATIO = 0.3;
    
    /**
     * Default thread pool size for batch operations
     */
    public static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    
    /**
     * Get QR code output directory from system property or return default
     */
    public static String getOutputDirectory() {
        return System.getProperty("qrcode.output.dir", DEFAULT_OUTPUT_DIR);
    }
    
    /**
     * Get batch processing thread pool size from system property or return default
     */
    public static int getThreadPoolSize() {
        String size = System.getProperty("qrcode.thread.pool.size");
        if (size != null) {
            try {
                return Integer.parseInt(size);
            } catch (NumberFormatException e) {
                // Fallback to default
            }
        }
        return DEFAULT_THREAD_POOL_SIZE;
    }
    
    /**
     * Check if image format is supported
     */
    public static boolean isSupportedFormat(String format) {
        if (format == null) {
            return false;
        }
        for (String supportedFormat : SUPPORTED_FORMATS) {
            if (supportedFormat.equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Load configuration from properties file
     * 
     * @param configPath path to configuration file
     */
    public static void loadConfiguration(String configPath) {
        try {
            java.util.Properties props = new java.util.Properties();
            try (java.io.InputStream input = QRCodeConfiguration.class.getClassLoader().getResourceAsStream(configPath)) {
                if (input != null) {
                    props.load(input);
                    // Update configuration values if present in properties file
                    // This provides a centralized way to override default settings
                    log.info("Configuration loaded from: {}", configPath);
                } else {
                    log.warn("Configuration file not found: {}, using defaults", configPath);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load configuration from {}: {}, using defaults", configPath, e.getMessage());
        }
    }
    
    /**
     * Validate file path for security (prevent path traversal)
     * 
     * @param path the file path to validate
     * @return sanitized path
     * @throws IllegalArgumentException if path is invalid
     */
    public static String validatePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        
        // Remove path traversal attempts
        String sanitized = path.replaceAll("\\.\\.", "").replaceAll("//+", "/");
        
        // Ensure path doesn't start with / or \ (relative paths only)
        if (sanitized.startsWith("/") || sanitized.startsWith("\\")) {
            throw new IllegalArgumentException("Absolute paths not allowed");
        }
        
        return sanitized;
    }
}
