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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Represents the response from the Google Translate API.
 */
public class TranslateResponse {
    @JsonProperty("data")
    private TranslationData data;

    // Getters and Setters

    public TranslationData getData() {
        return data;
    }

    public void setData(TranslationData data) {
        this.data = data;
    }

    /**
     * Represents translation data in the response.
     */
    @Data
    public static class TranslationData {
        @JsonProperty("translations")
        private List<Translation> translations;
    }

    /**
     * Represents a single translation result.
     */
    @Data
    public static class Translation {
        @JsonProperty("translatedText")
        private String translatedText;

        @JsonProperty("detectedSourceLanguage")
        private String detectedSourceLanguage;

        @JsonProperty("model")
        private String model;
    }
}