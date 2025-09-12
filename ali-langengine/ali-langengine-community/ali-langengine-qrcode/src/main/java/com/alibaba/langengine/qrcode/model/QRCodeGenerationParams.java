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

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Data;

import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDateTime;


@Data
public class QRCodeGenerationParams {
    
    /**
     * QR code content data
     */
    private String data;
    
    /**
     * Output file name
     */
    private String fileName;
    
    /**
     * QR code width
     */
    private int width = 300;
    
    /**
     * QR code height
     */
    private int height = 300;
    
    /**
     * Foreground color (QR code color)
     */
    private Color foregroundColor = Color.BLACK;
    
    /**
     * Background color
     */
    private Color backgroundColor = Color.WHITE;
    
    /**
     * Error correction level
     */
    private ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.H;
    
    /**
     * Character set encoding
     */
    private String charset = "UTF-8";
    
    /**
     * Margin size
     */
    private int margin = 1;
    
    /**
     * Logo file path (optional)
     */
    private String logoPath;
    
    /**
     * Logo size ratio (logo size / qr code size)
     */
    private double logoRatio = 0.2;
    
    /**
     * Output directory
     */
    private Path outputDirectory;
    
    /**
     * Image format (PNG, JPG, etc.)
     */
    private String format = "PNG";
    
    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Constructor with required parameters
     */
    public QRCodeGenerationParams(String data, String fileName) {
        this.data = data;
        this.fileName = fileName;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with data, filename, and size
     */
    public QRCodeGenerationParams(String data, String fileName, int width, int height) {
        this(data, fileName);
        this.width = width;
        this.height = height;
    }
    
    /**
     * Default constructor
     */
    public QRCodeGenerationParams() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Validate parameters
     */
    public void validate() {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("QR code data cannot be null or empty");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (width < 100 || width > 2000) {
            throw new IllegalArgumentException("Width must be between 100 and 2000 pixels");
        }
        if (height < 100 || height > 2000) {
            throw new IllegalArgumentException("Height must be between 100 and 2000 pixels");
        }
        if (logoRatio < 0 || logoRatio > 0.3) {
            throw new IllegalArgumentException("Logo ratio must be between 0 and 0.3");
        }
    }
    
    /**
     * Check if logo is enabled
     */
    public boolean hasLogo() {
        return logoPath != null && !logoPath.trim().isEmpty();
    }
    
    /**
     * Get logo size
     */
    public int getLogoSize() {
        return (int) (Math.min(width, height) * logoRatio);
    }
    
    /**
     * Copy constructor
     */
    public QRCodeGenerationParams copy() {
        QRCodeGenerationParams copy = new QRCodeGenerationParams();
        copy.data = this.data;
        copy.fileName = this.fileName;
        copy.width = this.width;
        copy.height = this.height;
        copy.foregroundColor = this.foregroundColor;
        copy.backgroundColor = this.backgroundColor;
        copy.errorCorrectionLevel = this.errorCorrectionLevel;
        copy.charset = this.charset;
        copy.margin = this.margin;
        copy.logoPath = this.logoPath;
        copy.logoRatio = this.logoRatio;
        copy.outputDirectory = this.outputDirectory;
        copy.format = this.format;
        copy.createdAt = LocalDateTime.now();
        return copy;
    }
}
