package com.alibaba.langengine.wework.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.wework.WeWorkConfiguration;
import com.alibaba.langengine.wework.model.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WeWorkService {
    
    private final WeWorkConfiguration config;
    private final OkHttpClient httpClient;
    private final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    
    public WeWorkService(WeWorkConfiguration config) {
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
        String url = config.getServerUrl() + "/cgi-bin/gettoken?corpid=" + config.getCorpId() + 
                    "&corpsecret=" + config.getCorpSecret();
        
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
    public MessageSendResponse sendTextMessage(String touser, String content) throws IOException {
        String accessToken = getAccessToken();
        String url = config.getServerUrl() + "/cgi-bin/message/send?access_token=" + accessToken;
        
        MessageSendRequest request = new MessageSendRequest();
        request.setTouser(touser);
        request.setMsgtype("text");
        request.setAgentid(Integer.parseInt(config.getAgentId()));
        
        TextMessage textMessage = new TextMessage();
        textMessage.setContent(content);
        request.setText(textMessage);
        
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
    public UserInfoResponse getUserInfo(String userid) throws IOException {
        String accessToken = getAccessToken();
        String url = config.getServerUrl() + "/cgi-bin/user/get?access_token=" + accessToken + "&userid=" + userid;
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            return JSON.parseObject(responseBody, UserInfoResponse.class);
        }
    }
    
    /**
     * 获取部门列表
     */
    public DepartmentListResponse getDepartmentList(Integer departmentId) throws IOException {
        String accessToken = getAccessToken();
        String url = config.getServerUrl() + "/cgi-bin/department/list?access_token=" + accessToken;
        if (departmentId != null) {
            url += "&id=" + departmentId;
        }
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            return JSON.parseObject(responseBody, DepartmentListResponse.class);
        }
    }
}
