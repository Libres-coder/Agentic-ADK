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
package com.alibaba.langengine.brave.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.brave.BraveConfiguration.BRAVE_API_KEY;
import static com.alibaba.langengine.brave.BraveConfiguration.BRAVE_API_URL;
import static com.alibaba.langengine.brave.sdk.BraveConstant.*;


/**
 * Brave Search API Client for Java
 * This client provides methods to interact with the Brave Search API.
 *
 * @author agentic-adk
 */
public class BraveClient {

    private final String apiKey;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a BraveClient with a specified API key.
     *
     * @param apiKey the API key for authentication with the Brave Search service
     */
    public BraveClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a BraveClient using the default API key from configuration.
     */
    public BraveClient() {
        this.apiKey = BRAVE_API_KEY;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a BraveClient with a specified API key and custom OkHttpClient.
     *
     * @param apiKey the API key for authentication with the Brave Search service
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public BraveClient(String apiKey, OkHttpClient okHttpClient) {
        this.apiKey = apiKey;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Executes a web search request to the Brave Search API.
     *
     * @param request the search request parameters
     * @return the search response result
     * @throws BraveException thrown when the API call fails
     */
    public SearchResponse search(SearchRequest request) throws BraveException {
        try {
            // Build the HTTP URL with query parameters
            HttpUrl.Builder urlBuilder = HttpUrl.parse(BRAVE_API_URL + WEB_SEARCH_ENDPOINT).newBuilder();
            
            if (request.getQuery() != null) {
                urlBuilder.addQueryParameter("q", request.getQuery());
            }
            
            if (request.getCount() != null) {
                urlBuilder.addQueryParameter("count", request.getCount().toString());
            }
            
            if (request.getOffset() != null) {
                urlBuilder.addQueryParameter("offset", request.getOffset().toString());
            }
            
            if (request.getSafesearch() != null) {
                urlBuilder.addQueryParameter("safesearch", request.getSafesearch());
            }
            
            if (request.getCountry() != null) {
                urlBuilder.addQueryParameter("country", request.getCountry());
            }
            
            if (request.getSearchLang() != null) {
                urlBuilder.addQueryParameter("search_lang", request.getSearchLang());
            }
            
            if (request.getUiLang() != null) {
                urlBuilder.addQueryParameter("ui_lang", request.getUiLang());
            }
            
            if (request.getSpellcheck() != null) {
                urlBuilder.addQueryParameter("spellcheck", request.getSpellcheck().toString());
            }
            
            if (request.getResultFilter() != null) {
                urlBuilder.addQueryParameter("result_filter", request.getResultFilter());
            }

            // Create the HTTP request
            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/json")
                    .addHeader("X-Subscription-Token", apiKey)
                    .get()
                    .build();

            // Execute the request
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new BraveException("API request failed: " + response.code() + " " + response.message());
                }

                ResponseBody body = response.body();
                if (body == null) {
                    throw new BraveException("API returned empty response");
                }

                // Parse the response
                return objectMapper.readValue(body.string(), SearchResponse.class);
            }
        } catch (IOException e) {
            throw new BraveException("Error occurred during API call", e);
        }
    }

    /**
     * Simplified search method using query string.
     *
     * @param query the search query string
     * @return the search response result
     * @throws BraveException thrown when the API call fails
     */
    public SearchResponse search(String query) throws BraveException {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        return search(request);
    }

    /**
     * Simplified search method with query string and result count.
     *
     * @param query the search query string
     * @param count the maximum number of results to return
     * @return the search response result
     * @throws BraveException thrown when the API call fails
     */
    public SearchResponse search(String query, int count) throws BraveException {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setCount(count);
        return search(request);
    }
}