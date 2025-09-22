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
public class TwitchStreamSearchTool extends BaseTool {
    
    private final TwitchConfiguration config;
    private final OkHttpClient httpClient;
    
    public TwitchStreamSearchTool() {
        this(new TwitchConfiguration());
    }
    
    public TwitchStreamSearchTool(TwitchConfiguration config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
        
        setName("twitch_stream_search");
        setHumanName("Twitch直播搜索工具");
        setDescription("搜索Twitch直播频道和主播");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"搜索关键词（主播名称或游戏名称）\"\n" +
                "    },\n" +
                "    \"gameId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"游戏ID，用于筛选特定游戏的直播\"\n" +
                "    },\n" +
                "    \"language\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"语言代码，如zh, en, ja\",\n" +
                "      \"default\": \"zh\"\n" +
                "    },\n" +
                "    \"limit\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"返回结果数量，最大100\",\n" +
                "      \"default\": 20\n" +
                "    },\n" +
                "    \"sortBy\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"排序方式：viewer_count(观看人数), started_at(开始时间)\",\n" +
                "      \"default\": \"viewer_count\"\n" +
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
            String gameId = (String) params.get("gameId");
            String language = (String) params.getOrDefault("language", config.getDefaultLanguage());
            Integer limit = (Integer) params.getOrDefault("limit", config.getDefaultLimit());
            String sortBy = (String) params.getOrDefault("sortBy", "viewer_count");
            
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
            String searchResult = performStreamSearch(query, gameId, language, limit, sortBy, accessToken);
            
            ToolExecuteResult toolResult = new ToolExecuteResult(searchResult);
            onToolEnd(this, toolInput, toolResult, executionContext);
            return toolResult;
            
        } catch (Exception e) {
            log.error("Twitch直播搜索异常", e);
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
    
    private String performStreamSearch(String query, String gameId, String language, int limit, String sortBy, String accessToken) {
        try {
            StringBuilder urlBuilder = new StringBuilder(config.getApiBaseUrl() + "/streams");
            urlBuilder.append("?first=").append(Math.min(limit, config.getMaxLimit()));
            
            if (StringUtils.isNotBlank(gameId)) {
                urlBuilder.append("&game_id=").append(gameId);
            }
            
            if (StringUtils.isNotBlank(language)) {
                urlBuilder.append("&language=").append(language);
            }
            
            Request request = new Request.Builder()
                    .url(urlBuilder.toString())
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Client-Id", config.getClientId())
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return formatStreamResults(responseBody, query);
                } else {
                    return "搜索失败: HTTP " + response.code() + " - " + response.message();
                }
            }
        } catch (IOException e) {
            log.error("执行Twitch直播搜索失败", e);
            return "搜索异常: " + e.getMessage();
        }
    }
    
    private String formatStreamResults(String responseBody, String query) {
        try {
            Map<String, Object> streamData = JSON.parseObject(responseBody, Map.class);
            StringBuilder result = new StringBuilder();
            
            java.util.List<Map<String, Object>> streams = (java.util.List<Map<String, Object>>) streamData.get("data");
            if (streams != null && !streams.isEmpty()) {
                result.append("=== Twitch直播搜索结果 ===\n");
                
                for (int i = 0; i < streams.size(); i++) {
                    Map<String, Object> stream = streams.get(i);
                    Map<String, Object> user = (Map<String, Object>) stream.get("user_name");
                    Map<String, Object> game = (Map<String, Object>) stream.get("game_name");
                    
                    String userName = (String) stream.get("user_name");
                    String gameName = (String) stream.get("game_name");
                    String title = (String) stream.get("title");
                    Integer viewerCount = (Integer) stream.get("viewer_count");
                    String startedAt = (String) stream.get("started_at");
                    String thumbnailUrl = (String) stream.get("thumbnail_url");
                    
                    result.append(String.format("%d. %s", i + 1, userName != null ? userName : "未知主播"));
                    if (StringUtils.isNotBlank(gameName)) {
                        result.append(String.format(" - 正在玩: %s", gameName));
                    }
                    if (viewerCount != null) {
                        result.append(String.format(" (观看人数: %d)", viewerCount));
                    }
                    result.append("\n");
                    
                    if (StringUtils.isNotBlank(title)) {
                        result.append(String.format("   标题: %s\n", title));
                    }
                    
                    if (StringUtils.isNotBlank(startedAt)) {
                        result.append(String.format("   开始时间: %s\n", startedAt));
                    }
                    
                    if (StringUtils.isNotBlank(thumbnailUrl)) {
                        result.append(String.format("   缩略图: %s\n", thumbnailUrl));
                    }
                    
                    result.append(String.format("   直播链接: https://www.twitch.tv/%s\n", userName));
                    result.append("\n");
                }
            } else {
                result.append("未找到相关的直播内容。\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("格式化直播搜索结果失败", e);
            return "搜索结果格式化失败: " + e.getMessage();
        }
    }
}
