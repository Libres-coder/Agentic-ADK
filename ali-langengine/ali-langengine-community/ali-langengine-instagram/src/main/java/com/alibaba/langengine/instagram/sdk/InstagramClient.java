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
package com.alibaba.langengine.instagram.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.instagram.InstagramConfiguration.*;

/**
 * Instagram API Client for Java
 * This client provides methods to interact with the Instagram Graph API.
 */
@Slf4j
public class InstagramClient {

    private final String accessToken;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private static final int DEFAULT_TIMEOUT = 30;

    /**
     * Constructs an InstagramClient with a specified Access Token.
     * 
     * @param accessToken the Access Token for authentication with the Instagram API
     */
    public InstagramClient(String accessToken) {
        this.accessToken = accessToken;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs an InstagramClient using the default Access Token from configuration.
     */
    public InstagramClient() {
        this.accessToken = INSTAGRAM_ACCESS_TOKEN;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs an InstagramClient with a specified Access Token and custom OkHttpClient.
     * 
     * @param accessToken the Access Token for authentication with the Instagram API
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public InstagramClient(String accessToken, OkHttpClient okHttpClient) {
        this.accessToken = accessToken;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     * @throws InstagramException 当API调用失败时抛出
     */
    public UserResponse getUserInfo(String userId) throws InstagramException {
        try {
            HttpUrl url = HttpUrl.parse(INSTAGRAM_API_URL + "/" + userId).newBuilder()
                    .addQueryParameter("fields", "id,username,account_type,media_count")
                    .addQueryParameter("access_token", accessToken)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new InstagramException("Instagram API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, UserResponse.class);
            }
        } catch (IOException e) {
            throw new InstagramException("Failed to get user info", e);
        }
    }

    /**
     * 获取用户的媒体内容
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 媒体内容
     * @throws InstagramException 当API调用失败时抛出
     */
    public MediaResponse getUserMedia(String userId, int limit) throws InstagramException {
        try {
            HttpUrl url = HttpUrl.parse(INSTAGRAM_API_URL + "/" + userId + "/media").newBuilder()
                    .addQueryParameter("fields", "id,caption,media_type,media_url,thumbnail_url,permalink,timestamp")
                    .addQueryParameter("limit", String.valueOf(Math.min(limit, 100)))
                    .addQueryParameter("access_token", accessToken)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new InstagramException("Instagram API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, MediaResponse.class);
            }
        } catch (IOException e) {
            throw new InstagramException("Failed to get user media", e);
        }
    }

    /**
     * 获取媒体详情
     * 
     * @param mediaId 媒体ID
     * @return 媒体详情
     * @throws InstagramException 当API调用失败时抛出
     */
    public MediaDetailResponse getMediaDetail(String mediaId) throws InstagramException {
        try {
            HttpUrl url = HttpUrl.parse(INSTAGRAM_API_URL + "/" + mediaId).newBuilder()
                    .addQueryParameter("fields", "id,caption,media_type,media_url,thumbnail_url,permalink,timestamp,like_count,comments_count")
                    .addQueryParameter("access_token", accessToken)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new InstagramException("Instagram API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, MediaDetailResponse.class);
            }
        } catch (IOException e) {
            throw new InstagramException("Failed to get media detail", e);
        }
    }

    /**
     * 搜索标签
     * 
     * @param hashtag 标签名（不包含#）
     * @param limit 限制数量
     * @return 标签媒体
     * @throws InstagramException 当API调用失败时抛出
     */
    public HashtagMediaResponse getHashtagMedia(String hashtag, int limit) throws InstagramException {
        try {
            // 首先获取hashtag ID
            String hashtagId = getHashtagId(hashtag);
            
            HttpUrl url = HttpUrl.parse(INSTAGRAM_API_URL + "/" + hashtagId + "/recent_media").newBuilder()
                    .addQueryParameter("fields", "id,caption,media_type,media_url,thumbnail_url,permalink,timestamp")
                    .addQueryParameter("limit", String.valueOf(Math.min(limit, 100)))
                    .addQueryParameter("access_token", accessToken)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new InstagramException("Instagram API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, HashtagMediaResponse.class);
            }
        } catch (IOException e) {
            throw new InstagramException("Failed to get hashtag media", e);
        }
    }

    private String getHashtagId(String hashtag) throws InstagramException {
        try {
            HttpUrl url = HttpUrl.parse(INSTAGRAM_API_URL + "/ig_hashtag_search").newBuilder()
                    .addQueryParameter("user_id", "me")
                    .addQueryParameter("q", hashtag)
                    .addQueryParameter("access_token", accessToken)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new InstagramException("Instagram API request failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                HashtagSearchResponse searchResponse = objectMapper.readValue(responseBody, HashtagSearchResponse.class);
                
                if (searchResponse.getData() != null && !searchResponse.getData().isEmpty()) {
                    return searchResponse.getData().get(0).getId();
                } else {
                    throw new InstagramException("Hashtag not found: " + hashtag);
                }
            }
        } catch (IOException e) {
            throw new InstagramException("Failed to get hashtag ID", e);
        }
    }
}