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
 * Represents the language detection response from the Google Translate API.
 */
public class DetectLanguageResponse {
    @JsonProperty("data")
    private DetectionData data;

    // Getters and Setters

    public DetectionData getData() {
        return data;
    }

    public void setData(DetectionData data) {
        this.data = data;
    }

    /**
     * Represents detection data in the response.
     */
    public static class DetectionData {
        @JsonProperty("detections")
        private List<List<Detection>> detections;

        // Getters and Setters

        public List<List<Detection>> getDetections() {
            return detections;
        }

        public void setDetections(List<List<Detection>> detections) {
            this.detections = detections;
        }
    }

    /**
     * Represents a single language detection result.
     */
    public static class Detection {
        @JsonProperty("language")
        private String language;

        @JsonProperty("confidence")
        private double confidence;

        @JsonProperty("isReliable")
        private boolean isReliable;

        // Getters and Setters

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public boolean isReliable() {
            return isReliable;
        }

        public void setReliable(boolean reliable) {
            isReliable = reliable;
        }
    }
}