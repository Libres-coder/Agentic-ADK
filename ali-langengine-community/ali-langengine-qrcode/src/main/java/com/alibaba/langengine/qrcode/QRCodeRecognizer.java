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
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class QRCodeRecognizer {

    private final ExecutorService executorService;
    private final Reader reader;
    private final Map<DecodeHintType, Object> hints;

    /**
     * Constructor
     */
    public QRCodeRecognizer() {
        this.executorService = Executors.newFixedThreadPool(QRCodeConfiguration.getThreadPoolSize());
        this.reader = new MultiFormatReader();
        this.hints = createDecodeHints();
        log.info("QRCodeRecognizer initialized with thread pool size: {}", QRCodeConfiguration.getThreadPoolSize());
    }

    /**
     * Recognize QR code from file path
     */
    public RecognitionResult recognize(String filePath) throws QRCodeException {
        if (StringUtils.isBlank(filePath)) {
            throw new QRCodeException("File path cannot be null or empty");
        }
        
        File file = new File(filePath);
        return recognize(file);
    }

    /**
     * Recognize QR code from file
     */
    public RecognitionResult recognize(File file) throws QRCodeException {
        if (file == null || !file.exists()) {
            throw new QRCodeException("File does not exist: " + (file != null ? file.getPath() : "null"));
        }

        if (!file.isFile()) {
            throw new QRCodeException("Path is not a file: " + file.getPath());
        }

        log.debug("Recognizing QR code from file: {}", file.getPath());

        try {
            // Load image
            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage == null) {
                String errorMsg = "Unable to read image file or unsupported format: " + file.getName();
                log.warn(errorMsg);
                return new RecognitionResult(file, errorMsg);
            }

            // Create recognition result
            RecognitionResult result = recognizeFromImage(bufferedImage, file);
            
            log.debug("Recognition completed for {}: success={}", file.getName(), result.isSuccess());
            return result;

        } catch (IOException e) {
            String errorMsg = "IO error reading file: " + e.getMessage();
            log.error(errorMsg, e);
            return new RecognitionResult(file, errorMsg);
        } catch (Exception e) {
            String errorMsg = "Unexpected error during recognition: " + e.getMessage();
            log.error(errorMsg, e);
            return new RecognitionResult(file, errorMsg);
        }
    }

    /**
     * Recognize QR code from BufferedImage
     */
    public RecognitionResult recognizeFromImage(BufferedImage image, File sourceFile) {
        try {
            // Store image dimensions
            int width = image.getWidth();
            int height = image.getHeight();

            // Convert to luminance source
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Decode QR code
            Result decodeResult = reader.decode(bitmap, hints);

            // Create successful result
            RecognitionResult result = new RecognitionResult(decodeResult.getText(), sourceFile);
            result.setBarcodeFormat(decodeResult.getBarcodeFormat());
            result.setResultPoints(decodeResult.getResultPoints());
            result.setImageWidth(width);
            result.setImageHeight(height);

            log.debug("Successfully decoded QR code: {}", decodeResult.getText());
            return result;

        } catch (NotFoundException e) {
            String errorMsg = "No QR code found in image";
            log.debug(errorMsg + ": {}", sourceFile.getName());
            return new RecognitionResult(sourceFile, errorMsg);
        } catch (ChecksumException e) {
            String errorMsg = "QR code checksum error (corrupted data)";
            log.debug(errorMsg + ": {}", sourceFile.getName());
            return new RecognitionResult(sourceFile, errorMsg);
        } catch (FormatException e) {
            String errorMsg = "QR code format error";
            log.debug(errorMsg + ": {}", sourceFile.getName());
            return new RecognitionResult(sourceFile, errorMsg);
        } catch (Exception e) {
            String errorMsg = "Recognition error: " + e.getMessage();
            log.error(errorMsg, e);
            return new RecognitionResult(sourceFile, errorMsg);
        }
    }

    /**
     * Batch recognition from directory
     */
    public CompletableFuture<List<RecognitionResult>> recognizeBatch(String directoryPath) {
        return recognizeBatch(directoryPath, true);
    }

    /**
     * Batch recognition from directory with recursive option
     */
    public CompletableFuture<List<RecognitionResult>> recognizeBatch(String directoryPath, boolean recursive) {
        if (StringUtils.isBlank(directoryPath)) {
            throw new QRCodeException("Directory path cannot be null or empty");
        }

        Path dir = Paths.get(directoryPath);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new QRCodeException("Directory does not exist or is not a directory: " + directoryPath);
        }

        log.info("Starting batch recognition from directory: {} (recursive={})", directoryPath, recursive);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Find image files
                List<File> imageFiles = findImageFiles(dir, recursive);
                log.info("Found {} image files for recognition", imageFiles.size());

                if (imageFiles.isEmpty()) {
                    log.warn("No image files found in directory: {}", directoryPath);
                    return new ArrayList<>();
                }

                // Process files concurrently
                List<CompletableFuture<RecognitionResult>> futures = imageFiles.stream()
                    .map(file -> CompletableFuture.supplyAsync(() -> recognize(file), executorService))
                    .collect(Collectors.toList());

                // Wait for all results
                List<RecognitionResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

                // Log summary
                long successCount = results.stream().filter(RecognitionResult::isSuccess).count();
                log.info("Batch recognition completed: {}/{} successful", successCount, results.size());

                return results;

            } catch (IOException e) {
                String errorMsg = "Error reading directory: " + e.getMessage();
                log.error(errorMsg, e);
                throw new QRCodeException(errorMsg, e);
            }
        }, executorService);
    }

    /**
     * Batch recognition from file list
     */
    public CompletableFuture<List<RecognitionResult>> recognizeBatch(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            throw new QRCodeException("File paths list cannot be null or empty");
        }

        log.info("Starting batch recognition for {} files", filePaths.size());

        return CompletableFuture.supplyAsync(() -> {
            List<CompletableFuture<RecognitionResult>> futures = filePaths.stream()
                .map(path -> CompletableFuture.supplyAsync(() -> recognize(path), executorService))
                .collect(Collectors.toList());

            List<RecognitionResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

            long successCount = results.stream().filter(RecognitionResult::isSuccess).count();
            log.info("Batch recognition completed: {}/{} successful", successCount, results.size());

            return results;
        }, executorService);
    }

    /**
     * Get recognition statistics
     */
    public Map<String, Object> getRecognitionStatistics(List<RecognitionResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> stats = new HashMap<>();
        
        int total = results.size();
        long successful = results.stream().filter(RecognitionResult::isSuccess).count();
        long failed = total - successful;
        
        stats.put("total", total);
        stats.put("successful", successful);
        stats.put("failed", failed);
        stats.put("successRate", total > 0 ? (double) successful / total : 0.0);
        
        // Group by error types
        Map<String, Long> errorTypes = results.stream()
            .filter(r -> !r.isSuccess())
            .collect(Collectors.groupingBy(
                r -> r.getErrorMessage() != null ? r.getErrorMessage() : "Unknown error",
                Collectors.counting()
            ));
        stats.put("errorTypes", errorTypes);

        // Group by barcode formats
        Map<BarcodeFormat, Long> formats = results.stream()
            .filter(RecognitionResult::isSuccess)
            .filter(r -> r.getBarcodeFormat() != null)
            .collect(Collectors.groupingBy(
                RecognitionResult::getBarcodeFormat,
                Collectors.counting()
            ));
        stats.put("barcodeFormats", formats);

        return stats;
    }

    /**
     * Find image files in directory
     */
    private List<File> findImageFiles(Path directory, boolean recursive) throws IOException {
        Set<String> supportedExtensions = Set.of("png", "jpg", "jpeg", "bmp", "gif", "tiff", "webp");
        
        try (Stream<Path> paths = recursive ? Files.walk(directory) : Files.list(directory)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String filename = path.getFileName().toString().toLowerCase();
                    return supportedExtensions.stream().anyMatch(ext -> filename.endsWith("." + ext));
                })
                .map(Path::toFile)
                .collect(Collectors.toList());
        }
    }

    /**
     * Create decode hints for better recognition
     */
    private Map<DecodeHintType, Object> createDecodeHints() {
        Map<DecodeHintType, Object> hints = new HashMap<>();
        
        // Try harder to find codes
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        
        // Support multiple formats
        hints.put(DecodeHintType.POSSIBLE_FORMATS, 
            Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.DATA_MATRIX, BarcodeFormat.AZTEC));
        
        // Character set
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        
        return hints;
    }

    /**
     * Shutdown executor service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            log.info("QRCodeRecognizer executor service shutdown");
        }
    }

    /**
     * Check if file is supported image format
     */
    public static boolean isSupportedImageFile(String filename) {
        if (StringUtils.isBlank(filename)) {
            return false;
        }
        
        String lowercaseFilename = filename.toLowerCase();
        return Arrays.stream(QRCodeConfiguration.SUPPORTED_FORMATS)
            .anyMatch(format -> lowercaseFilename.endsWith("." + format.toLowerCase()));
    }
}
