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
package com.alibaba.langengine.qrcode.model;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import lombok.Data;

import java.io.File;
import java.time.LocalDateTime;


@Data
public class RecognitionResult {
    
    /**
     * Recognized text content
     */
    private String text;
    
    /**
     * Source file path
     */
    private String filePath;
    
    /**
     * File name
     */
    private String fileName;
    
    /**
     * Recognition success flag
     */
    private boolean success;
    
    /**
     * Error message if recognition failed
     */
    private String errorMessage;
    
    /**
     * Barcode format
     */
    private BarcodeFormat barcodeFormat;
    
    /**
     * Result points (corner positions of QR code)
     */
    private ResultPoint[] resultPoints;
    
    /**
     * Recognition timestamp
     */
    private LocalDateTime recognizedAt;
    
    /**
     * File size in bytes
     */
    private long fileSize;
    
    /**
     * Image width
     */
    private int imageWidth;
    
    /**
     * Image height
     */
    private int imageHeight;
    
    /**
     * Constructor for successful recognition
     */
    public RecognitionResult(String text, File file) {
        this.text = text;
        this.filePath = file.getAbsolutePath();
        this.fileName = file.getName();
        this.success = true;
        this.recognizedAt = LocalDateTime.now();
        this.fileSize = file.length();
    }
    
    /**
     * Constructor for failed recognition
     */
    public RecognitionResult(File file, String errorMessage) {
        this.filePath = file.getAbsolutePath();
        this.fileName = file.getName();
        this.success = false;
        this.errorMessage = errorMessage;
        this.recognizedAt = LocalDateTime.now();
        this.fileSize = file.length();
    }
    
    /**
     * Default constructor
     */
    public RecognitionResult() {
        this.recognizedAt = LocalDateTime.now();
    }
    
    /**
     * Check if QR code has position markers
     */
    public boolean hasPositionMarkers() {
        return resultPoints != null && resultPoints.length > 0;
    }
    
    /**
     * Get confidence score based on result points availability
     */
    public double getConfidenceScore() {
        if (!success) {
            return 0.0;
        }
        if (hasPositionMarkers()) {
            return 1.0;
        }
        return 0.8;
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("RecognitionResult{success=true, text='%s', file='%s'}", 
                               text, fileName);
        } else {
            return String.format("RecognitionResult{success=false, error='%s', file='%s'}", 
                               errorMessage, fileName);
        }
    }
}
