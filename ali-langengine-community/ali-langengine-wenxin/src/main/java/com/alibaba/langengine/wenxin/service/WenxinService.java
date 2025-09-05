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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.wenxin.model.completion.WenxinCompletionRequest;
import com.alibaba.langengine.wenxin.model.completion.WenxinCompletionResult;
import com.alibaba.langengine.wenxin.model.embedding.WenxinEmbeddingRequest;
import com.alibaba.langengine.wenxin.model.embedding.WenxinEmbeddingResult;
import com.alibaba.langengine.wenxin.model.service.WenxinAuthService;
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

    public WenxinService(String serverUrl, Duration timeout, String apiKey, String secretKey) {
        this.serverUrl = serverUrl;
        this.timeout = timeout;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.authService = new WenxinAuthService(serverUrl, timeout);

        this.httpClient = new OkHttpClient.Builder()
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    public WenxinCompletionResult createCompletion(WenxinCompletionRequest request) {
        try {
            String accessToken = authService.getAccessToken(apiKey, secretKey);
            String url = serverUrl + "rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-4.0-8k?access_token=" + accessToken;
            
            String jsonBody = JSON.toJSONString(request);
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
                return JSON.parseObject(responseBody, WenxinCompletionResult.class);
            } else {
                log.error("文心一言API调用失败: {} {}", response.code(), response.message());
                throw new RuntimeException("文心一言API调用失败: " + response.code());
            }
        } catch (IOException e) {
            log.error("文心一言API调用异常", e);
            throw new RuntimeException("文心一言API调用异常", e);
        }
    }

    public WenxinEmbeddingResult createEmbedding(WenxinEmbeddingRequest request) {
        try {
            String accessToken = authService.getAccessToken(apiKey, secretKey);
            String url = serverUrl + "rpc/2.0/ai_custom/v1/wenxinworkshop/embeddings/embedding-v1?access_token=" + accessToken;
            
            String jsonBody = JSON.toJSONString(request);
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
                return JSON.parseObject(responseBody, WenxinEmbeddingResult.class);
            } else {
                log.error("文心Embedding API调用失败: {} {}", response.code(), response.message());
                throw new RuntimeException("文心Embedding API调用失败: " + response.code());
            }
        } catch (IOException e) {
            log.error("文心Embedding API调用异常", e);
            throw new RuntimeException("文心Embedding API调用异常", e);
        }
    }
}
