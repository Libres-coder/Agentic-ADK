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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Slf4j
public class QRCodeGenerator {

    private final Path outputDirectory;
    private final ExecutorService executorService;

    /**
     * Constructor with output directory
     */
    public QRCodeGenerator(String outputDirectoryPath) {
        this.outputDirectory = createOutputDirectory(outputDirectoryPath);
        this.executorService = Executors.newFixedThreadPool(QRCodeConfiguration.getThreadPoolSize());
        log.info("QRCodeGenerator initialized with output directory: {}", outputDirectory);
    }

    /**
     * Constructor with default output directory
     */
    public QRCodeGenerator() {
        this(QRCodeConfiguration.getOutputDirectory());
    }

    /**
     * Generate QR code with basic parameters
     */
    public Path generate(String data, String filename) throws QRCodeException {
        validateInput(data, filename);
        return generate(data, filename, QRCodeConfiguration.DEFAULT_SIZE, QRCodeConfiguration.DEFAULT_SIZE);
    }
    
    /**
     * Validate input parameters for security and correctness
     */
    private void validateInput(String data, String filename) throws QRCodeException {
        if (data == null || data.trim().isEmpty()) {
            throw new QRCodeException("Data cannot be null or empty");
        }
        
        if (data.length() > 4296) { // QR Code Version 40 limit
            throw new QRCodeException("Data exceeds maximum QR code capacity (4296 characters)");
        }
        
        if (filename == null || filename.trim().isEmpty()) {
            throw new QRCodeException("Filename cannot be null or empty");
        }
        
        // Prevent path traversal attacks
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new QRCodeException("Filename contains invalid characters");
        }
        
