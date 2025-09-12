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
package com.alibaba.langengine.wenxin.model.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.wenxin.WenxinConfiguration.*;


@Slf4j
public class WenxinAuthService {

    private static final String TOKEN_URL = "oauth/2.0/token";
    private static final String GRANT_TYPE = "client_credentials";
    
    /**
     * Token缓存，避免频繁请求
     */
    private static final Map<String, TokenInfo> TOKEN_CACHE = new ConcurrentHashMap<>();
    
    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public WenxinAuthService() {
        this(WENXIN_SERVER_URL, Duration.ofSeconds(Long.parseLong(WENXIN_API_TIMEOUT)));
    }

    public WenxinAuthService(String baseUrl, Duration timeout) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 获取访问令牌
     *
     * @param apiKey    API Key
     * @param secretKey Secret Key
     * @return Access Token
     * @throws IOException 网络异常
     */
    public String getAccessToken(String apiKey, String secretKey) throws IOException {
        if (StringUtils.isBlank(apiKey) || StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("API Key和Secret Key不能为空");
        }

        String cacheKey = apiKey + ":" + secretKey;
        TokenInfo tokenInfo = TOKEN_CACHE.get(cacheKey);
        
        // 检查token是否过期（提前5分钟刷新）
        if (tokenInfo != null && (System.currentTimeMillis() - tokenInfo.getCreateTime()) < (tokenInfo.getExpiresIn() - 300) * 1000L) {
            return tokenInfo.getAccessToken();
        }

        // 请求新的token
        String url = baseUrl + TOKEN_URL + "?grant_type=" + GRANT_TYPE + "&client_id=" + apiKey + "&client_secret=" + secretKey;
        
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create("", MediaType.parse("application/x-www-form-urlencoded")))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("User-Agent", DEFAULT_USER_AGENT)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取Access Token失败: HTTP " + response.code() + " - " + response.message());
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("响应体为空");
            }

            String responseText = responseBody.string();
            log.debug("Token响应: {}", responseText);

            TokenResponse tokenResponse = objectMapper.readValue(responseText, TokenResponse.class);
            if (tokenResponse.getError() != null) {
                throw new IOException("获取Access Token失败: " + tokenResponse.getError() + " - " + tokenResponse.getErrorDescription());
            }

            if (StringUtils.isBlank(tokenResponse.getAccessToken())) {
                throw new IOException("Access Token为空");
            }

            // 缓存token
            TokenInfo newTokenInfo = new TokenInfo();
            newTokenInfo.setAccessToken(tokenResponse.getAccessToken());
            newTokenInfo.setExpiresIn(tokenResponse.getExpiresIn());
            newTokenInfo.setCreateTime(System.currentTimeMillis());
            TOKEN_CACHE.put(cacheKey, newTokenInfo);

            log.info("成功获取Access Token，有效期: {} 秒", tokenResponse.getExpiresIn());
            return tokenResponse.getAccessToken();
        }
    }

    /**
     * 清除缓存的token
     */
    public static void clearTokenCache() {
        TOKEN_CACHE.clear();
    }

    /**
     * 清除指定的token缓存
     */
    public static void clearTokenCache(String apiKey, String secretKey) {
        if (StringUtils.isNotBlank(apiKey) && StringUtils.isNotBlank(secretKey)) {
            TOKEN_CACHE.remove(apiKey + ":" + secretKey);
        }
    }

    /**
     * Token响应类
     */
    @Data
    private static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        
        @JsonProperty("expires_in")
        private Long expiresIn;
        
        @JsonProperty("error")
        private String error;
        
        @JsonProperty("error_description")
        private String errorDescription;
    }

    /**
     * Token信息缓存类
     */
    @Data
    private static class TokenInfo {
        private String accessToken;
        private Long expiresIn;
        private Long createTime;
    }
}
