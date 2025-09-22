package com.alibaba.langengine.spotify.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.spotify.SpotifyConfiguration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SpotifySearchTool extends BaseTool {
    
    private final SpotifyConfiguration config;
    private final OkHttpClient httpClient;
    
    public SpotifySearchTool() {
        this(new SpotifyConfiguration());
    }
    
    public SpotifySearchTool(SpotifyConfiguration config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
        
        setName("spotify_search");
        setHumanName("Spotify音乐搜索工具");
        setDescription("搜索Spotify音乐、艺术家、专辑和播放列表");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"搜索关键词\"\n" +
                "    },\n" +
                "    \"type\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"搜索类型：track(歌曲), artist(艺术家), album(专辑), playlist(播放列表)，多个用逗号分隔\",\n" +
                "      \"default\": \"track\"\n" +
                "    },\n" +
                "    \"limit\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"返回结果数量，最大50\",\n" +
                "      \"default\": 20\n" +
                "    },\n" +
                "    \"offset\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"偏移量，用于分页\",\n" +
                "      \"default\": 0\n" +
                "    },\n" +
                "    \"market\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"市场代码，如CN, US等\",\n" +
                "      \"default\": \"CN\"\n" +
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
            String type = (String) params.getOrDefault("type", "track");
            Integer limit = (Integer) params.getOrDefault("limit", config.getDefaultLimit());
            Integer offset = (Integer) params.getOrDefault("offset", 0);
            String market = (String) params.getOrDefault("market", "CN");
            
            if (StringUtils.isBlank(query)) {
                return new ToolExecuteResult("错误：搜索关键词不能为空");
            }
            
            // 限制结果数量
            if (limit > config.getMaxLimit()) {
                limit = config.getMaxLimit();
            }
            
            // 获取访问令牌
            String accessToken = getAccessToken();
            if (StringUtils.isBlank(accessToken)) {
                return new ToolExecuteResult("错误：无法获取Spotify访问令牌，请检查客户端ID和密钥配置");
            }
            
            // 执行搜索
            String searchResult = performSearch(query, type, limit, offset, market, accessToken);
            
            ToolExecuteResult toolResult = new ToolExecuteResult(searchResult);
            onToolEnd(this, toolInput, toolResult, executionContext);
            return toolResult;
            
        } catch (Exception e) {
            log.error("Spotify搜索异常", e);
            return new ToolExecuteResult("搜索异常: " + e.getMessage());
        }
    }
    
    private String getAccessToken() {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("grant_type", "client_credentials")
                    .build();
            
            Request request = new Request.Builder()
                    .url(config.getAuthUrl())
                    .post(formBody)
                    .addHeader("Authorization", "Basic " + 
                            java.util.Base64.getEncoder().encodeToString(
                                    (config.getClientId() + ":" + config.getClientSecret()).getBytes()))
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Map<String, Object> tokenData = JSON.parseObject(responseBody, Map.class);
                    return (String) tokenData.get("access_token");
                }
            }
        } catch (IOException e) {
            log.error("获取Spotify访问令牌失败", e);
        }
        return null;
    }
    
    private String performSearch(String query, String type, int limit, int offset, String market, String accessToken) {
        try {
            String url = config.getApiBaseUrl() + "/search?" +
                    "q=" + java.net.URLEncoder.encode(query, "UTF-8") +
                    "&type=" + type +
                    "&limit=" + limit +
                    "&offset=" + offset +
                    "&market=" + market;
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return formatSearchResults(responseBody, type);
                } else {
                    return "搜索失败: HTTP " + response.code() + " - " + response.message();
                }
            }
        } catch (IOException e) {
            log.error("执行Spotify搜索失败", e);
            return "搜索异常: " + e.getMessage();
        }
    }
    
    private String formatSearchResults(String responseBody, String type) {
        try {
            Map<String, Object> searchData = JSON.parseObject(responseBody, Map.class);
            StringBuilder result = new StringBuilder();
            
            if (type.contains("track")) {
                Map<String, Object> tracks = (Map<String, Object>) searchData.get("tracks");
                if (tracks != null) {
                    result.append("=== 歌曲搜索结果 ===\n");
                    formatTracks(tracks, result);
                }
            }
            
            if (type.contains("artist")) {
                Map<String, Object> artists = (Map<String, Object>) searchData.get("artists");
                if (artists != null) {
                    result.append("\n=== 艺术家搜索结果 ===\n");
                    formatArtists(artists, result);
                }
            }
            
            if (type.contains("album")) {
                Map<String, Object> albums = (Map<String, Object>) searchData.get("albums");
                if (albums != null) {
                    result.append("\n=== 专辑搜索结果 ===\n");
                    formatAlbums(albums, result);
                }
            }
            
            if (type.contains("playlist")) {
                Map<String, Object> playlists = (Map<String, Object>) searchData.get("playlists");
                if (playlists != null) {
                    result.append("\n=== 播放列表搜索结果 ===\n");
                    formatPlaylists(playlists, result);
                }
            }
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("格式化搜索结果失败", e);
            return "搜索结果格式化失败: " + e.getMessage();
        }
    }
    
    private void formatTracks(Map<String, Object> tracks, StringBuilder result) {
        java.util.List<Map<String, Object>> items = (java.util.List<Map<String, Object>>) tracks.get("items");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> track = items.get(i);
                String name = (String) track.get("name");
                java.util.List<Map<String, Object>> artists = (java.util.List<Map<String, Object>>) track.get("artists");
                String artistName = artists != null && !artists.isEmpty() ? (String) artists.get(0).get("name") : "未知艺术家";
                String albumName = ((Map<String, Object>) track.get("album")).get("name").toString();
                String externalUrl = ((Map<String, Object>) track.get("external_urls")).get("spotify").toString();
                
                result.append(String.format("%d. %s - %s (专辑: %s)\n", 
                        i + 1, name, artistName, albumName));
                result.append(String.format("   Spotify链接: %s\n", externalUrl));
            }
        }
    }
    
    private void formatArtists(Map<String, Object> artists, StringBuilder result) {
        java.util.List<Map<String, Object>> items = (java.util.List<Map<String, Object>>) artists.get("items");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> artist = items.get(i);
                String name = (String) artist.get("name");
                String externalUrl = ((Map<String, Object>) artist.get("external_urls")).get("spotify").toString();
                java.util.List<String> genres = (java.util.List<String>) artist.get("genres");
                String genreStr = genres != null && !genres.isEmpty() ? String.join(", ", genres) : "未知风格";
                
                result.append(String.format("%d. %s (风格: %s)\n", i + 1, name, genreStr));
                result.append(String.format("   Spotify链接: %s\n", externalUrl));
            }
        }
    }
    
    private void formatAlbums(Map<String, Object> albums, StringBuilder result) {
        java.util.List<Map<String, Object>> items = (java.util.List<Map<String, Object>>) albums.get("items");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> album = items.get(i);
                String name = (String) album.get("name");
                java.util.List<Map<String, Object>> artists = (java.util.List<Map<String, Object>>) album.get("artists");
                String artistName = artists != null && !artists.isEmpty() ? (String) artists.get(0).get("name") : "未知艺术家";
                String releaseDate = (String) album.get("release_date");
                String externalUrl = ((Map<String, Object>) album.get("external_urls")).get("spotify").toString();
                
                result.append(String.format("%d. %s - %s (发行日期: %s)\n", 
                        i + 1, name, artistName, releaseDate));
                result.append(String.format("   Spotify链接: %s\n", externalUrl));
            }
        }
    }
    
    private void formatPlaylists(Map<String, Object> playlists, StringBuilder result) {
        java.util.List<Map<String, Object>> items = (java.util.List<Map<String, Object>>) playlists.get("items");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> playlist = items.get(i);
                String name = (String) playlist.get("name");
                String description = (String) playlist.get("description");
                Map<String, Object> owner = (Map<String, Object>) playlist.get("owner");
                String ownerName = owner != null ? (String) owner.get("display_name") : "未知用户";
                String externalUrl = ((Map<String, Object>) playlist.get("external_urls")).get("spotify").toString();
                
                result.append(String.format("%d. %s (创建者: %s)\n", i + 1, name, ownerName));
                if (StringUtils.isNotBlank(description)) {
                    result.append(String.format("   描述: %s\n", description));
                }
                result.append(String.format("   Spotify链接: %s\n", externalUrl));
            }
        }
    }
}
