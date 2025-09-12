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

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.alibaba.langengine.googletranslate.GoogleTranslateConfiguration.GOOGLE_TRANSLATE_API_KEY;
import static com.alibaba.langengine.googletranslate.GoogleTranslateConfiguration.GOOGLE_TRANSLATE_API_URL;
import static com.alibaba.langengine.googletranslate.sdk.GoogleTranslateConstant.*;
import static com.alibaba.langengine.googletranslate.sdk.GoogleTranslateConstant.DEFAULT_TIMEOUT;

/**
 * Google Translate API Client for Java
 * This client provides methods to interact with the Google Translate API.
 *
 * @author agentic-adk
 */
public class GoogleTranslateClient {

    private final String apiKey;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a GoogleTranslateClient with a specified API key.
     *
     * @param apiKey the API key for authentication with the Google Translate service
     */
    public GoogleTranslateClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a GoogleTranslateClient using the default API key from configuration.
     */
    public GoogleTranslateClient() {
        this.apiKey = GOOGLE_TRANSLATE_API_KEY;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a GoogleTranslateClient with a specified API key and custom OkHttpClient.
     *
     * @param apiKey the API key for authentication with the Google Translate service
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public GoogleTranslateClient(String apiKey, OkHttpClient okHttpClient) {
        this.apiKey = apiKey;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Translates text from source language to target language.
     *
     * @param request the translate request parameters
     * @return the translate response result
     * @throws GoogleTranslateException thrown when the API call fails
     */
    public TranslateResponse translate(TranslateRequest request) throws GoogleTranslateException {
        try {
            // Build the HTTP URL
            String url = GOOGLE_TRANSLATE_API_URL + TRANSLATE_ENDPOINT;

            // Build form body
            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("key", apiKey);

            // Add queries
            if (request.getQueries() != null) {
                for (String query : request.getQueries()) {
                    formBuilder.add("q", query);
                }
            }

            // Add other parameters
            if (request.getSource() != null) {
                formBuilder.add("source", request.getSource());
            }

            if (request.getTarget() != null) {
                formBuilder.add("target", request.getTarget());
            }

            if (request.getFormat() != null) {
                formBuilder.add("format", request.getFormat());
            }

            if (request.getModel() != null) {
                formBuilder.add("model", request.getModel());
            }

            // Create the HTTP request
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(formBuilder.build())
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            // Execute the request
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new GoogleTranslateException("API request failed: " + response.code() + " " + response.message());
                }

                ResponseBody body = response.body();
                if (body == null) {
                    throw new GoogleTranslateException("API returned empty response");
                }

                // Parse the response
                return objectMapper.readValue(body.string(), TranslateResponse.class);
            }
        } catch (IOException e) {
            throw new GoogleTranslateException("Error occurred during API call", e);
        }
    }

    /**
     * Simplified translate method using single text.
     *
     * @param text the text to translate
     * @param targetLanguage the target language code
     * @return the translate response result
     * @throws GoogleTranslateException thrown when the API call fails
     */
    public TranslateResponse translate(String text, String targetLanguage) throws GoogleTranslateException {
        TranslateRequest request = new TranslateRequest();
        request.setQueries(Collections.singletonList(text));
        request.setTarget(targetLanguage);
        return translate(request);
    }

    /**
     * Simplified translate method using single text with source and target languages.
     *
     * @param text the text to translate
     * @param sourceLanguage the source language code
     * @param targetLanguage the target language code
     * @return the translate response result
     * @throws GoogleTranslateException thrown when the API call fails
     */
    public TranslateResponse translate(String text, String sourceLanguage, String targetLanguage) throws GoogleTranslateException {
        TranslateRequest request = new TranslateRequest();
        request.setQueries(Collections.singletonList(text));
        request.setSource(sourceLanguage);
        request.setTarget(targetLanguage);
        return translate(request);
    }

