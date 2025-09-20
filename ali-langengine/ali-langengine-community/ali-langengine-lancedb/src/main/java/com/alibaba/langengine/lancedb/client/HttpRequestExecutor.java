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
package com.alibaba.langengine.lancedb.client;

import com.alibaba.langengine.lancedb.LanceDbException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;


@Slf4j
public class HttpRequestExecutor {
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public HttpRequestExecutor(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 执行HTTP请求并解析响应
     *
     * @param httpRequest   HTTP请求
     * @param responseType  响应类型
     * @param failureCode   失败错误码
     * @param <T>           响应类型泛型
     * @return 解析后的响应对象
     * @throws LanceDbException 请求失败异常
     */
    public <T> T executeAndParse(Request httpRequest, Class<T> responseType, String failureCode) 
            throws LanceDbException {
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                String errorMsg = String.format("Request failed with status %d: %s", 
                        response.code(), responseBody);
                throw new LanceDbException(errorMsg, failureCode, response.code());
            }
            
            if (responseBody.isEmpty()) {
                throw new LanceDbException("Empty response from LanceDB API", failureCode);
            }
            
            return objectMapper.readValue(responseBody, responseType);
        } catch (IOException e) {
            throw new LanceDbException("Failed to execute HTTP request", e, failureCode, null);
        }
    }
    
    /**
     * 执行不需要解析响应体的HTTP请求
     *
     * @param httpRequest HTTP请求
     * @param failureCode 失败错误码
     * @throws LanceDbException 请求失败异常
     */
    public void executeNoResponse(Request httpRequest, String failureCode) throws LanceDbException {
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                String errorMsg = String.format("Request failed with status %d: %s", 
                        response.code(), responseBody);
                throw new LanceDbException(errorMsg, failureCode, response.code());
            }
        } catch (IOException e) {
            throw new LanceDbException("Failed to execute HTTP request", e, failureCode, null);
        }
    }
    
    /**
     * 执行HTTP请求并返回原始响应体
     *
     * @param httpRequest HTTP请求
     * @param failureCode 失败错误码
     * @return 响应体字符串
     * @throws LanceDbException 请求失败异常
     */
    public String executeForRawResponse(Request httpRequest, String failureCode) throws LanceDbException {
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                String errorMsg = String.format("Request failed with status %d: %s", 
                        response.code(), responseBody);
                throw new LanceDbException(errorMsg, failureCode, response.code());
            }
            
            return responseBody;
        } catch (IOException e) {
            throw new LanceDbException("Failed to execute HTTP request", e, failureCode, null);
        }
    }
}
