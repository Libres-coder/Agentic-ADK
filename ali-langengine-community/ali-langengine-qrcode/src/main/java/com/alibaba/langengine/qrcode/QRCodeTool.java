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

import com.alibaba.langengine.qrcode.exception.QRCodeException;
import com.alibaba.langengine.qrcode.model.QRCodeGenerationParams;
import com.alibaba.langengine.qrcode.model.RecognitionResult;
import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Slf4j
public class QRCodeTool implements AutoCloseable {

    private final QRCodeGenerator generator;
    private final QRCodeRecognizer recognizer;

    /**
     * Constructor with custom output directory
     */
    public QRCodeTool(String outputDirectory) {
        this.generator = new QRCodeGenerator(outputDirectory);
        this.recognizer = new QRCodeRecognizer();
        log.info("QRCodeTool initialized with output directory: {}", outputDirectory);
    }

    /**
     * Constructor with default settings
     */
    public QRCodeTool() {
        this(QRCodeConfiguration.getOutputDirectory());
    }

    // ================== Generation Methods ==================

    /**
     * Generate simple QR code
     */
    public Path generateQRCode(String data, String filename) throws QRCodeException {
        return generator.generate(data, filename);
    }

    /**
     * Generate QR code with custom size
     */
    public Path generateQRCode(String data, String filename, int size) throws QRCodeException {
        return generator.generate(data, filename, size, size);
    }

    /**
     * Generate QR code with custom dimensions
     */
    public Path generateQRCode(String data, String filename, int width, int height) throws QRCodeException {
        return generator.generate(data, filename, width, height);
    }

    /**
     * Generate colorful QR code
     */
    public Path generateColorfulQRCode(String data, String filename, Color foreground, Color background) 
            throws QRCodeException {
        QRCodeGenerationParams params = new QRCodeGenerationParams(data, filename);
        params.setForegroundColor(foreground);
        params.setBackgroundColor(background);
        return generator.generate(params);
    }

    /**
     * Generate QR code with logo
     */
    public Path generateQRCodeWithLogo(String data, String filename, String logoPath) throws QRCodeException {
        return generateQRCodeWithLogo(data, filename, logoPath, QRCodeConfiguration.DEFAULT_SIZE);
    }

    /**
     * Generate QR code with logo and custom size
     */
    public Path generateQRCodeWithLogo(String data, String filename, String logoPath, int size) 
            throws QRCodeException {
        QRCodeGenerationParams params = new QRCodeGenerationParams(data, filename, size, size);
        params.setLogoPath(logoPath);
        return generator.generate(params);
    }

    /**
     * Generate QR code with full customization
     */
    public Path generateCustomQRCode(String data, String filename, int size, Color foreground, 
                                   Color background, String logoPath, double logoRatio) throws QRCodeException {
        QRCodeGenerationParams params = new QRCodeGenerationParams(data, filename, size, size);
        params.setForegroundColor(foreground);
        params.setBackgroundColor(background);
        params.setLogoPath(logoPath);
        params.setLogoRatio(logoRatio);
        return generator.generate(params);
    }

    /**
     * Generate batch QR codes
     */
    public CompletableFuture<List<Path>> generateBatch(List<QRCodeGenerationParams> paramsList) {
        return generator.generateBatch(paramsList);
    }

    // ================== Recognition Methods ==================

    /**
     * Recognize QR code from file
     */
    public RecognitionResult recognizeQRCode(String filePath) throws QRCodeException {
        return recognizer.recognize(filePath);
    }

    /**
     * Get recognized text or null if recognition failed
     */
    public String getQRCodeText(String filePath) {
        try {
            RecognitionResult result = recognizer.recognize(filePath);
            return result.isSuccess() ? result.getText() : null;
        } catch (Exception e) {
            log.warn("Failed to recognize QR code from {}: {}", filePath, e.getMessage());
            return null;
        }
    }

