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
public class TwitchGameSearchTool extends BaseTool {
    
    private final TwitchConfiguration config;
    private final OkHttpClient httpClient;
    
    public TwitchGameSearchTool() {
        this(new TwitchConfiguration());
    }
    
    public TwitchGameSearchTool(TwitchConfiguration config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
        
        setName("twitch_game_search");
        setHumanName("Twitch游戏搜索工具");
        setDescription("搜索Twitch平台上的游戏");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"游戏名称搜索关键词\"\n" +
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
            String searchResult = performGameSearch(query, limit, accessToken);
            
            ToolExecuteResult toolResult = new ToolExecuteResult(searchResult);
            onToolEnd(this, toolInput, toolResult, executionContext);
            return toolResult;
            
        } catch (Exception e) {
            log.error("Twitch游戏搜索异常", e);
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
    
    private String performGameSearch(String query, int limit, String accessToken) {
        try {
            String url = config.getApiBaseUrl() + "/games?" +
                    "name=" + java.net.URLEncoder.encode(query, "UTF-8");
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Client-Id", config.getClientId())
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return formatGameResults(responseBody);
                } else {
                    return "搜索失败: HTTP " + response.code() + " - " + response.message();
                }
            }
        } catch (IOException e) {
            log.error("执行Twitch游戏搜索失败", e);
            return "搜索异常: " + e.getMessage();
        }
    }
    
    private String formatGameResults(String responseBody) {
        try {
            Map<String, Object> gameData = JSON.parseObject(responseBody, Map.class);
            StringBuilder result = new StringBuilder();
            
            java.util.List<Map<String, Object>> games = (java.util.List<Map<String, Object>>) gameData.get("data");
            if (games != null && !games.isEmpty()) {
                result.append("=== Twitch游戏搜索结果 ===\n");
                
                for (int i = 0; i < games.size(); i++) {
                    Map<String, Object> game = games.get(i);
                    String id = (String) game.get("id");
                    String name = (String) game.get("name");
                    String boxArtUrl = (String) game.get("box_art_url");
                    
                    result.append(String.format("%d. %s", i + 1, name != null ? name : "未知游戏"));
                    if (StringUtils.isNotBlank(id)) {
                        result.append(String.format(" (ID: %s)", id));
                    }
                    result.append("\n");
                    
                    if (StringUtils.isNotBlank(boxArtUrl)) {
                        // 替换URL中的尺寸参数
                        String thumbnailUrl = boxArtUrl.replace("{width}", "285").replace("{height}", "380");
                        result.append(String.format("   封面: %s\n", thumbnailUrl));
                    }
                    
                    result.append("\n");
                }
            } else {
                result.append("未找到相关的游戏。\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("格式化游戏搜索结果失败", e);
            return "搜索结果格式化失败: " + e.getMessage();
        }
    }
}
