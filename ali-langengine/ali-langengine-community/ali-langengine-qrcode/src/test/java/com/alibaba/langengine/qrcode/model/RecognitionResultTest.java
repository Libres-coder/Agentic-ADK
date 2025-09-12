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
import org.junit.jupiter.api.*;
import java.io.File;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RecognitionResultTest {

    private File testFile;

    @BeforeEach
    void setUp() {
        testFile = new File("test-qr.png");
    }

    @Test
    @Order(1)
    void testConstructorWithSuccessfulRecognition() {
        String expectedText = "Hello World";
        RecognitionResult result = new RecognitionResult(expectedText, testFile);

        assertTrue(result.isSuccess());
        assertEquals(expectedText, result.getText());
        assertEquals(testFile.getAbsolutePath(), result.getFilePath());
        assertEquals(testFile.getName(), result.getFileName());
        assertNull(result.getErrorMessage());
        assertNotNull(result.getRecognizedAt());
        assertEquals(testFile.length(), result.getFileSize());
    }

    @Test
    @Order(2)
    void testConstructorWithFailedRecognition() {
        String expectedError = "No QR code found";
        RecognitionResult result = new RecognitionResult(testFile, expectedError);

        assertFalse(result.isSuccess());
        assertNull(result.getText());
        assertEquals(testFile.getAbsolutePath(), result.getFilePath());
        assertEquals(testFile.getName(), result.getFileName());
        assertEquals(expectedError, result.getErrorMessage());
        assertNotNull(result.getRecognizedAt());
        assertEquals(testFile.length(), result.getFileSize());
    }

    @Test
    @Order(3)
    void testDefaultConstructor() {
        RecognitionResult result = new RecognitionResult();

        assertFalse(result.isSuccess());
        assertNull(result.getText());
        assertNull(result.getFilePath());
        assertNull(result.getFileName());
        assertNotNull(result.getRecognizedAt());
    }

    @Test
    @Order(4)
    void testSettersAndGetters() {
        RecognitionResult result = new RecognitionResult();
        
        String testText = "Test QR Code Content";
        String testFilePath = "/path/to/test.png";
        String testFileName = "test.png";
        String testError = "Test error message";
        BarcodeFormat testFormat = BarcodeFormat.QR_CODE;
        ResultPoint[] testPoints = new ResultPoint[]{new ResultPoint(10, 10)};
        LocalDateTime testTime = LocalDateTime.now();
        
        result.setText(testText);
        result.setFilePath(testFilePath);
        result.setFileName(testFileName);
        result.setSuccess(true);
        result.setErrorMessage(testError);
        result.setBarcodeFormat(testFormat);
        result.setResultPoints(testPoints);
        result.setRecognizedAt(testTime);
        result.setFileSize(1024L);
        result.setImageWidth(300);
        result.setImageHeight(300);

        assertEquals(testText, result.getText());
        assertEquals(testFilePath, result.getFilePath());
        assertEquals(testFileName, result.getFileName());
        assertTrue(result.isSuccess());
        assertEquals(testError, result.getErrorMessage());
        assertEquals(testFormat, result.getBarcodeFormat());
        assertEquals(testPoints, result.getResultPoints());
        assertEquals(testTime, result.getRecognizedAt());
        assertEquals(1024L, result.getFileSize());
        assertEquals(300, result.getImageWidth());
        assertEquals(300, result.getImageHeight());
    }

    @Test
    @Order(5)
    void testHasPositionMarkers() {
        RecognitionResult result = new RecognitionResult();
        
        // Test without result points
        assertFalse(result.hasPositionMarkers());
        
        // Test with empty result points
        result.setResultPoints(new ResultPoint[0]);
        assertFalse(result.hasPositionMarkers());
        
        // Test with result points
        result.setResultPoints(new ResultPoint[]{new ResultPoint(10, 10)});
        assertTrue(result.hasPositionMarkers());
    }

    @Test
    @Order(6)
    void testGetConfidenceScore() {
        RecognitionResult result = new RecognitionResult();
        
        // Test failed recognition
        result.setSuccess(false);
        assertEquals(0.0, result.getConfidenceScore(), 0.001);
        
        // Test successful recognition without position markers
        result.setSuccess(true);
        result.setResultPoints(null);
        assertEquals(0.8, result.getConfidenceScore(), 0.001);
        
        // Test successful recognition with position markers
        result.setResultPoints(new ResultPoint[]{new ResultPoint(10, 10)});
        assertEquals(1.0, result.getConfidenceScore(), 0.001);
    }

    @Test
    @Order(7)
    void testToStringForSuccessfulResult() {
        String testText = "Hello World";
        RecognitionResult result = new RecognitionResult(testText, testFile);
        
        String resultString = result.toString();
        assertTrue(resultString.contains("success=true"));
        assertTrue(resultString.contains(testText));
        assertTrue(resultString.contains(testFile.getName()));
    }

    @Test
    @Order(8)
    void testToStringForFailedResult() {
        String errorMessage = "No QR code found";
        RecognitionResult result = new RecognitionResult(testFile, errorMessage);
        
        String resultString = result.toString();
        assertTrue(resultString.contains("success=false"));
        assertTrue(resultString.contains(errorMessage));
        assertTrue(resultString.contains(testFile.getName()));
    }

    @Test
    @Order(9)
    void testEqualsAndHashCode() {
        RecognitionResult result1 = new RecognitionResult("test", testFile);
        
        try {
            Thread.sleep(1); // Ensure different timestamps
        } catch (InterruptedException e) {
            // Ignore
        }
        
        RecognitionResult result2 = new RecognitionResult("test", testFile);
        
        // Note: Since RecognitionResult uses lombok @Data annotation,
        // equals and hashCode are auto-generated based on all fields
        // The timestamp will be different, so they won't be equal
        // This is expected behavior for this class
        assertNotEquals(result1, result2);
    }

    @Test
    @Order(10)
    void testNullValues() {
        RecognitionResult result = new RecognitionResult();
        
        // Test null text
        result.setText(null);
        assertNull(result.getText());
        
        // Test null file path
        result.setFilePath(null);
        assertNull(result.getFilePath());
        
        // Test null error message
        result.setErrorMessage(null);
        assertNull(result.getErrorMessage());
        
        // Test null barcode format
        result.setBarcodeFormat(null);
        assertNull(result.getBarcodeFormat());
    }

    @Test
    @Order(11)
    void testEdgeCases() {
        // Test with empty text
        RecognitionResult result = new RecognitionResult("", testFile);
        assertEquals("", result.getText());
        assertTrue(result.isSuccess());
        
        // Test with very long text
        String longText = "A".repeat(1000);
        result = new RecognitionResult(longText, testFile);
        assertEquals(longText, result.getText());
        assertTrue(result.isSuccess());
        
        // Test with special characters
        String specialText = "测试中文 Special chars: !@#$%^&*()";
        result = new RecognitionResult(specialText, testFile);
        assertEquals(specialText, result.getText());
        assertTrue(result.isSuccess());
    }

    @Test
    @Order(12)
    void testImageDimensions() {
        RecognitionResult result = new RecognitionResult();
        
        // Test default values
        assertEquals(0, result.getImageWidth());
        assertEquals(0, result.getImageHeight());
        
        // Test setting dimensions
        result.setImageWidth(1920);
        result.setImageHeight(1080);
        assertEquals(1920, result.getImageWidth());
        assertEquals(1080, result.getImageHeight());
        
        // Test negative dimensions (edge case)
        result.setImageWidth(-1);
        result.setImageHeight(-1);
        assertEquals(-1, result.getImageWidth());
        assertEquals(-1, result.getImageHeight());
    }

    @Test
    @Order(13)
    void testFileSizeHandling() {
        RecognitionResult result = new RecognitionResult();
        
        // Test default file size
        assertEquals(0L, result.getFileSize());
        
        // Test large file size
        long largeSize = Long.MAX_VALUE;
        result.setFileSize(largeSize);
        assertEquals(largeSize, result.getFileSize());
        
        // Test zero file size
        result.setFileSize(0L);
        assertEquals(0L, result.getFileSize());
    }
}
