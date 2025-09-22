package com.alibaba.langengine.twitch.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.twitch.TwitchConfiguration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TwitchUserSearchTool extends BaseTool {
    
    private final TwitchConfiguration config;
    private final OkHttpClient httpClient;
    
    public TwitchUserSearchTool() {
        this(new TwitchConfiguration());
    }
    
    public TwitchUserSearchTool(TwitchConfiguration config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
        
        setName("twitch_user_search");
        setHumanName("Twitch用户搜索工具");
        setDescription("搜索Twitch平台上的用户和主播");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"用户名搜索关键词\"\n" +
                "    },\n" +
                "    \"limit\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"返回结果数量，最大100\",\n" +
                "      \"default\": 20\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"query\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);
            
            Map<String, Object> params = JSON.parseObject(toolInput, Map.class);
            String query = (String) params.get("query");
            Integer limit = (Integer) params.getOrDefault("limit", config.getDefaultLimit());
            
            if (StringUtils.isBlank(query)) {
                return new ToolExecuteResult("错误：搜索关键词不能为空");
            }
            
            if (StringUtils.isBlank(config.getClientId())) {
                return new ToolExecuteResult("错误：Twitch客户端ID未配置，请设置twitch.client.id系统属性");
            }
            
            // 获取访问令牌
            String accessToken = getAccessToken();
            if (StringUtils.isBlank(accessToken)) {
                return new ToolExecuteResult("错误：无法获取Twitch访问令牌，请检查客户端ID和密钥配置");
            }
            
            // 执行搜索
            String searchResult = performUserSearch(query, limit, accessToken);
            
            ToolExecuteResult toolResult = new ToolExecuteResult(searchResult);
            onToolEnd(this, toolInput, toolResult, executionContext);
            return toolResult;
            
        } catch (Exception e) {
            log.error("Twitch用户搜索异常", e);
            return new ToolExecuteResult("搜索异常: " + e.getMessage());
        }
    }
    
    private String getAccessToken() {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("client_id", config.getClientId())
                    .add("client_secret", config.getClientSecret())
                    .add("grant_type", "client_credentials")
                    .build();
            
            Request request = new Request.Builder()
                    .url(config.getAuthUrl())
                    .post(formBody)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Map<String, Object> tokenData = JSON.parseObject(responseBody, Map.class);
                    return (String) tokenData.get("access_token");
                }
            }
        } catch (IOException e) {
            log.error("获取Twitch访问令牌失败", e);
        }
        return null;
    }
    
    private String performUserSearch(String query, int limit, String accessToken) {
        try {
            String url = config.getApiBaseUrl() + "/users?" +
                    "login=" + java.net.URLEncoder.encode(query, "UTF-8");
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Client-Id", config.getClientId())
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return formatUserResults(responseBody);
                } else {
                    return "搜索失败: HTTP " + response.code() + " - " + response.message();
                }
            }
        } catch (IOException e) {
            log.error("执行Twitch用户搜索失败", e);
            return "搜索异常: " + e.getMessage();
        }
    }
    
    private String formatUserResults(String responseBody) {
        try {
            Map<String, Object> userData = JSON.parseObject(responseBody, Map.class);
            StringBuilder result = new StringBuilder();
            
            java.util.List<Map<String, Object>> users = (java.util.List<Map<String, Object>>) userData.get("data");
            if (users != null && !users.isEmpty()) {
                result.append("=== Twitch用户搜索结果 ===\n");
                
                for (int i = 0; i < users.size(); i++) {
                    Map<String, Object> user = users.get(i);
                    String id = (String) user.get("id");
                    String login = (String) user.get("login");
                    String displayName = (String) user.get("display_name");
                    String description = (String) user.get("description");
                    String profileImageUrl = (String) user.get("profile_image_url");
                    String broadcasterType = (String) user.get("broadcaster_type");
                    String createdAt = (String) user.get("created_at");
                    
                    result.append(String.format("%d. %s", i + 1, displayName != null ? displayName : login));
                    if (StringUtils.isNotBlank(broadcasterType)) {
                        result.append(String.format(" (%s)", broadcasterType));
                    }
                    if (StringUtils.isNotBlank(id)) {
                        result.append(String.format(" (ID: %s)", id));
                    }
                    result.append("\n");
                    
                    if (StringUtils.isNotBlank(description)) {
                        result.append(String.format("   简介: %s\n", 
                                description.length() > 100 ? description.substring(0, 100) + "..." : description));
                    }
                    
                    if (StringUtils.isNotBlank(profileImageUrl)) {
                        result.append(String.format("   头像: %s\n", profileImageUrl));
                    }
                    
                    if (StringUtils.isNotBlank(createdAt)) {
                        result.append(String.format("   注册时间: %s\n", createdAt.substring(0, 10)));
                    }
                    
                    result.append(String.format("   频道链接: https://www.twitch.tv/%s\n", login));
                    result.append("\n");
                }
            } else {
                result.append("未找到相关的用户。\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("格式化用户搜索结果失败", e);
            return "搜索结果格式化失败: " + e.getMessage();
        }
    }
}
