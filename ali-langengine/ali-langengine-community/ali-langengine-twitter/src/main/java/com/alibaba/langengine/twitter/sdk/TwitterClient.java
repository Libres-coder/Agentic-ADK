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
package com.alibaba.langengine.twitter.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.twitter.TwitterConfiguration.*;

/**
 * Twitter API Client for Java
 * This client provides methods to interact with the Twitter API v2.
 */
@Slf4j
public class TwitterClient {

    private final String bearerToken;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private static final int DEFAULT_TIMEOUT = 30;

    /**
     * Constructs a TwitterClient with a specified Bearer Token.
     * 
     * @param bearerToken the Bearer Token for authentication with the Twitter API
     */
    public TwitterClient(String bearerToken) {
        this.bearerToken = bearerToken;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a TwitterClient using the default Bearer Token from configuration.
     */
    public TwitterClient() {
        this.bearerToken = TWITTER_BEARER_TOKEN;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a TwitterClient with a specified Bearer Token and custom OkHttpClient.
     * 
     * @param bearerToken the Bearer Token for authentication with the Twitter API
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public TwitterClient(String bearerToken, OkHttpClient okHttpClient) {
        this.bearerToken = bearerToken;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 搜索推文
     * 
     * @param query 搜索查询
     * @param maxResults 最大结果数
     * @return 搜索结果
     * @throws TwitterException 当API调用失败时抛出
     */
    public TweetSearchResponse searchTweets(String query, int maxResults) throws TwitterException {
        return searchTweets(query, maxResults, null, null);
    }

    /**
     * 搜索推文
     * 
     * @param query 搜索查询
     * @param maxResults 最大结果数
     * @param startTime 开始时间 (ISO 8601格式)
     * @param endTime 结束时间 (ISO 8601格式)
     * @return 搜索结果
     * @throws TwitterException 当API调用失败时抛出
     */
    public TweetSearchResponse searchTweets(String query, int maxResults, String startTime, String endTime) throws TwitterException {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(TWITTER_API_URL + "/tweets/search/recent").newBuilder()
                    .addQueryParameter("query", query)
                    .addQueryParameter("max_results", String.valueOf(Math.min(maxResults, 100)))
                    .addQueryParameter("tweet.fields", "created_at,author_id,public_metrics,context_annotations");

            if (startTime != null) {
                urlBuilder.addQueryParameter("start_time", startTime);
            }
            if (endTime != null) {
                urlBuilder.addQueryParameter("end_time", endTime);
            }

            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Authorization", "Bearer " + bearerToken)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new TwitterException("Twitter API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, TweetSearchResponse.class);
            }
        } catch (IOException e) {
            throw new TwitterException("Failed to search tweets", e);
        }
    }

    /**
     * 获取用户信息
     * 
     * @param username 用户名
     * @return 用户信息
     * @throws TwitterException 当API调用失败时抛出
     */
    public UserResponse getUserByUsername(String username) throws TwitterException {
        try {
            HttpUrl url = HttpUrl.parse(TWITTER_API_URL + "/users/by/username/" + username).newBuilder()
                    .addQueryParameter("user.fields", "created_at,description,public_metrics,verified")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + bearerToken)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new TwitterException("Twitter API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, UserResponse.class);
            }
        } catch (IOException e) {
            throw new TwitterException("Failed to get user by username", e);
        }
    }

    /**
     * 获取用户的推文时间线
     * 
     * @param userId 用户ID
     * @param maxResults 最大结果数
     * @return 推文时间线
     * @throws TwitterException 当API调用失败时抛出
     */
    public TweetTimelineResponse getUserTimeline(String userId, int maxResults) throws TwitterException {
        try {
            HttpUrl url = HttpUrl.parse(TWITTER_API_URL + "/users/" + userId + "/tweets").newBuilder()
                    .addQueryParameter("max_results", String.valueOf(Math.min(maxResults, 100)))
                    .addQueryParameter("tweet.fields", "created_at,public_metrics,context_annotations")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + bearerToken)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new TwitterException("Twitter API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, TweetTimelineResponse.class);
            }
        } catch (IOException e) {
            throw new TwitterException("Failed to get user timeline", e);
        }
    }
}