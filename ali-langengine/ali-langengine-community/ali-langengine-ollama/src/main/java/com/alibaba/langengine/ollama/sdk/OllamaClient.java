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
package com.alibaba.langengine.ollama.sdk;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static com.alibaba.langengine.ollama.OllamaConfiguration.*;

/**
 * Ollama API Client for Java
 * This client provides methods to interact with the Ollama API.
 *
 * @author disaster
 */
@Slf4j
@Data
public class OllamaClient {
    
    private final OkHttpClient client;
    
    private final ObjectMapper objectMapper;
    
    private final String baseUrl;
    
    /**
     * Constructs a OllamaClient with a specified base URL.
     * 
     * @param baseUrl the base URL for the Ollama service
     */
    public OllamaClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
        
        // Create OkHttpClient with configuration
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(OLLAMA_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(OLLAMA_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(OLLAMA_WRITE_TIMEOUT, TimeUnit.MILLISECONDS);
        
        // Add logging interceptor if needed
        if (log.isDebugEnabled()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(log::debug);
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }
        
        this.client = builder.build();
    }
    
    /**
     * Constructs a OllamaClient using the default base URL from configuration.
     */
    public OllamaClient() {
        this(OLLAMA_API_URL);
    }
    
    /**
     * Constructs a OllamaClient with a specified base URL and custom OkHttpClient.
     * 
     * @param baseUrl the base URL for the Ollama service
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public OllamaClient(String baseUrl, OkHttpClient okHttpClient) {
        this.baseUrl = baseUrl;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Generate a response for a given prompt
     *
     * @param request Generate request
     * @return Generate response
     * @throws OllamaException if the request fails
     */
    public GenerateResponse generate(GenerateRequest request) throws OllamaException {
        try {
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_GENERATE)
                    .post(body)
                    .build();
            
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OllamaException("Request failed with code: " + response.code());
                }
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new OllamaException("Empty response body");
                }
                