    /**
     * Translates multiple texts from source language to target language.
     *
     * @param texts the list of texts to translate
     * @param targetLanguage the target language code
     * @return the translate response result
     * @throws GoogleTranslateException thrown when the API call fails
     */
    public TranslateResponse translate(List<String> texts, String targetLanguage) throws GoogleTranslateException {
        TranslateRequest request = new TranslateRequest();
        request.setQueries(texts);
        request.setTarget(targetLanguage);
        return translate(request);
    }

    /**
     * Detects the language of the provided text.
     *
     * @param request the detect language request parameters
     * @return the detect language response result
     * @throws GoogleTranslateException thrown when the API call fails
     */
    public DetectLanguageResponse detectLanguage(DetectLanguageRequest request) throws GoogleTranslateException {
        try {
            // Build the HTTP URL
            String url = GOOGLE_TRANSLATE_API_URL + DETECT_ENDPOINT;

            // Build form body
            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("key", apiKey);

            // Add queries
            if (request.getQueries() != null) {
                for (String query : request.getQueries()) {
                    formBuilder.add("q", query);
                }
            }

            // Create the HTTP request
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(formBuilder.build())
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            // Execute the request
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new GoogleTranslateException("API request failed: " + response.code() + " " + response.message());
                }

                ResponseBody body = response.body();
                if (body == null) {
                    throw new GoogleTranslateException("API returned empty response");
                }

                // Parse the response
                return objectMapper.readValue(body.string(), DetectLanguageResponse.class);
            }
        } catch (IOException e) {
            throw new GoogleTranslateException("Error occurred during API call", e);
        }
    }

    /**
     * Simplified language detection method using single text.
     *
     * @param text the text to detect language for
     * @return the detect language response result
     * @throws GoogleTranslateException thrown when the API call fails
     */
    public DetectLanguageResponse detectLanguage(String text) throws GoogleTranslateException {
        DetectLanguageRequest request = new DetectLanguageRequest();
        request.setQueries(Collections.singletonList(text));
        return detectLanguage(request);
    }

    /**
     * Detects languages for multiple texts.
     *
     * @param texts the list of texts to detect languages for
     * @return the detect language response result
     * @throws GoogleTranslateException thrown when the API call fails
     */
    public DetectLanguageResponse detectLanguage(List<String> texts) throws GoogleTranslateException {
        DetectLanguageRequest request = new DetectLanguageRequest();
        request.setQueries(texts);
        return detectLanguage(request);
    }

    /**
     * Gets the list of supported languages.
     *
     * @param targetLanguage optional target language for language names
     * @return the supported languages response result
     * @throws GoogleTranslateException thrown when the API call fails
     */
    public LanguagesResponse getSupportedLanguages(String targetLanguage) throws GoogleTranslateException {
        try {
            // Build the HTTP URL
            HttpUrl.Builder urlBuilder = HttpUrl.parse(GOOGLE_TRANSLATE_API_URL + LANGUAGES_ENDPOINT).newBuilder();
            urlBuilder.addQueryParameter("key", apiKey);
            
            if (targetLanguage != null) {
                urlBuilder.addQueryParameter("target", targetLanguage);
            }

            // Create the HTTP request
            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .get()
                    .build();

            // Execute the request
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new GoogleTranslateException("API request failed: " + response.code() + " " + response.message());
                }

                ResponseBody body = response.body();
                if (body == null) {
                    throw new GoogleTranslateException("API returned empty response");
                }

                // Parse the response
                return objectMapper.readValue(body.string(), LanguagesResponse.class);
            }
        } catch (IOException e) {
            throw new GoogleTranslateException("Error occurred during API call", e);
        }
    }

    /**
     * Gets the list of supported languages with default target language.
     *
     * @return the supported languages response result
     * @throws GoogleTranslateException thrown when the API call fails
     */
    public LanguagesResponse getSupportedLanguages() throws GoogleTranslateException {
        return getSupportedLanguages(null);
    }
}