package com.alibaba.langgengine.kagi.sdk;

import com.alibaba.langgengine.kagi.KagiConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Kagi API Client for Java.
 * This client provides methods to interact with the Kagi search API.
 */
public class KagiClient {

    private final String apiKey;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a KagiClient using the default API key from configuration.
     */
    public KagiClient() {
        this.apiKey = KagiConfiguration.KAGI_API_KEY;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(KagiConfiguration.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(KagiConfiguration.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(KagiConfiguration.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a KagiClient with a specified API key.
     *
     * @param apiKey the API key for authentication with the Kagi service
     */
    public KagiClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(KagiConfiguration.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(KagiConfiguration.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(KagiConfiguration.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Executes a search request to the Kagi API.
     *
     * @param request the search request parameters
     * @return the search response result
     * @throws KagiException thrown when the API call fails
     */
    public SearchResponse search(SearchRequest request) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new KagiException("API key is missing.");
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse(KagiConfiguration.KAGI_API_URL + KagiConfiguration.SEARCH_ENDPOINT)
                .newBuilder()
                .addQueryParameter("q", request.getQuery());

        if (request.getLimit() != null) {
            urlBuilder.addQueryParameter("limit", request.getLimit().toString());
        }

        Request httpRequest = new Request.Builder()
                .url(urlBuilder.build())
                .header("Authorization", "Bot " + apiKey)
                .get()
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new KagiException("API request failed: " + response.code() + " " + response.message());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new KagiException("API returned empty response");
            }

            return objectMapper.readValue(body.string(), SearchResponse.class);
        } catch (IOException e) {
            throw new KagiException("Error occurred during API call", e);
        }
    }

    /**
     * Simplified search method using default parameters.
     *
     * @param query the search query string
     * @return the search response result
     * @throws KagiException thrown when the API call fails
     */
    public SearchResponse search(String query) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        return search(request);
    }

}
