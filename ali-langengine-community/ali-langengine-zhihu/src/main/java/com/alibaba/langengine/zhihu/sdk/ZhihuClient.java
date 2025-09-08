package com.alibaba.langengine.zhihu.sdk;

import com.alibaba.langengine.zhihu.ZhihuConfiguration;
import com.alibaba.langengine.zhihu.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 知乎客户端
 */
public class ZhihuClient {
    private static final Logger log = LoggerFactory.getLogger(ZhihuClient.class);
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public ZhihuClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(ZhihuConfiguration.ZHIHU_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ZhihuConfiguration.ZHIHU_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ZhihuConfiguration.ZHIHU_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 搜索问题
     */
    public ZhihuSearchResponse searchQuestions(String query, int limit) throws IOException {
        String url = String.format("%s/search_v3?t=general&q=%s&correction=1&offset=0&limit=%d&lc_idx=0&show_all_topics=0",
                ZhihuConfiguration.ZHIHU_BASE_URL, query, limit);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", ZhihuConfiguration.ZHIHU_USER_AGENT)
                .addHeader("Referer", "https://www.zhihu.com/search")
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("搜索问题失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, ZhihuSearchResponse.class);
        }
    }
    
    /**
     * 获取问题详情
     */
    public ZhihuQuestion getQuestion(String questionId) throws IOException {
        String url = String.format("%s/questions/%s", ZhihuConfiguration.ZHIHU_BASE_URL, questionId);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", ZhihuConfiguration.ZHIHU_USER_AGENT)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取问题详情失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, ZhihuQuestion.class);
        }
    }
    
    /**
     * 获取问题回答
     */
    public ZhihuAnswerResponse getQuestionAnswers(String questionId, int limit) throws IOException {
        String url = String.format("%s/questions/%s/answers?include=data[*].is_normal,admin_closed_comment,reward_info,is_collapsed,annotation_action,annotation_detail,collapse_reason,is_sticky,collapsed_by,suggest_edit,comment_count,can_comment,content,editable_content,voteup_count,reshipment_settings,comment_permission,created_time,updated_time,review_info,relevant_info,question_type,excerpt,relationship.is_authorized,is_author,voting,is_thanked,is_nothelp,is_labeled,is_recognized,paid_info,paid_info_content;data[*].mark_infos[*].url;data[*].author.follower_count,badge[*].topics&offset=0&limit=%d&sort_by=default&platform=desktop",
                ZhihuConfiguration.ZHIHU_BASE_URL, questionId, limit);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", ZhihuConfiguration.ZHIHU_USER_AGENT)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取问题回答失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, ZhihuAnswerResponse.class);
        }
    }
    
    /**
     * 获取用户信息
     */
    public ZhihuAuthor getUserInfo(String userId) throws IOException {
        String url = String.format("%s/members/%s", ZhihuConfiguration.ZHIHU_BASE_URL, userId);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", ZhihuConfiguration.ZHIHU_USER_AGENT)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取用户信息失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, ZhihuAuthor.class);
        }
    }
}