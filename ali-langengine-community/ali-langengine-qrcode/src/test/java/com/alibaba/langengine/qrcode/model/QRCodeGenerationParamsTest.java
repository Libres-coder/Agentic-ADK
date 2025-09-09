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
import org.junit.jupiter.api.*;
import java.awt.Color;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QRCodeGenerationParamsTest {

    @Test
    @Order(1)
    void testConstructorWithDataAndFileName() {
        String testData = "Hello World";
        String testFileName = "test.png";
        
        QRCodeGenerationParams params = new QRCodeGenerationParams(testData, testFileName);
        
        assertEquals(testData, params.getData());
        assertEquals(testFileName, params.getFileName());
        assertEquals(300, params.getWidth());
        assertEquals(300, params.getHeight());
        assertEquals(Color.BLACK, params.getForegroundColor());
        assertEquals(Color.WHITE, params.getBackgroundColor());
        assertEquals(ErrorCorrectionLevel.H, params.getErrorCorrectionLevel());
        assertEquals("UTF-8", params.getCharset());
        assertEquals(1, params.getMargin());
        assertNull(params.getLogoPath());
        assertEquals(0.2, params.getLogoRatio(), 0.001);
        assertEquals("PNG", params.getFormat());
        assertNotNull(params.getCreatedAt());
    }

    @Test
    @Order(2)
    void testConstructorWithDataFileNameAndSize() {
        String testData = "Test Data";
        String testFileName = "test.jpg";
        int width = 400;
        int height = 500;
        
        QRCodeGenerationParams params = new QRCodeGenerationParams(testData, testFileName, width, height);
        
        assertEquals(testData, params.getData());
        assertEquals(testFileName, params.getFileName());
        assertEquals(width, params.getWidth());
        assertEquals(height, params.getHeight());
        assertNotNull(params.getCreatedAt());
    }

    @Test
    @Order(3)
    void testDefaultConstructor() {
        QRCodeGenerationParams params = new QRCodeGenerationParams();
        
        assertNull(params.getData());
        assertNull(params.getFileName());
        assertEquals(300, params.getWidth());
        assertEquals(300, params.getHeight());
        assertNotNull(params.getCreatedAt());
    }

    @Test
    @Order(4)
    void testSettersAndGetters() {
        QRCodeGenerationParams params = new QRCodeGenerationParams();
        
        // Test all setters and getters
        params.setData("Test Data");
        assertEquals("Test Data", params.getData());
        
        params.setFileName("test.png");
        assertEquals("test.png", params.getFileName());
        
        params.setWidth(500);
        assertEquals(500, params.getWidth());
        
        params.setHeight(600);
        assertEquals(600, params.getHeight());
        
        params.setForegroundColor(Color.RED);
        assertEquals(Color.RED, params.getForegroundColor());
        
        params.setBackgroundColor(Color.BLUE);
        assertEquals(Color.BLUE, params.getBackgroundColor());
        
        params.setErrorCorrectionLevel(ErrorCorrectionLevel.M);
        assertEquals(ErrorCorrectionLevel.M, params.getErrorCorrectionLevel());
        
        params.setCharset("ISO-8859-1");
        assertEquals("ISO-8859-1", params.getCharset());
        
        params.setMargin(5);
        assertEquals(5, params.getMargin());
        
        params.setLogoPath("/path/to/logo.png");
        assertEquals("/path/to/logo.png", params.getLogoPath());
        
        params.setLogoRatio(0.25);
        assertEquals(0.25, params.getLogoRatio(), 0.001);
        
        params.setOutputDirectory(Paths.get("/output"));
        assertEquals(Paths.get("/output"), params.getOutputDirectory());
        
        params.setFormat("JPG");
        assertEquals("JPG", params.getFormat());
    }

    @Test
    @Order(5)
    void testValidateWithValidParams() {
        QRCodeGenerationParams params = new QRCodeGenerationParams("Valid data", "test.png", 300, 300);
        
        // Should not throw exception
        assertDoesNotThrow(params::validate);
    }

    @Test
    @Order(6)
    void testValidateWithNullData() {
        QRCodeGenerationParams params = new QRCodeGenerationParams();
        params.setFileName("test.png");
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, params::validate);
        assertTrue(exception.getMessage().contains("data cannot be null or empty"));
    }

    @Test
    @Order(7)
    void testValidateWithEmptyData() {
        QRCodeGenerationParams params = new QRCodeGenerationParams("  ", "test.png");
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, params::validate);
        assertTrue(exception.getMessage().contains("data cannot be null or empty"));
    }

    @Test
    @Order(8)
    void testValidateWithNullFileName() {
        QRCodeGenerationParams params = new QRCodeGenerationParams();
        params.setData("Valid data");
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, params::validate);
        assertTrue(exception.getMessage().contains("File name cannot be null or empty"));
    }

    @Test
    @Order(9)
    void testValidateWithEmptyFileName() {
        QRCodeGenerationParams params = new QRCodeGenerationParams("Valid data", "   ");
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, params::validate);
        assertTrue(exception.getMessage().contains("File name cannot be null or empty"));
    }

    @Test
    @Order(10)
    void testValidateWithInvalidWidth() {
        QRCodeGenerationParams params = new QRCodeGenerationParams("Valid data", "test.png", 50, 300);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, params::validate);
        assertTrue(exception.getMessage().contains("Width must be between 100 and 2000"));
        
        params.setWidth(2500);
        exception = assertThrows(IllegalArgumentException.class, params::validate);
        assertTrue(exception.getMessage().contains("Width must be between 100 and 2000"));
    }

    @Test
    @Order(11)
    void testValidateWithInvalidHeight() {
        QRCodeGenerationParams params = new QRCodeGenerationParams("Valid data", "test.png", 300, 50);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, params::validate);
        assertTrue(exception.getMessage().contains("Height must be between 100 and 2000"));
        
        params.setHeight(2500);
        exception = assertThrows(IllegalArgumentException.class, params::validate);
        assertTrue(exception.getMessage().contains("Height must be between 100 and 2000"));
    }

    @Test
    @Order(12)
    void testValidateWithInvalidLogoRatio() {
        QRCodeGenerationParams params = new QRCodeGenerationParams("Valid data", "test.png");
        
        params.setLogoRatio(-0.1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, params::validate);
        assertTrue(exception.getMessage().contains("Logo ratio must be between 0 and 0.3"));
        
        params.setLogoRatio(0.5);
        exception = assertThrows(IllegalArgumentException.class, params::validate);
        assertTrue(exception.getMessage().contains("Logo ratio must be between 0 and 0.3"));
    }

    @Test
    @Order(13)
    void testHasLogo() {
        QRCodeGenerationParams params = new QRCodeGenerationParams();
        
        // Test without logo
        assertFalse(params.hasLogo());
        
        // Test with null logo path
        params.setLogoPath(null);
        assertFalse(params.hasLogo());
        
        // Test with empty logo path
        params.setLogoPath("   ");
        assertFalse(params.hasLogo());
        
        // Test with valid logo path
        params.setLogoPath("/path/to/logo.png");
        assertTrue(params.hasLogo());
    }

    @Test
    @Order(14)
    void testGetLogoSize() {
        QRCodeGenerationParams params = new QRCodeGenerationParams("data", "test.png", 400, 300);
        params.setLogoRatio(0.2);
        
        // Logo size should be based on the smaller dimension
        int expectedSize = (int) (300 * 0.2); // 60
        assertEquals(expectedSize, params.getLogoSize());
        
        // Test with different dimensions
        params.setWidth(200);
        params.setHeight(400);
        expectedSize = (int) (200 * 0.2); // 40
        assertEquals(expectedSize, params.getLogoSize());
    }

    @Test
    @Order(15)
    void testCopy() {
        QRCodeGenerationParams original = new QRCodeGenerationParams("Test Data", "test.png", 400, 500);
        original.setForegroundColor(Color.RED);
        original.setBackgroundColor(Color.BLUE);
        original.setErrorCorrectionLevel(ErrorCorrectionLevel.M);
        original.setCharset("ISO-8859-1");
        original.setMargin(3);
        original.setLogoPath("/path/to/logo.png");
        original.setLogoRatio(0.25);
        original.setOutputDirectory(Paths.get("/output"));
        original.setFormat("JPG");
        
        QRCodeGenerationParams copy = original.copy();
        
        // Verify all fields are copied
        assertEquals(original.getData(), copy.getData());
        assertEquals(original.getFileName(), copy.getFileName());
        assertEquals(original.getWidth(), copy.getWidth());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getForegroundColor(), copy.getForegroundColor());
        assertEquals(original.getBackgroundColor(), copy.getBackgroundColor());
        assertEquals(original.getErrorCorrectionLevel(), copy.getErrorCorrectionLevel());
        assertEquals(original.getCharset(), copy.getCharset());
        assertEquals(original.getMargin(), copy.getMargin());
        assertEquals(original.getLogoPath(), copy.getLogoPath());
        assertEquals(original.getLogoRatio(), copy.getLogoRatio(), 0.001);
        assertEquals(original.getOutputDirectory(), copy.getOutputDirectory());
        assertEquals(original.getFormat(), copy.getFormat());
        
        // Verify they are different objects
        assertNotSame(original, copy);
        
        // Verify timestamps (might be same due to fast execution)
        assertNotNull(copy.getCreatedAt());
    }

    @Test
    @Order(16)
    void testAllErrorCorrectionLevels() {
        QRCodeGenerationParams params = new QRCodeGenerationParams();
        
        params.setErrorCorrectionLevel(ErrorCorrectionLevel.L);
        assertEquals(ErrorCorrectionLevel.L, params.getErrorCorrectionLevel());
        
        params.setErrorCorrectionLevel(ErrorCorrectionLevel.M);
        assertEquals(ErrorCorrectionLevel.M, params.getErrorCorrectionLevel());
        
        params.setErrorCorrectionLevel(ErrorCorrectionLevel.Q);
        assertEquals(ErrorCorrectionLevel.Q, params.getErrorCorrectionLevel());
        
        params.setErrorCorrectionLevel(ErrorCorrectionLevel.H);
        assertEquals(ErrorCorrectionLevel.H, params.getErrorCorrectionLevel());
    }

    @Test
    @Order(17)
    void testColorHandling() {
        QRCodeGenerationParams params = new QRCodeGenerationParams();
        
        // Test RGB colors
        Color customColor = new Color(128, 64, 192);
        params.setForegroundColor(customColor);
        assertEquals(customColor, params.getForegroundColor());
        
        // Test predefined colors
        params.setBackgroundColor(Color.YELLOW);
        assertEquals(Color.YELLOW, params.getBackgroundColor());
        
        // Test null colors (should be allowed)
        params.setForegroundColor(null);
        assertNull(params.getForegroundColor());
    }

    @Test
    @Order(18)
    void testBoundaryValues() {
        QRCodeGenerationParams params = new QRCodeGenerationParams("data", "test.png");
        
        // Test minimum valid values
        params.setWidth(100);
        params.setHeight(100);
        params.setLogoRatio(0.0);
        assertDoesNotThrow(params::validate);
        
        // Test maximum valid values
        params.setWidth(2000);
        params.setHeight(2000);
        params.setLogoRatio(0.3);
        assertDoesNotThrow(params::validate);
    }
}
