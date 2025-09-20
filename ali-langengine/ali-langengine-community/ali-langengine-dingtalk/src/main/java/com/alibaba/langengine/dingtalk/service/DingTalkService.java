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
package com.alibaba.langengine.dingtalk.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.dingtalk.DingTalkConfiguration;
import com.alibaba.langengine.dingtalk.model.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 钉钉服务类
 * 提供钉钉API的调用功能
 * 
 * @author langengine
 */
@Slf4j
public class DingTalkService {
    
    private final DingTalkConfiguration config;
    private final OkHttpClient httpClient;
    private final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    
    public DingTalkService(DingTalkConfiguration config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 获取访问令牌
     */
    public String getAccessToken() throws IOException {
        String url = config.getServerUrl() + "/gettoken?appkey=" + config.getAppKey() + 
                    "&appsecret=" + config.getAppSecret();
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取访问令牌失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            AccessTokenResponse tokenResponse = JSON.parseObject(responseBody, AccessTokenResponse.class);
            
            if (tokenResponse.getErrcode() != 0) {
                throw new IOException("获取访问令牌失败: " + tokenResponse.getErrmsg());
            }
            
            return tokenResponse.getAccessToken();
        }
    }
    
    /**
     * 发送文本消息
     */
    public MessageSendResponse sendTextMessage(String userIds, String content) throws IOException {
        String accessToken = getAccessToken();
        String url = config.getServerUrl() + "/topapi/message/corpconversation/asyncsend_v2?access_token=" + accessToken;
        
        MessageSendRequest request = new MessageSendRequest();
        request.setAgentId(Long.parseLong(config.getAgentId()));
        request.setUseridList(userIds);
        
        TextMessage textMessage = new TextMessage();
        textMessage.setContent(content);
        request.setMsg(textMessage);
        
        String jsonBody = JSON.toJSONString(request);
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, jsonBody);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            return JSON.parseObject(responseBody, MessageSendResponse.class);
        }
    }
    
    /**
     * 发送链接消息
     */
    public MessageSendResponse sendLinkMessage(String userIds, String title, String text, String messageUrl, String picUrl) throws IOException {
        String accessToken = getAccessToken();
        String url = config.getServerUrl() + "/topapi/message/corpconversation/asyncsend_v2?access_token=" + accessToken;
        
        MessageSendRequest request = new MessageSendRequest();
        request.setAgentId(Long.parseLong(config.getAgentId()));
        request.setUseridList(userIds);
        
        LinkMessage linkMessage = new LinkMessage();
        linkMessage.setTitle(title);
        linkMessage.setText(text);
        linkMessage.setMessageUrl(messageUrl);
        linkMessage.setPicUrl(picUrl);
        request.setMsg(linkMessage);
        
        String jsonBody = JSON.toJSONString(request);
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, jsonBody);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            return JSON.parseObject(responseBody, MessageSendResponse.class);
        }
    }
    
    /**
     * 获取用户信息
     */
    public UserInfoResponse getUserInfo(String userId) throws IOException {
        String accessToken = getAccessToken();
        String url = config.getServerUrl() + "/topapi/v2/user/get?access_token=" + accessToken;
        
        UserInfoRequest request = new UserInfoRequest();
        request.setUserid(userId);
        
        String jsonBody = JSON.toJSONString(request);
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, jsonBody);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            return JSON.parseObject(responseBody, UserInfoResponse.class);
        }
    }
    
    /**
     * 获取部门列表
     */
    public DepartmentListResponse getDepartmentList(Long deptId) throws IOException {
        String accessToken = getAccessToken();
        String url = config.getServerUrl() + "/topapi/v2/department/listsub?access_token=" + accessToken;
        
        DepartmentListRequest request = new DepartmentListRequest();
        request.setDeptId(deptId);
        
        String jsonBody = JSON.toJSONString(request);
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, jsonBody);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            return JSON.parseObject(responseBody, DepartmentListResponse.class);
        }
    }
}
