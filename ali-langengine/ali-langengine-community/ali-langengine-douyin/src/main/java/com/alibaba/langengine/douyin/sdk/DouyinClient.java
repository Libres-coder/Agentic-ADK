package com.alibaba.langengine.douyin.sdk;

import com.alibaba.langengine.douyin.DouyinConfiguration;
import com.alibaba.langengine.douyin.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 抖音客户端
 */
public class DouyinClient {
    private static final Logger log = LoggerFactory.getLogger(DouyinClient.class);
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public DouyinClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(DouyinConfiguration.DOUYIN_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DouyinConfiguration.DOUYIN_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DouyinConfiguration.DOUYIN_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 搜索视频
     */
    public DouyinSearchResponse searchVideos(String keyword, int count) throws IOException {
        String url = String.format("%s/search/item/?keyword=%s&count=%d&offset=0&sort_type=0&publish_time=0",
                DouyinConfiguration.DOUYIN_BASE_URL, keyword, count);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", DouyinConfiguration.DOUYIN_USER_AGENT)
                .addHeader("Referer", "https://www.douyin.com/search/")
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("搜索视频失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, DouyinSearchResponse.class);
        }
    }
    
    /**
     * 搜索用户
     */
    public DouyinSearchResponse searchUsers(String keyword, int count) throws IOException {
        String url = String.format("%s/search/user/?keyword=%s&count=%d&offset=0&sort_type=0",
                DouyinConfiguration.DOUYIN_BASE_URL, keyword, count);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", DouyinConfiguration.DOUYIN_USER_AGENT)
                .addHeader("Referer", "https://www.douyin.com/search/")
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("搜索用户失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, DouyinSearchResponse.class);
        }
    }
    
    /**
     * 获取用户信息
     */
    public DouyinUser getUserInfo(String userId) throws IOException {
        String url = String.format("%s/user/?user_id=%s", DouyinConfiguration.DOUYIN_BASE_URL, userId);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", DouyinConfiguration.DOUYIN_USER_AGENT)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取用户信息失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, DouyinUser.class);
        }
    }
    
    /**
     * 获取用户视频列表
     */
    public DouyinSearchResponse getUserVideos(String userId, int count) throws IOException {
        String url = String.format("%s/aweme/post/?user_id=%s&count=%d&max_cursor=0&min_cursor=0",
                DouyinConfiguration.DOUYIN_BASE_URL, userId, count);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", DouyinConfiguration.DOUYIN_USER_AGENT)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取用户视频失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, DouyinSearchResponse.class);
        }
    }
    
    /**
     * 获取视频详情
     */
    public DouyinVideo getVideoDetail(String awemeId) throws IOException {
        String url = String.format("%s/aweme/detail/?aweme_id=%s", DouyinConfiguration.DOUYIN_BASE_URL, awemeId);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", DouyinConfiguration.DOUYIN_USER_AGENT)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取视频详情失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, DouyinVideo.class);
        }
    }
}