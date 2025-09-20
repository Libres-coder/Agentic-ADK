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

import java.util.List;

/**
 * Represents the supported languages response from the Google Translate API.
 */
public class LanguagesResponse {
    @JsonProperty("data")
    private LanguagesData data;

    // Getters and Setters

    public LanguagesData getData() {
        return data;
    }

    public void setData(LanguagesData data) {
        this.data = data;
    }

    /**
     * Represents languages data in the response.
     */
    public static class LanguagesData {
        @JsonProperty("languages")
        private List<Language> languages;

        // Getters and Setters

        public List<Language> getLanguages() {
            return languages;
        }

        public void setLanguages(List<Language> languages) {
            this.languages = languages;
        }
    }

    /**
     * Represents a single supported language.
     */
    public static class Language {
        @JsonProperty("language")
        private String languageCode;

        @JsonProperty("name")
        private String name;

        // Getters and Setters

        public String getLanguageCode() {
            return languageCode;
        }

        public void setLanguageCode(String languageCode) {
            this.languageCode = languageCode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}