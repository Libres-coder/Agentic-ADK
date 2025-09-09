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
package com.alibaba.langengine.googletranslate.sdk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the GoogleTranslateClient class.
 */
@EnabledIfEnvironmentVariable(named = "GOOGLE_TRANSLATE_API_KEY", matches = ".*")
class GoogleTranslateClientTest {

    @Test
    void testGoogleTranslateClientConstruction() {
        // Test default constructor
        assertDoesNotThrow(() -> new GoogleTranslateClient());
        
        // Test constructor with API key
        assertDoesNotThrow(() -> new GoogleTranslateClient("test-api-key"));
    }

    @Test
    void testTranslateSingleText() {
        GoogleTranslateClient client = new GoogleTranslateClient();
        // This test will only run if GOOGLE_TRANSLATE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            TranslateResponse response = client.translate("Hello, world!", "es");
            assertNotNull(response);
            assertNotNull(response.getData());
            assertNotNull(response.getData().getTranslations());
            assertFalse(response.getData().getTranslations().isEmpty());
        });
    }

    @Test
    void testTranslateWithSourceAndTargetLanguages() {
        GoogleTranslateClient client = new GoogleTranslateClient();
        // This test will only run if GOOGLE_TRANSLATE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            TranslateResponse response = client.translate("Hello, world!", "en", "fr");
            assertNotNull(response);
            assertNotNull(response.getData());
            assertNotNull(response.getData().getTranslations());
            assertFalse(response.getData().getTranslations().isEmpty());
        });
    }

    @Test
    void testTranslateMultipleTexts() {
        GoogleTranslateClient client = new GoogleTranslateClient();
        // This test will only run if GOOGLE_TRANSLATE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            TranslateResponse response = client.translate(Arrays.asList("Hello, world!", "Goodbye!"), "de");
            assertNotNull(response);
            assertNotNull(response.getData());
            assertNotNull(response.getData().getTranslations());
            assertEquals(2, response.getData().getTranslations().size());
        });
    }

    @Test
    void testDetectLanguage() {
        GoogleTranslateClient client = new GoogleTranslateClient();
        // This test will only run if GOOGLE_TRANSLATE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            DetectLanguageResponse response = client.detectLanguage("Hello, world!");
            assertNotNull(response);
            assertNotNull(response.getData());
            assertNotNull(response.getData().getDetections());
            assertFalse(response.getData().getDetections().isEmpty());
        });
    }

    @Test
    void testDetectMultipleLanguages() {
        GoogleTranslateClient client = new GoogleTranslateClient();
        // This test will only run if GOOGLE_TRANSLATE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            DetectLanguageResponse response = client.detectLanguage(Arrays.asList("Hello, world!", "Bonjour le monde!"));
            assertNotNull(response);
            assertNotNull(response.getData());
            assertNotNull(response.getData().getDetections());
            assertEquals(2, response.getData().getDetections().size());
        });
    }

    @Test
    void testGetSupportedLanguages() {
        GoogleTranslateClient client = new GoogleTranslateClient();
        // This test will only run if GOOGLE_TRANSLATE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            LanguagesResponse response = client.getSupportedLanguages();
            assertNotNull(response);
            assertNotNull(response.getData());
            assertNotNull(response.getData().getLanguages());
            assertFalse(response.getData().getLanguages().isEmpty());
        });
    }

    @Test
    void testGetSupportedLanguagesWithTarget() {
        GoogleTranslateClient client = new GoogleTranslateClient();
        // This test will only run if GOOGLE_TRANSLATE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            LanguagesResponse response = client.getSupportedLanguages("es");
            assertNotNull(response);
            assertNotNull(response.getData());
            assertNotNull(response.getData().getLanguages());
            assertFalse(response.getData().getLanguages().isEmpty());
        });
    }

    @Test
    void testTranslateWithRequestObject() {
        GoogleTranslateClient client = new GoogleTranslateClient();
        TranslateRequest request = new TranslateRequest();
        request.setQueries(Collections.singletonList("Hello, world!"));
        request.setTarget("ja");
        request.setSource("en");
        
        // This test will only run if GOOGLE_TRANSLATE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            TranslateResponse response = client.translate(request);
            assertNotNull(response);
            assertNotNull(response.getData());
            assertNotNull(response.getData().getTranslations());
            assertFalse(response.getData().getTranslations().isEmpty());
        });
    }

    @Test
    void testDetectLanguageWithRequestObject() {
        GoogleTranslateClient client = new GoogleTranslateClient();
        DetectLanguageRequest request = new DetectLanguageRequest();
        request.setQueries(Arrays.asList("Hello, world!", "Bonjour le monde!"));
        
        // This test will only run if GOOGLE_TRANSLATE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            DetectLanguageResponse response = client.detectLanguage(request);
            assertNotNull(response);
            assertNotNull(response.getData());
            assertNotNull(response.getData().getDetections());
            assertFalse(response.getData().getDetections().isEmpty());
        });
    }
}