                return objectMapper.readValue(responseBody.string(), GenerateResponse.class);
            }
        } catch (IOException e) {
            throw new OllamaException("Failed to execute generate request", e);
        }
    }
    
    /**
     * Generate a response for a given prompt with streaming
     *
     * @param request  Generate request
     * @param callback Callback to handle streaming responses
     * @throws OllamaException if the request fails
     */
    public void generateStream(GenerateRequest request, OllamaStreamCallback<GenerateResponse> callback) throws OllamaException {
        try {
            // Ensure streaming is enabled
            request.setStream(true);
            
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_GENERATE)
                    .post(body)
                    .build();
            
            client.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e);
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError(new OllamaException("Request failed with code: " + response.code()));
                        return;
                    }
                    
                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        callback.onError(new OllamaException("Empty response body"));
                        return;
                    }
                    
                    try {
                        String line;
                        while ((line = responseBody.source().readUtf8Line()) != null) {
                            try {
                                GenerateResponse generateResponse = objectMapper.readValue(line, GenerateResponse.class);
                                callback.onPartialResponse(generateResponse);
                                
                                if (Boolean.TRUE.equals(generateResponse.getDone())) {
                                    break;
                                }
                            } catch (Exception e) {
                                log.warn("Failed to parse partial response: {}", line, e);
                            }
                        }
                        callback.onComplete();
                    } catch (Exception e) {
                        callback.onError(e);
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (Exception e) {
            throw new OllamaException("Failed to execute generate stream request", e);
        }
    }
    
    /**
     * Chat with the model
     *
     * @param request Chat request
     * @return Chat response
     * @throws OllamaException if the request fails
     */
    public ChatResponse chat(ChatRequest request) throws OllamaException {
        try {
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_CHAT)
                    .post(body)
                    .build();
            
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OllamaException("Request failed with code: " + response.code());
                }
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new OllamaException("Empty response body");
                }
                
                return objectMapper.readValue(responseBody.string(), ChatResponse.class);
            }
        } catch (IOException e) {
            throw new OllamaException("Failed to execute chat request", e);
        }
    }
    
    /**
     * Chat with the model with streaming
     *
     * @param request  Chat request
     * @param callback Callback to handle streaming responses
     * @throws OllamaException if the request fails
     */
    public void chatStream(ChatRequest request, OllamaStreamCallback<ChatResponse> callback) throws OllamaException {
        try {
            // Ensure streaming is enabled
            request.setStream(true);
            
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_CHAT)
                    .post(body)
                    .build();
            
            client.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e);
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError(new OllamaException("Request failed with code: " + response.code()));
                        return;
                    }
                    
                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        callback.onError(new OllamaException("Empty response body"));
                        return;
                    }
                    
                    try {
                        String line;
                        while ((line = responseBody.source().readUtf8Line()) != null) {
                            try {
                                ChatResponse chatResponse = objectMapper.readValue(line, ChatResponse.class);
                                callback.onPartialResponse(chatResponse);
                                
                                if (Boolean.TRUE.equals(chatResponse.getDone())) {
                                    break;
                                }
                            } catch (Exception e) {
                                log.warn("Failed to parse partial response: {}", line, e);
                            }
                        }
                        callback.onComplete();
                    } catch (Exception e) {
                        callback.onError(e);
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (Exception e) {
            throw new OllamaException("Failed to execute chat stream request", e);
        }
    }
    
    /**
     * List local models
     *
     * @return Models response
     * @throws OllamaException if the request fails
     */
    public ModelsResponse listModels() throws OllamaException {
        try {
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_TAGS)
                    .get()
                    .build();
            
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OllamaException("Request failed with code: " + response.code());
                }
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new OllamaException("Empty response body");
                }
                
                return objectMapper.readValue(responseBody.string(), ModelsResponse.class);
            }
        } catch (IOException e) {
            throw new OllamaException("Failed to execute list models request", e);
        }
    }
    
    /**
     * Pull a model from the library
     *
     * @param request Pull request
     * @throws OllamaException if the request fails
     */
    public void pullModel(PullRequest request) throws OllamaException {
        try {
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_PULL)
                    .post(body)
                    .build();
            
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OllamaException("Request failed with code: " + response.code());
                }
            }
        } catch (IOException e) {
            throw new OllamaException("Failed to execute pull model request", e);
        }
    }
    
    /**
     * Pull a model from the library with streaming progress
     *
     * @param request  Pull request
     * @param callback Callback to handle streaming responses
     * @throws OllamaException if the request fails
     */
    public void pullModelStream(PullRequest request, OllamaStreamCallback<String> callback) throws OllamaException {
        try {
            // Ensure streaming is enabled
            request.setStream(true);
            
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_PULL)
                    .post(body)
                    .build();
            
            client.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e);
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError(new OllamaException("Request failed with code: " + response.code()));
                        return;
                    }
                    
                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        callback.onError(new OllamaException("Empty response body"));
                        return;
                    }
                    
                    try {
                        String line;
                        while ((line = responseBody.source().readUtf8Line()) != null) {
                            callback.onPartialResponse(line);
                        }
                        callback.onComplete();
                    } catch (Exception e) {
                        callback.onError(e);
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (Exception e) {
            throw new OllamaException("Failed to execute pull model stream request", e);
        }
    }
    
    /**
     * Delete a model
     *
     * @param request Delete request
     * @throws OllamaException if the request fails
     */
    public void deleteModel(DeleteRequest request) throws OllamaException {
        try {
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_DELETE)
                    .delete(body)
                    .build();
            
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OllamaException("Request failed with code: " + response.code());
                }
            }
        } catch (IOException e) {
            throw new OllamaException("Failed to execute delete model request", e);
        }
    }
    
    /**
     * Show model information
     *
     * @param request Show request
     * @return Show response
     * @throws OllamaException if the request fails
     */
    public ShowResponse showModel(ShowRequest request) throws OllamaException {
        try {
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_SHOW)
                    .post(body)
                    .build();
            
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OllamaException("Request failed with code: " + response.code());
                }
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new OllamaException("Empty response body");
                }
                
                return objectMapper.readValue(responseBody.string(), ShowResponse.class);
            }
        } catch (IOException e) {
            throw new OllamaException("Failed to execute show model request", e);
        }
    }
    
    /**
     * Copy a model
     *
     * @param request Copy request
     * @throws OllamaException if the request fails
     */
    public void copyModel(CopyRequest request) throws OllamaException {
        try {
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_COPY)
                    .post(body)
                    .build();
            
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OllamaException("Request failed with code: " + response.code());
                }
            }
        } catch (IOException e) {
            throw new OllamaException("Failed to execute copy model request", e);
        }
    }
    
    /**
     * Create a model
     *
     * @param request Create request
     * @throws OllamaException if the request fails
     */
    public void createModel(CreateRequest request) throws OllamaException {
        try {
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_CREATE)
                    .post(body)
                    .build();
            
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OllamaException("Request failed with code: " + response.code());
                }
            }
        } catch (IOException e) {
            throw new OllamaException("Failed to execute create model request", e);
        }
    }
    
    /**
     * Create a model with streaming progress
     *
     * @param request  Create request
     * @param callback Callback to handle streaming responses
     * @throws OllamaException if the request fails
     */
    public void createModelStream(CreateRequest request, OllamaStreamCallback<String> callback) throws OllamaException {
        try {
            // Ensure streaming is enabled
            request.setStream(true);
            
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_CREATE)
                    .post(body)
                    .build();
            
            client.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e);
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError(new OllamaException("Request failed with code: " + response.code()));
                        return;
                    }
                    
                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        callback.onError(new OllamaException("Empty response body"));
                        return;
                    }
                    
                    try {
                        String line;
                        while ((line = responseBody.source().readUtf8Line()) != null) {
                            callback.onPartialResponse(line);
                        }
                        callback.onComplete();
                    } catch (Exception e) {
                        callback.onError(e);
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (Exception e) {
            throw new OllamaException("Failed to execute create model stream request", e);
        }
    }
    
    /**
     * Get version of Ollama
     *
     * @return Version string
     * @throws OllamaException if the request fails
     */
    public String getVersion() throws OllamaException {
        try {
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + OllamaConstant.API_VERSION)
                    .get()
                    .build();
            
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OllamaException("Request failed with code: " + response.code());
                }
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new OllamaException("Empty response body");
                }
                
                return responseBody.string();
            }
        } catch (IOException e) {
            throw new OllamaException("Failed to execute get version request", e);
        }
    }
}