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
package com.alibaba.langengine.metaso.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.metaso.MetaSoConfiguration.*;

/**
 * MetaSo API Client for Java
 * This client provides methods to interact with the MetaSo search API.
 *
 * @author disaster
 */
@Slf4j
@Data
public class MetaSoClient {
    
    private final OkHttpClient client;
    
    private final ObjectMapper objectMapper;
    
    private final String baseUrl;
    
    private final String apiKey;
    
    /**
     * Constructs a MetaSoClient with a specified base URL and API key.
     * 
     * @param baseUrl the base URL for the MetaSo service
     * @param apiKey the API key for authentication with the MetaSo service
     */
    public MetaSoClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        
        // Create OkHttpClient with configuration
        this.client = new OkHttpClient.Builder()
                .connectTimeout(METASO_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(METASO_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(METASO_WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }
    
    /**
     * Constructs a MetaSoClient using the default base URL and API key from configuration.
     */
    public MetaSoClient() {
        this(METASO_API_URL, METASO_API_KEY);
    }
    
    /**
     * Constructs a MetaSoClient with a specified base URL, API key and custom OkHttpClient.
     * 
     * @param baseUrl the base URL for the MetaSo service
     * @param apiKey the API key for authentication with the MetaSo service
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public MetaSoClient(String baseUrl, String apiKey, OkHttpClient okHttpClient) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Executes a search request to the MetaSo API.
     * 
     * @param request the search request parameters
     * @return the search response result
     * @throws MetaSoException thrown when the API call fails
     */
    public SearchResponse search(SearchRequest request) throws MetaSoException {
        try {
            // Convert the request object to JSON
            String jsonBody = objectMapper.writeValueAsString(request);
            
            // Create the HTTP request
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + MetaSoConstant.API_SEARCH)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            // Execute the request
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new MetaSoException("API request failed: " + response.code() + " " + response.message());
                }
                
                ResponseBody body = response.body();
                if (body == null) {
                    throw new MetaSoException("API returned empty response");
                }
                
                // Parse the response
                return objectMapper.readValue(body.string(), SearchResponse.class);
            }
        } catch (IOException e) {
            throw new MetaSoException("Error occurred during API call", e);
        }
    }
    
    /**
     * Simplified search method using default parameters.
     * 
     * @param query the search query string
     * @return the search response result
     * @throws MetaSoException thrown when the API call fails
     */
    public SearchResponse search(String query) throws MetaSoException {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        return search(request);
    }
    
    /**
     * Executes a QA request to the MetaSo API.
     * 
     * @param request the QA request parameters
     * @return the QA response result
     * @throws MetaSoException thrown when the API call fails
     */
    public QAResponse qa(QARequest request) throws MetaSoException {
        try {
            // Convert the request object to JSON
            String jsonBody = objectMapper.writeValueAsString(request);
            
            // Create the HTTP request
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + MetaSoConstant.API_QA)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            // Execute the request
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new MetaSoException("API request failed: " + response.code() + " " + response.message());
                }
                
                ResponseBody body = response.body();
                if (body == null) {
                    throw new MetaSoException("API returned empty response");
                }
                
                // Parse the response
                return objectMapper.readValue(body.string(), QAResponse.class);
            }
        } catch (IOException e) {
            throw new MetaSoException("Error occurred during API call", e);
        }
    }
    
    /**
     * Simplified QA method using default parameters.
     * 
     * @param question the question string
     * @return the QA response result
     * @throws MetaSoException thrown when the API call fails
     */
    public QAResponse qa(String question) throws MetaSoException {
        QARequest request = new QARequest();
        request.setQuestion(question);
        return qa(request);
    }
}