        // Check for valid filename characters
        if (!filename.matches("^[a-zA-Z0-9._-]+$")) {
            throw new QRCodeException("Filename contains invalid characters. Use only alphanumeric, dots, hyphens, and underscores");
        }
    }

    /**
     * Generate QR code with specified size
     */
    public Path generate(String data, String filename, int width, int height) throws QRCodeException {
        validateInput(data, filename, width, height);
        QRCodeGenerationParams params = new QRCodeGenerationParams(data, filename, width, height);
        params.setOutputDirectory(outputDirectory);
        return generate(params);
    }
    
    /**
     * Validate input parameters including dimensions
     */
    private void validateInput(String data, String filename, int width, int height) throws QRCodeException {
        validateInput(data, filename);
        
        if (width < 50 || width > 2000) {
            throw new QRCodeException("Width must be between 50 and 2000 pixels");
        }
        
        if (height < 50 || height > 2000) {
            throw new QRCodeException("Height must be between 50 and 2000 pixels");
        }
    }

    /**
     * Generate QR code with full parameters
     */
    public Path generate(QRCodeGenerationParams params) throws QRCodeException {
        try {
            params.validate();
            
            // Set output directory if not specified
            if (params.getOutputDirectory() == null) {
                params.setOutputDirectory(outputDirectory);
            }

            log.debug("Generating QR code: data={}, filename={}, size={}x{}", 
                     params.getData(), params.getFileName(), params.getWidth(), params.getHeight());

            // Create hints for encoding
            Map<EncodeHintType, Object> hints = createEncodingHints(params);

            // Generate QR code matrix
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(
                params.getData(), 
                BarcodeFormat.QR_CODE, 
                params.getWidth(), 
                params.getHeight(), 
                hints
            );

            // Determine output file path
            Path outputPath = resolveOutputPath(params);

            // Generate image
            if (params.hasLogo()) {
                generateWithLogo(bitMatrix, params, outputPath);
            } else {
                generateBasic(bitMatrix, params, outputPath);
            }

            log.info("QR code generated successfully: {}", outputPath);
            return outputPath;

        } catch (WriterException e) {
            String errorMsg = String.format("Failed to encode QR code data: %s", params.getData());
            log.error(errorMsg, e);
            throw new QRCodeException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = String.format("Failed to write QR code to file: %s", params.getFileName());
            log.error(errorMsg, e);
            throw new QRCodeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = String.format("Unexpected error generating QR code: %s", e.getMessage());
            log.error(errorMsg, e);
            throw new QRCodeException(errorMsg, e);
        }
    }

    /**
     * Generate multiple QR codes asynchronously
     */
    public CompletableFuture<List<Path>> generateBatch(List<QRCodeGenerationParams> paramsList) {
        log.info("Starting batch generation of {} QR codes", paramsList.size());
        
        List<CompletableFuture<Path>> futures = paramsList.stream()
            .map(params -> CompletableFuture.supplyAsync(() -> {
                try {
                    return generate(params);
                } catch (QRCodeException e) {
                    log.error("Failed to generate QR code in batch: {}", params.getFileName(), e);
                    throw new RuntimeException(e);
                }
            }, executorService))
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }

    /**
     * Generate QR code with logo
     */
    private void generateWithLogo(BitMatrix bitMatrix, QRCodeGenerationParams params, Path outputPath) 
            throws IOException {
        
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        
        // Create base image with custom colors
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Draw QR code pattern
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 
                    params.getForegroundColor().getRGB() : params.getBackgroundColor().getRGB());
            }
        }

        // Load and draw logo
        try {
            BufferedImage logo = ImageIO.read(new File(params.getLogoPath()));
            int logoSize = params.getLogoSize();
            int logoX = (width - logoSize) / 2;
            int logoY = (height - logoSize) / 2;

            // Create white background for logo
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(logoX - 5, logoY - 5, logoSize + 10, logoSize + 10, 10, 10);
            
            // Draw logo
            g2d.drawImage(logo, logoX, logoY, logoSize, logoSize, null);
            
            // Add border around logo
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(new RoundRectangle2D.Float(logoX - 2, logoY - 2, logoSize + 4, logoSize + 4, 8, 8));
            
            log.debug("Logo embedded successfully: size={}x{}, position=({},{})", 
                     logoSize, logoSize, logoX, logoY);
                     
        } catch (IOException e) {
            log.warn("Failed to load logo from path: {}, generating without logo", params.getLogoPath());
        }

        g2d.dispose();
        
        // Write to file
        ImageIO.write(image, params.getFormat(), outputPath.toFile());
    }

    /**
     * Generate basic QR code without logo
     */
    private void generateBasic(BitMatrix bitMatrix, QRCodeGenerationParams params, Path outputPath) 
            throws IOException {
        
        MatrixToImageConfig config = new MatrixToImageConfig(
            params.getForegroundColor().getRGB(),
            params.getBackgroundColor().getRGB()
        );
        
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix, config);
        ImageIO.write(image, params.getFormat(), outputPath.toFile());
    }

    /**
     * Create encoding hints
     */
    private Map<EncodeHintType, Object> createEncodingHints(QRCodeGenerationParams params) {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, params.getCharset());
        hints.put(EncodeHintType.ERROR_CORRECTION, params.getErrorCorrectionLevel());
        hints.put(EncodeHintType.MARGIN, params.getMargin());
        return hints;
    }

    /**
     * Resolve output file path
     */
    private Path resolveOutputPath(QRCodeGenerationParams params) {
        String filename = params.getFileName();
        if (!filename.toLowerCase().endsWith("." + params.getFormat().toLowerCase())) {
            filename += "." + params.getFormat().toLowerCase();
        }
        return params.getOutputDirectory().resolve(filename);
    }

    /**
     * Create output directory
     */
    private Path createOutputDirectory(String outputDirectoryPath) {
        try {
            Path path = Paths.get(outputDirectoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Created output directory: {}", path);
            }
            return path;
        } catch (IOException e) {
            String errorMsg = String.format("Failed to create output directory: %s", outputDirectoryPath);
            log.error(errorMsg, e);
            throw new QRCodeException(errorMsg, e);
        }
    }

    /**
     * Get output directory
     */
    public Path getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Shutdown executor service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            log.info("QRCodeGenerator executor service shutdown");
        }
    }


}
