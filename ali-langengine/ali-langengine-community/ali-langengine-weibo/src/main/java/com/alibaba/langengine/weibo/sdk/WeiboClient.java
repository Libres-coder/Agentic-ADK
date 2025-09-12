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
package com.alibaba.langengine.weibo.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.weibo.WeiboConfiguration.*;

/**
 * 微博 API Client for Java
 * This client provides methods to interact with the Weibo API v2.
 */
@Slf4j
public class WeiboClient {

    private final String accessToken;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private static final int DEFAULT_TIMEOUT = 30;

    /**
     * Constructs a WeiboClient with a specified Access Token.
     * 
     * @param accessToken the Access Token for authentication with the Weibo API
     */
    public WeiboClient(String accessToken) {
        this.accessToken = accessToken;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a WeiboClient using the default Access Token from configuration.
     */
    public WeiboClient() {
        this.accessToken = WEIBO_ACCESS_TOKEN;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a WeiboClient with a specified Access Token and custom OkHttpClient.
     * 
     * @param accessToken the Access Token for authentication with the Weibo API
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public WeiboClient(String accessToken, OkHttpClient okHttpClient) {
        this.accessToken = accessToken;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 搜索微博
     * 
     * @param keyword 搜索关键词
     * @param count 返回数量
     * @return 搜索结果
     * @throws WeiboException 当API调用失败时抛出
     */
    public WeiboSearchResponse searchWeibo(String keyword, int count) throws WeiboException {
        try {
            HttpUrl url = HttpUrl.parse(WEIBO_API_URL + "/search/topics.json").newBuilder()
                    .addQueryParameter("q", keyword)
                    .addQueryParameter("count", String.valueOf(Math.min(count, 50)))
                    .addQueryParameter("access_token", accessToken)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new WeiboException("Weibo API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, WeiboSearchResponse.class);
            }
        } catch (IOException e) {
            throw new WeiboException("Failed to search weibo", e);
        }
    }

    /**
     * 获取用户信息
     * 
     * @param uid 用户ID
     * @return 用户信息
     * @throws WeiboException 当API调用失败时抛出
     */
    public UserResponse getUserInfo(String uid) throws WeiboException {
        try {
            HttpUrl url = HttpUrl.parse(WEIBO_API_URL + "/users/show.json").newBuilder()
                    .addQueryParameter("uid", uid)
                    .addQueryParameter("access_token", accessToken)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new WeiboException("Weibo API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, UserResponse.class);
            }
        } catch (IOException e) {
            throw new WeiboException("Failed to get user info", e);
        }
    }

    /**
     * 获取用户的微博时间线
     * 
     * @param uid 用户ID
     * @param count 返回数量
     * @return 微博时间线
     * @throws WeiboException 当API调用失败时抛出
     */
    public WeiboTimelineResponse getUserTimeline(String uid, int count) throws WeiboException {
        try {
            HttpUrl url = HttpUrl.parse(WEIBO_API_URL + "/statuses/user_timeline.json").newBuilder()
                    .addQueryParameter("uid", uid)
                    .addQueryParameter("count", String.valueOf(Math.min(count, 200)))
                    .addQueryParameter("access_token", accessToken)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new WeiboException("Weibo API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, WeiboTimelineResponse.class);
            }
        } catch (IOException e) {
            throw new WeiboException("Failed to get user timeline", e);
        }
    }

    /**
     * 获取热门话题
     * 
     * @param count 返回数量
     * @return 热门话题
     * @throws WeiboException 当API调用失败时抛出
     */
    public HotTopicsResponse getHotTopics(int count) throws WeiboException {
        try {
            HttpUrl url = HttpUrl.parse(WEIBO_API_URL + "/trends/hourly.json").newBuilder()
                    .addQueryParameter("count", String.valueOf(Math.min(count, 20)))
                    .addQueryParameter("access_token", accessToken)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new WeiboException("Weibo API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, HotTopicsResponse.class);
            }
        } catch (IOException e) {
            throw new WeiboException("Failed to get hot topics", e);
        }
    }
}