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
import com.alibaba.langengine.qrcode.model.RecognitionResult;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * QR Code recognizer with enhanced security and validation features
 * 
 * @author langengine-team
 */
@Slf4j
public class QRCodeRecognizer {
    
    /** Maximum file size for security (10MB) */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /** Allowed image file extensions */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".png", ".jpg", ".jpeg", ".bmp", ".gif");
    
    /** Default timeout for batch operations */
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    
    private final MultiFormatReader reader;
    
    public QRCodeRecognizer() {
        this.reader = new MultiFormatReader();
        // Configure to read QR codes and other formats
        this.reader.setHints(java.util.Collections.singletonMap(DecodeHintType.POSSIBLE_FORMATS, 
                             java.util.Collections.singletonList(BarcodeFormat.QR_CODE)));
    }

    /**
     * Recognize QR code from file with security validation
     */
    public RecognitionResult recognize(String filePath) throws QRCodeException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new QRCodeException("File path cannot be null or empty");
        }
        
        File file = new File(filePath);
        return recognize(file);
    }

    /**
     * Recognize QR code from file with comprehensive validation
     */
    public RecognitionResult recognize(File file) throws QRCodeException {
        // Security validations
        validateFile(file);
        
        try {
            log.debug("Starting QR code recognition for file: {}", file.getAbsolutePath());
            
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                log.warn("Failed to load image from file: {}", file.getAbsolutePath());
                return new RecognitionResult(file, "Failed to load image file");
            }

            // Convert to luminance source
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Attempt to decode
            Result result = reader.decode(bitmap);
            
            if (result != null) {
                log.debug("Successfully recognized QR code from file: {}", file.getAbsolutePath());
                RecognitionResult recognitionResult = new RecognitionResult(result.getText(), file);
                
                // Set additional metadata
                recognitionResult.setBarcodeFormat(result.getBarcodeFormat());
                recognitionResult.setResultPoints(result.getResultPoints());
                recognitionResult.setImageWidth(image.getWidth());
                recognitionResult.setImageHeight(image.getHeight());
                
                return recognitionResult;
            } else {
                log.warn("No QR code found in file: {}", file.getAbsolutePath());
                return new RecognitionResult(file, "No QR code found in image");
            }
            
        } catch (NotFoundException e) {
            log.debug("No barcode found in file: {}", file.getAbsolutePath());
            return new RecognitionResult(file, "No QR code found in image");
        } catch (IOException e) {
            log.error("IO error while reading file: {}", file.getAbsolutePath(), e);
            throw new QRCodeException("Failed to read image file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error during QR code recognition for file: {}", file.getAbsolutePath(), e);
            throw new QRCodeException("Failed to recognize QR code: " + e.getMessage(), e);
        }
    }

    /**
     * Batch recognize QR codes from multiple files with timeout support
     */
    public List<RecognitionResult> recognizeBatch(List<String> filePaths) throws QRCodeException {
        return recognizeBatch(filePaths, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Batch recognize QR codes with custom timeout
     */
    public List<RecognitionResult> recognizeBatch(List<String> filePaths, int timeoutSeconds) throws QRCodeException {
        if (filePaths == null || filePaths.isEmpty()) {
            throw new IllegalArgumentException("File paths list cannot be null or empty");
        }

        log.info("Starting batch QR code recognition for {} files with {}s timeout", filePaths.size(), timeoutSeconds);
        
        List<RecognitionResult> results = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(filePaths.size(), 4));
        
        try {
            List<Future<RecognitionResult>> futures = new ArrayList<>();
            
            // Submit all tasks
            for (String filePath : filePaths) {
                futures.add(executor.submit(() -> {
                    try {
                        return recognize(filePath);
                    } catch (QRCodeException e) {
                        log.warn("Failed to recognize QR code from file: {}", filePath, e);
                        return new RecognitionResult(new File(filePath), e.getMessage());
                    }
                }));
            }
            
            // Collect results with timeout
            for (Future<RecognitionResult> future : futures) {
                try {
                    RecognitionResult result = future.get(timeoutSeconds, TimeUnit.SECONDS);
                    results.add(result);
                } catch (TimeoutException e) {
                    log.warn("Timeout occurred during batch recognition");
                    future.cancel(true);
                    results.add(new RecognitionResult(new File("unknown"), "Operation timed out"));
                } catch (Exception e) {
                    log.error("Error during batch recognition", e);
                    results.add(new RecognitionResult(new File("unknown"), "Recognition failed: " + e.getMessage()));
                }
            }
            
            log.info("Batch QR code recognition completed. Processed {} files", results.size());
            return results;
            
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Comprehensive file security validation
     */
    private void validateFile(File file) throws QRCodeException {
        if (file == null) {
            throw new QRCodeException("File cannot be null");
        }

        if (!file.exists()) {
            throw new QRCodeException("File does not exist: " + file.getAbsolutePath());
        }

        if (!file.isFile()) {
            throw new QRCodeException("Path is not a file: " + file.getAbsolutePath());
        }

        if (!file.canRead()) {
            throw new QRCodeException("File is not readable: " + file.getAbsolutePath());
        }

        // Check file size
        long fileSize = file.length();
        if (fileSize > MAX_FILE_SIZE) {
            throw new QRCodeException("File size exceeds maximum allowed size (" + MAX_FILE_SIZE + " bytes): " + fileSize);
        }

        if (fileSize == 0) {
            throw new QRCodeException("File is empty: " + file.getAbsolutePath());
        }

        // Validate file extension
        String fileName = file.getName().toLowerCase();
        boolean hasValidExtension = ALLOWED_EXTENSIONS.stream()
                .anyMatch(fileName::endsWith);
                
        if (!hasValidExtension) {
            throw new QRCodeException("File extension not allowed. Allowed extensions: " + ALLOWED_EXTENSIONS);
        }

        // Check for path traversal attacks
        try {
            Path normalizedPath = file.toPath().normalize();
            if (!normalizedPath.equals(file.toPath())) {
                throw new QRCodeException("Invalid file path detected (possible path traversal): " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new QRCodeException("Invalid file path: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Check if file can be processed (convenience method for validation)
     */
    public boolean canProcess(File file) {
        try {
            validateFile(file);
            return true;
        } catch (QRCodeException e) {
            log.debug("File cannot be processed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get supported file extensions
     */
    public List<String> getSupportedExtensions() {
        return new ArrayList<>(ALLOWED_EXTENSIONS);
    }

    /**
     * Get maximum allowed file size
     */
    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }
}
