package com.alibaba.langengine.perplexity.sdk;

import com.alibaba.langengine.perplexity.PerplexityConfiguration;
import com.alibaba.langengine.perplexity.sdk.request.*;
import com.alibaba.langengine.perplexity.sdk.response.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.perplexity.PerplexityConfiguration.*;

/**
 * Perplexity API Client for Java
 * This client provides methods to interact with the Perplexity AI API.
 */
public class PerplexityClient {

    private final String apiKey;
    private final OkHttpClient syncClient;
    private final OkHttpClient deepResearchClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a PerplexityClient using the default API key from configuration.
     */
    public PerplexityClient() {
        this.apiKey = PERPLEXITY_API_KEY;
        this.syncClient = createHttpClient(SYNC_TIMEOUT);
        this.deepResearchClient = createHttpClient(DEEP_RESEARCH_TIMEOUT);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a PerplexityClient with a specified API key.
     *
     * @param apiKey the API key for authentication with the Perplexity service
     */
    public PerplexityClient(String apiKey) {
        this.apiKey = apiKey;
        this.syncClient = createHttpClient(SYNC_TIMEOUT);
        this.deepResearchClient = createHttpClient(DEEP_RESEARCH_TIMEOUT);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a PerplexityClient with custom HTTP clients.
     *
     * @param apiKey the API key for authentication
     * @param syncClient HTTP client for sync operations
     * @param deepResearchClient HTTP client for deep research operations
     */
    public PerplexityClient(String apiKey, OkHttpClient syncClient, OkHttpClient deepResearchClient) {
        this.apiKey = apiKey;
        this.syncClient = syncClient;
        this.deepResearchClient = deepResearchClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Creates an HTTP client with the specified timeout.
     *
     * @param timeoutSeconds timeout in seconds
     * @return configured OkHttpClient
     */
    private OkHttpClient createHttpClient(int timeoutSeconds) {
        return new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Executes a synchronous chat completion request.
     *
     * @param request the chat completion request
     * @return the chat completion response
     * @throws PerplexityException if the API call fails
     */
    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request) throws PerplexityException {
        validateApiKey();
        
        try {
            String jsonBody = objectMapper.writeValueAsString(request);
            
            Request httpRequest = new Request.Builder()
                    .url(PERPLEXITY_API_URL + SYNC_ENDPOINT)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .build();

            // Use deep research client for deep research model
            OkHttpClient clientToUse = isDeepResearchModel(request.getModel()) ? deepResearchClient : syncClient;
            
            try (Response response = clientToUse.newCall(httpRequest).execute()) {
                return handleResponse(response, ChatCompletionResponse.class);
            }
        } catch (IOException e) {
            throw new PerplexityException("Error occurred during sync chat completion API call", e);
        }
    }

    /**
     * Simplified synchronous chat completion with just a query string.
     *
     * @param query the search query
     * @return the chat completion response
     * @throws PerplexityException if the API call fails
     */
    public ChatCompletionResponse chatCompletion(String query) throws PerplexityException {
        return chatCompletion(query, PerplexityModelConstant.SONAR);
    }

    /**
     * Simplified synchronous chat completion with query and model.
     *
     * @param query the search query
     * @param model the model to use
     * @return the chat completion response
     * @throws PerplexityException if the API call fails
     */
    public ChatCompletionResponse chatCompletion(String query, String model) throws PerplexityException {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(model);
        
        Message userMessage = new Message(Message.Role.USER, query);
        request.setMessages(Arrays.asList(userMessage));
        
        return chatCompletion(request);
    }

    /**
     * Creates an asynchronous chat completion job.
     *
     * @param request the chat completion request
     * @return the async job response
     * @throws PerplexityException if the API call fails
     */
    public AsyncJobResponse createAsyncChatCompletion(ChatCompletionRequest request) throws PerplexityException {
        validateApiKey();
        
        try {
            AsyncChatCompletionRequest asyncRequest = new AsyncChatCompletionRequest(request);
            String jsonBody = objectMapper.writeValueAsString(asyncRequest);
            
            Request httpRequest = new Request.Builder()
                    .url(PERPLEXITY_API_URL + ASYNC_ENDPOINT)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .build();

            try (Response response = syncClient.newCall(httpRequest).execute()) {
                return handleResponse(response, AsyncJobResponse.class);
            }
        } catch (IOException e) {
            throw new PerplexityException("Error occurred during async chat completion creation", e);
        }
    }

    /**
     * Simplified async chat completion creation with query string.
     *
     * @param query the search query
     * @return the async job response
     * @throws PerplexityException if the API call fails
     */
    public AsyncJobResponse createAsyncChatCompletion(String query) throws PerplexityException {
        return createAsyncChatCompletion(query, PerplexityModelConstant.SONAR_DEEP_RESEARCH);
    }

    /**
     * Simplified async chat completion creation with query and model.
     *
     * @param query the search query
     * @param model the model to use
     * @return the async job response
     * @throws PerplexityException if the API call fails
     */
    public AsyncJobResponse createAsyncChatCompletion(String query, String model) throws PerplexityException {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(model);
        
        Message userMessage = new Message(Message.Role.USER, query);
        request.setMessages(Arrays.asList(userMessage));
        
        return createAsyncChatCompletion(request);
    }

    /**
     * Lists async chat completion jobs.
     *
     * @param limit maximum number of jobs to return (default: 20)
     * @param nextToken token for pagination
     * @return list of async jobs
     * @throws PerplexityException if the API call fails
     */
    public AsyncListResponse listAsyncChatCompletions(Integer limit, String nextToken) throws PerplexityException {
        validateApiKey();
        
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(PERPLEXITY_API_URL + ASYNC_LIST_ENDPOINT).newBuilder();
            
            if (limit != null) {
                urlBuilder.addQueryParameter("limit", limit.toString());
            }
            if (nextToken != null && !nextToken.trim().isEmpty()) {
                urlBuilder.addQueryParameter("next_token", nextToken);
            }
            
            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .get()
                    .header("Authorization", "Bearer " + apiKey)
                    .build();

            try (Response response = syncClient.newCall(httpRequest).execute()) {
                return handleResponse(response, AsyncListResponse.class);
            }
        } catch (IOException e) {
            throw new PerplexityException("Error occurred during async job listing", e);
        }
    }

    /**
     * Lists async chat completion jobs with default parameters.
     *
     * @return list of async jobs
     * @throws PerplexityException if the API call fails
     */
    public AsyncListResponse listAsyncChatCompletions() throws PerplexityException {
        return listAsyncChatCompletions(null, null);
    }

    /**
     * Gets the result of a specific async chat completion job.
     *
     * @param requestId the ID of the async job
     * @return the async job response with results
     * @throws PerplexityException if the API call fails
     */
    public AsyncJobResponse getAsyncChatCompletion(String requestId) throws PerplexityException {
        validateApiKey();
        
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new PerplexityException("Request ID cannot be null or empty");
        }
        
        try {
            String url = PERPLEXITY_API_URL + PerplexityConstant.ASYNC_SEARCH_RESULT_ENDPOINT.replace("{request_id}", requestId);
            
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .get()
                    .header("Authorization", "Bearer " + apiKey)
                    .build();

            try (Response response = syncClient.newCall(httpRequest).execute()) {
                return handleResponse(response, AsyncJobResponse.class);
            }
        } catch (IOException e) {
            throw new PerplexityException("Error occurred during async job retrieval", e);
        }
    }

    /**
     * Handles HTTP response and converts to specified type.
     *
     * @param response the HTTP response
     * @param responseClass the class to convert the response to
     * @param <T> response type
     * @return parsed response object
     * @throws PerplexityException if response handling fails
     */
    private <T> T handleResponse(Response response, Class<T> responseClass) throws PerplexityException {
        try {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new PerplexityException(
                    "API request failed: " + response.code() + " " + response.message(),
                    response.code(),
                    errorBody
                );
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new PerplexityException("API returned empty response");
            }

            return objectMapper.readValue(body.string(), responseClass);
        } catch (IOException e) {
            throw new PerplexityException("Failed to parse API response", e);
        }
    }

    /**
     * Validates that API key is present.
     *
     * @throws PerplexityException if API key is missing
     */
    private void validateApiKey() throws PerplexityException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new PerplexityException("API key is missing. Please set PERPLEXITY_API_KEY configuration.");
        }
    }

    /**
     * Checks if the model is a deep research model that requires longer timeout.
     *
     * @param model the model name
     * @return true if it's a deep research model
     */
    private boolean isDeepResearchModel(String model) {
        return PerplexityModelConstant.SONAR_DEEP_RESEARCH.equals(model);
    }
}