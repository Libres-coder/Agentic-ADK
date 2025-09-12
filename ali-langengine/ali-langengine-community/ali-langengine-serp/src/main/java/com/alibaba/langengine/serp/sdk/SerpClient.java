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
package com.alibaba.langengine.serp.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.serp.SerpConfiguration.SERP_API_KEY;
import static com.alibaba.langengine.serp.SerpConfiguration.SERP_API_URL;
import static com.alibaba.langengine.serp.sdk.SerpConstant.*;

/**
 * SerpAPI Client for Java
 * This client provides methods to interact with the SerpAPI search API.
 *
 * @author disaster
 */
public class SerpClient {

    private final String apiKey;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a SerpClient with a specified API key.
     *
     * @param apiKey the API key for authentication with the SerpAPI service
     */
    public SerpClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a SerpClient using the default API key from configuration.
     */
    public SerpClient() {
        this.apiKey = SERP_API_KEY;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a SerpClient with a specified API key and custom OkHttpClient.
     *
     * @param apiKey the API key for authentication with the SerpAPI service
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public SerpClient(String apiKey, OkHttpClient okHttpClient) {
        this.apiKey = apiKey;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Executes a search request to the SerpAPI.
     *
     * @param request the search request parameters
     * @return the search response result
     * @throws SerpException thrown when the API call fails
     */
    public SearchResponse search(SearchRequest request) throws SerpException {
        try {
            // Set the API key in the request
            request.setApiKey(apiKey);

            // Convert the request object to JSON
            String jsonBody = objectMapper.writeValueAsString(request);

            // Create the HTTP request
            Request httpRequest = new Request.Builder()
                    .url(SERP_API_URL + SEARCH_ENDPOINT)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            // Execute the request
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new SerpException("API request failed: " + response.code() + " " + response.message());
                }

                ResponseBody body = response.body();
                if (body == null) {
                    throw new SerpException("API returned empty response");
                }

                // Parse the response
                return objectMapper.readValue(body.string(), SearchResponse.class);
            }
        } catch (IOException e) {
            throw new SerpException("Error occurred during API call", e);
        }
    }

    /**
     * Simplified search method using default parameters.
     *
     * @param query the search query string
     * @return the search response result
     * @throws SerpException thrown when the API call fails
     */
    public SearchResponse search(String query) throws SerpException {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        return search(request);
    }
}