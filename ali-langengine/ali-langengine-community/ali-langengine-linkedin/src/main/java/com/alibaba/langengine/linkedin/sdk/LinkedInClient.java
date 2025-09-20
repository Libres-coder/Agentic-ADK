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
package com.alibaba.langengine.linkedin.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.linkedin.LinkedInConfiguration.*;

/**
 * LinkedIn API Client for Java
 * This client provides methods to interact with the LinkedIn API v2.
 */
@Slf4j
public class LinkedInClient {

    private final String accessToken;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private static final int DEFAULT_TIMEOUT = 30;

    /**
     * Constructs a LinkedInClient with a specified Access Token.
     * 
     * @param accessToken the Access Token for authentication with the LinkedIn API
     */
    public LinkedInClient(String accessToken) {
        this.accessToken = accessToken;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a LinkedInClient using the default Access Token from configuration.
     */
    public LinkedInClient() {
        this.accessToken = LINKEDIN_ACCESS_TOKEN;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a LinkedInClient with a specified Access Token and custom OkHttpClient.
     * 
     * @param accessToken the Access Token for authentication with the LinkedIn API
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public LinkedInClient(String accessToken, OkHttpClient okHttpClient) {
        this.accessToken = accessToken;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取当前用户信息
     * 
     * @return 用户信息
     * @throws LinkedInException 当API调用失败时抛出
     */
    public UserResponse getCurrentUser() throws LinkedInException {
        try {
            HttpUrl url = HttpUrl.parse(LINKEDIN_API_URL + "/people/~").newBuilder()
                    .addQueryParameter("projection", "(id,firstName,lastName,headline,summary,location,industry)")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new LinkedInException("LinkedIn API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, UserResponse.class);
            }
        } catch (IOException e) {
            throw new LinkedInException("Failed to get current user", e);
        }
    }

    /**
     * 搜索公司
     * 
     * @param keyword 搜索关键词
     * @param count 返回数量
     * @return 搜索结果
     * @throws LinkedInException 当API调用失败时抛出
     */
    public CompanySearchResponse searchCompanies(String keyword, int count) throws LinkedInException {
        try {
            HttpUrl url = HttpUrl.parse(LINKEDIN_API_URL + "/companySearch").newBuilder()
                    .addQueryParameter("keywords", keyword)
                    .addQueryParameter("count", String.valueOf(Math.min(count, 100)))
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new LinkedInException("LinkedIn API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, CompanySearchResponse.class);
            }
        } catch (IOException e) {
            throw new LinkedInException("Failed to search companies", e);
        }
    }

    /**
     * 获取公司信息
     * 
     * @param companyId 公司ID
     * @return 公司信息
     * @throws LinkedInException 当API调用失败时抛出
     */
    public CompanyResponse getCompany(String companyId) throws LinkedInException {
        try {
            HttpUrl url = HttpUrl.parse(LINKEDIN_API_URL + "/companies/" + companyId).newBuilder()
                    .addQueryParameter("projection", "(id,name,description,websiteUrl,industry,companySize,logoUrl)")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new LinkedInException("LinkedIn API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, CompanyResponse.class);
            }
        } catch (IOException e) {
            throw new LinkedInException("Failed to get company", e);
        }
    }

    /**
     * 搜索人员
     * 
     * @param keyword 搜索关键词
     * @param count 返回数量
     * @return 搜索结果
     * @throws LinkedInException 当API调用失败时抛出
     */
    public PeopleSearchResponse searchPeople(String keyword, int count) throws LinkedInException {
        try {
            HttpUrl url = HttpUrl.parse(LINKEDIN_API_URL + "/peopleSearch").newBuilder()
                    .addQueryParameter("keywords", keyword)
                    .addQueryParameter("count", String.valueOf(Math.min(count, 100)))
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new LinkedInException("LinkedIn API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, PeopleSearchResponse.class);
            }
        } catch (IOException e) {
            throw new LinkedInException("Failed to search people", e);
        }
    }
}