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
package com.alibaba.langengine.wenxin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.alibaba.langengine.wenxin.model.completion.WenxinCompletionRequest;
import com.alibaba.langengine.wenxin.model.completion.WenxinCompletionResult;
import com.alibaba.langengine.wenxin.model.embedding.WenxinEmbeddingRequest;
import com.alibaba.langengine.wenxin.model.embedding.WenxinEmbeddingResult;
import com.alibaba.langengine.wenxin.model.service.WenxinAuthService;
import com.alibaba.langengine.wenxin.exception.WenxinApiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Slf4j
@Data
public class WenxinService {

    private String apiKey;
    private String secretKey;
    private String serverUrl;
    private Duration timeout;
    private WenxinAuthService authService;
    private OkHttpClient httpClient;
    private ObjectMapper objectMapper;

    public WenxinService(String serverUrl, Duration timeout, String apiKey, String secretKey) {
        this.serverUrl = serverUrl;
        this.timeout = timeout;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.authService = new WenxinAuthService(serverUrl, timeout);
        this.objectMapper = new ObjectMapper();

        this.httpClient = new OkHttpClient.Builder()
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    public WenxinCompletionResult createCompletion(WenxinCompletionRequest request) {
        try {
            String accessToken = authService.getAccessToken(apiKey, secretKey);
            String url = serverUrl + "rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-4.0-8k?access_token=" + accessToken;
            
            String jsonBody = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), 
                jsonBody
            );
            
            Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
            
            Response response = httpClient.newCall(httpRequest).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, WenxinCompletionResult.class);
            } else {
                String errorMessage = String.format("文心一言嵌入API调用失败 - HTTP %d: %s", 
                    response.code(), response.message());
                WenxinApiException.WenxinErrorType errorType = 
                    WenxinApiException.getErrorTypeByHttpStatus(response.code());
                
                log.error("文心一言嵌入API调用失败: {}", errorMessage);
                throw new WenxinApiException(errorMessage, 
                    String.valueOf(response.code()), 
                    response.code(), 
                    errorType);
            }
        } catch (IOException e) {
            log.error("文心一言嵌入API网络调用异常", e);
            throw new WenxinApiException("文心一言嵌入API网络调用异常: " + e.getMessage(), 
                e.getClass().getSimpleName(), 
                0, 
                WenxinApiException.WenxinErrorType.NETWORK_ERROR, 
                e);
        } catch (Exception e) {
            log.error("文心一言嵌入API调用发生未知异常", e);
            throw new WenxinApiException("文心一言嵌入API调用发生未知异常: " + e.getMessage(), 
                e.getClass().getSimpleName(), 
                0, 
                WenxinApiException.WenxinErrorType.UNKNOWN_ERROR, 
                e);
        }
    }

    public WenxinEmbeddingResult createEmbedding(WenxinEmbeddingRequest request) {
        try {
            String accessToken = authService.getAccessToken(apiKey, secretKey);
            String url = serverUrl + "rpc/2.0/ai_custom/v1/wenxinworkshop/embeddings/embedding-v1?access_token=" + accessToken;
            
            String jsonBody = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), 
                jsonBody
            );
            
            Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
            
            Response response = httpClient.newCall(httpRequest).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, WenxinEmbeddingResult.class);
            } else {
                String errorMessage = String.format("文心Embedding API调用失败 - HTTP %d: %s", 
                    response.code(), response.message());
                WenxinApiException.WenxinErrorType errorType = 
                    WenxinApiException.getErrorTypeByHttpStatus(response.code());
                
                log.error("文心Embedding API调用失败: {}", errorMessage);
                throw new WenxinApiException(errorMessage, 
                    String.valueOf(response.code()), 
                    response.code(), 
                    errorType);
            }
        } catch (IOException e) {
            log.error("文心Embedding API网络调用异常", e);
            throw new WenxinApiException("文心Embedding API网络调用异常: " + e.getMessage(), 
                e.getClass().getSimpleName(), 
                0, 
                WenxinApiException.WenxinErrorType.NETWORK_ERROR, 
                e);
        } catch (Exception e) {
            log.error("文心Embedding API调用发生未知异常", e);
            throw new WenxinApiException("文心Embedding API调用发生未知异常: " + e.getMessage(), 
                e.getClass().getSimpleName(), 
                0, 
                WenxinApiException.WenxinErrorType.UNKNOWN_ERROR, 
                e);
        }
    }
}