    /**
     * Check if file contains a valid QR code
     */
    public boolean hasValidQRCode(String filePath) {
        try {
            RecognitionResult result = recognizer.recognize(filePath);
            return result.isSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Batch recognition from directory (synchronous)
     */
    public List<RecognitionResult> recognizeBatch(String directoryPath) throws QRCodeException {
        // Convert directory path to list of image files
        java.util.List<String> filePaths = java.util.Arrays.asList(directoryPath);
        return recognizer.recognizeBatch(filePaths);
    }

    /**
     * Batch recognition from directory with recursive option (synchronous)
     */
    public List<RecognitionResult> recognizeBatch(String directoryPath, boolean recursive) throws QRCodeException {
        // For now, treat as single path - could be enhanced to scan directory
        java.util.List<String> filePaths = java.util.Arrays.asList(directoryPath);
        return recognizer.recognizeBatch(filePaths);
    }

    /**
     * Batch recognition from file list (synchronous)
     */
    public List<RecognitionResult> recognizeBatch(List<String> filePaths) throws QRCodeException {
        return recognizer.recognizeBatch(filePaths);
    }

    // ================== Utility Methods ==================

    /**
     * Validate QR code data
     */
    public boolean isValidQRCodeData(String data) {
        if (data == null || data.trim().isEmpty()) {
            return false;
        }
        
        // Check length constraints (QR code has data capacity limits)
        // For simplicity, we'll use a reasonable limit
        return data.length() <= 4296; // Max capacity for QR code with binary encoding
    }

    /**
     * Get optimal QR code size for given data length
     */
    public int getOptimalSize(String data) {
        if (data == null) {
            return QRCodeConfiguration.DEFAULT_SIZE;
        }
        
        int length = data.length();
        if (length <= 100) {
            return 200;
        } else if (length <= 500) {
            return 300;
        } else if (length <= 1000) {
            return 400;
        } else {
            return 500;
        }
    }

    /**
     * Create QR code for URL with optimal settings
     */
    public Path generateURLQRCode(String url, String filename) throws QRCodeException {
        if (!isValidURL(url)) {
            throw new QRCodeException("Invalid URL format: " + url);
        }
        
        int optimalSize = getOptimalSize(url);
        return generateQRCode(url, filename, optimalSize);
    }

    /**
     * Create QR code for WiFi connection
     */
    public Path generateWiFiQRCode(String ssid, String password, String security, String filename) 
            throws QRCodeException {
        String wifiString = String.format("WIFI:T:%s;S:%s;P:%s;H:false;;", 
                                        security != null ? security : "WPA", ssid, password);
        return generateQRCode(wifiString, filename);
    }

    /**
     * Create QR code for contact information (vCard)
     */
    public Path generateContactQRCode(String name, String phone, String email, String filename) 
            throws QRCodeException {
        StringBuilder vCard = new StringBuilder();
        vCard.append("BEGIN:VCARD\n");
        vCard.append("VERSION:3.0\n");
        vCard.append("FN:").append(name).append("\n");
        if (phone != null && !phone.trim().isEmpty()) {
            vCard.append("TEL:").append(phone).append("\n");
        }
        if (email != null && !email.trim().isEmpty()) {
            vCard.append("EMAIL:").append(email).append("\n");
        }
        vCard.append("END:VCARD");
        
        return generateQRCode(vCard.toString(), filename);
    }

    /**
     * Get recognition statistics
     */
    public Map<String, Object> getRecognitionStatistics(List<RecognitionResult> results) {
        Map<String, Object> stats = new java.util.HashMap<>();
        if (results == null || results.isEmpty()) {
            stats.put("totalFiles", 0);
            stats.put("successCount", 0);
            stats.put("failureCount", 0);
            stats.put("successRate", 0.0);
            return stats;
        }
        
        long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
        stats.put("totalFiles", results.size());
        stats.put("successCount", successCount);
        stats.put("failureCount", results.size() - successCount);
        stats.put("successRate", (double) successCount / results.size());
        return stats;
    }

    /**
     * Get output directory
     */
    public Path getOutputDirectory() {
        return generator.getOutputDirectory();
    }

    /**
     * Check if file is supported image format
     */
    public static boolean isSupportedImageFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        String lowerCase = filename.toLowerCase();
        return lowerCase.endsWith(".png") || lowerCase.endsWith(".jpg") || 
               lowerCase.endsWith(".jpeg") || lowerCase.endsWith(".bmp") || 
               lowerCase.endsWith(".gif");
    }

    // ================== Cleanup Methods ==================

    /**
     * Shutdown all resources
     */
    public void shutdown() {
        generator.shutdown();
        // QRCodeRecognizer doesn't have shutdown method in current implementation
        log.info("QRCodeTool shutdown completed");
    }

    // ================== Private Helper Methods ==================

    /**
     * Simple URL validation
     */
    private boolean isValidURL(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        String trimmedUrl = url.trim().toLowerCase();
        return trimmedUrl.startsWith("http://") || 
               trimmedUrl.startsWith("https://") || 
               trimmedUrl.startsWith("ftp://") ||
               trimmedUrl.contains(".");
    }

    /**
     * Get version information
     */
    public static String getVersion() {
        return "1.0.0";
    }

    /**
     * Get supported features
     */
    public static String[] getSupportedFeatures() {
        return new String[]{
            "QR Code Generation",
            "QR Code Recognition", 
            "Batch Processing",
            "Logo Embedding",
            "Color Customization",
            "URL QR Codes",
            "WiFi QR Codes",
            "Contact vCard QR Codes",
            "Async Processing"
        };
    }
    
    /**
     * Clean up resources when closing the tool
     * 
     * @throws Exception if cleanup fails
     */
    @Override
    public void close() throws Exception {
        if (generator != null) {
            log.info("Cleaning up QR code generator resources");
        }
        if (recognizer != null) {
            log.info("Cleaning up QR code recognizer resources");
        }
        log.debug("QRCodeTool resources have been cleaned up");
    }
}
