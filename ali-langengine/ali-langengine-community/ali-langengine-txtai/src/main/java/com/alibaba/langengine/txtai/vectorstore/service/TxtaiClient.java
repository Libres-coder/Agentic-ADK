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
package com.alibaba.langengine.txtai.vectorstore.service;

import com.alibaba.langengine.txtai.exception.TxtaiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TxtaiClient {

    private final String serverUrl;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TxtaiClient(String serverUrl) {
        this.serverUrl = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 发送POST请求
     *
     * @param endpoint API端点
     * @param requestBody 请求体
     * @return 响应字符串
     * @throws IOException 网络异常
     */
    public String post(String endpoint, Object requestBody) throws IOException {
        String url = serverUrl + endpoint;
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");

        if (requestBody != null) {
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(jsonRequest, StandardCharsets.UTF_8));
            log.debug("发送POST请求到: {} 请求体: {}", url, jsonRequest);
        }

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                log.debug("POST请求成功，状态码: {} 响应: {}", statusCode, responseBody);
                return responseBody;
            } else {
                log.error("POST请求失败，状态码: {} 响应: {}", statusCode, responseBody);
                throw TxtaiException.apiError(statusCode, responseBody);
            }
        }
    }

    /**
     * 发送GET请求
     *
     * @param endpoint API端点
     * @return 响应字符串
     * @throws IOException 网络异常
     */
    public String get(String endpoint) throws IOException {
        String url = serverUrl + endpoint;
        HttpGet httpGet = new HttpGet(url);

        httpGet.setHeader("Accept", "application/json");

        log.debug("发送GET请求到: {}", url);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                log.debug("GET请求成功，状态码: {} 响应: {}", statusCode, responseBody);
                return responseBody;
            } else {
                log.error("GET请求失败，状态码: {} 响应: {}", statusCode, responseBody);
                throw TxtaiException.apiError(statusCode, responseBody);
            }
        }
    }

    /**
     * 关闭客户端
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            log.warn("关闭HTTP客户端时发生异常", e);
        }
    }
}