package com.alibaba.langengine.netflix.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.netflix.NetflixConfiguration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NetflixTrendingTool extends BaseTool {
    
    private final NetflixConfiguration config;
    private final OkHttpClient httpClient;
    
    public NetflixTrendingTool() {
        this(new NetflixConfiguration());
    }
    
    public NetflixTrendingTool(NetflixConfiguration config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
        
        setName("netflix_trending");
        setHumanName("Netflix热门内容工具");
        setDescription("获取Netflix热门电影和电视剧");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"timeWindow\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"时间窗口：day(今日热门), week(本周热门)\",\n" +
                "      \"default\": \"day\"\n" +
                "    },\n" +
                "    \"type\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"内容类型：all(全部), movie(电影), tv(电视剧), person(人物)\",\n" +
                "      \"default\": \"all\"\n" +
                "    },\n" +
                "    \"page\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"页码，从1开始\",\n" +
                "      \"default\": 1\n" +
                "    },\n" +
                "    \"language\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"语言代码，如zh-CN, en-US\",\n" +
                "      \"default\": \"zh-CN\"\n" +
                "    },\n" +
                "    \"region\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"地区代码，如CN, US\",\n" +
                "      \"default\": \"CN\"\n" +
                "    }\n" +
                "  }\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);
            
            Map<String, Object> params = JSON.parseObject(toolInput, Map.class);
            String timeWindow = (String) params.getOrDefault("timeWindow", "day");
            String type = (String) params.getOrDefault("type", "all");
            Integer page = (Integer) params.getOrDefault("page", config.getDefaultPage());
            String language = (String) params.getOrDefault("language", config.getDefaultLanguage());
            String region = (String) params.getOrDefault("region", config.getDefaultRegion());
            
            if (StringUtils.isBlank(config.getApiKey())) {
                return new ToolExecuteResult("错误：Netflix API密钥未配置，请设置netflix.api.key系统属性");
            }
            
            // 获取热门内容
            String trendingResult = getTrendingContent(timeWindow, type, page, language, region);
            
            ToolExecuteResult toolResult = new ToolExecuteResult(trendingResult);
            onToolEnd(this, toolInput, toolResult, executionContext);
            return toolResult;
            
        } catch (Exception e) {
            log.error("Netflix热门内容获取异常", e);
            return new ToolExecuteResult("获取热门内容异常: " + e.getMessage());
        }
    }
    
    private String getTrendingContent(String timeWindow, String type, int page, String language, String region) {
        try {
            StringBuilder urlBuilder = new StringBuilder(config.getTrendingUrl());
            urlBuilder.append("?api_key=").append(config.getApiKey());
            urlBuilder.append("&page=").append(page);
            urlBuilder.append("&language=").append(language);
            urlBuilder.append("&region=").append(region);
            
            Request request = new Request.Builder()
                    .url(urlBuilder.toString())
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return formatTrendingResults(responseBody, timeWindow, type);
                } else {
                    return "获取热门内容失败: HTTP " + response.code() + " - " + response.message();
                }
            }
        } catch (IOException e) {
            log.error("获取Netflix热门内容失败", e);
            return "获取热门内容异常: " + e.getMessage();
        }
    }
    
    private String formatTrendingResults(String responseBody, String timeWindow, String type) {
        try {
            Map<String, Object> trendingData = JSON.parseObject(responseBody, Map.class);
            StringBuilder result = new StringBuilder();
            
            String timeWindowText = "day".equals(timeWindow) ? "今日" : "本周";
            result.append(String.format("=== Netflix %s热门内容 ===\n", timeWindowText));
            
            java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) trendingData.get("results");
            if (results != null && !results.isEmpty()) {
                for (int i = 0; i < Math.min(results.size(), 20); i++) {
                    Map<String, Object> item = results.get(i);
                    String mediaType = (String) item.get("media_type");
                    
                    if ("movie".equals(mediaType) && (type.equals("all") || type.equals("movie"))) {
                        formatMovieItem(item, i + 1, result);
                    } else if ("tv".equals(mediaType) && (type.equals("all") || type.equals("tv"))) {
                        formatTvItem(item, i + 1, result);
                    } else if ("person".equals(mediaType) && (type.equals("all") || type.equals("person"))) {
                        formatPersonItem(item, i + 1, result);
                    }
                }
            }
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("格式化热门内容结果失败", e);
            return "热门内容结果格式化失败: " + e.getMessage();
        }
    }
    
    private void formatMovieItem(Map<String, Object> movie, int index, StringBuilder result) {
        String title = (String) movie.get("title");
        String overview = (String) movie.get("overview");
        String releaseDate = (String) movie.get("release_date");
        Double voteAverage = (Double) movie.get("vote_average");
        String posterPath = (String) movie.get("poster_path");
        
        result.append(String.format("%d. [电影] %s", index, title != null ? title : "未知标题"));
        if (releaseDate != null) {
            result.append(String.format(" (%s)", releaseDate.substring(0, 4)));
        }
        if (voteAverage != null) {
            result.append(String.format(" - 评分: %.1f/10", voteAverage));
        }
        result.append("\n");
        
        if (StringUtils.isNotBlank(overview)) {
            result.append(String.format("   简介: %s\n", 
                    overview.length() > 100 ? overview.substring(0, 100) + "..." : overview));
        }
        
        if (StringUtils.isNotBlank(posterPath)) {
            result.append(String.format("   海报: https://image.tmdb.org/t/p/w500%s\n", posterPath));
        }
        result.append("\n");
    }
    
    private void formatTvItem(Map<String, Object> tvShow, int index, StringBuilder result) {
        String name = (String) tvShow.get("name");
        String overview = (String) tvShow.get("overview");
        String firstAirDate = (String) tvShow.get("first_air_date");
        Double voteAverage = (Double) tvShow.get("vote_average");
        String posterPath = (String) tvShow.get("poster_path");
        
        result.append(String.format("%d. [电视剧] %s", index, name != null ? name : "未知标题"));
        if (firstAirDate != null) {
            result.append(String.format(" (%s)", firstAirDate.substring(0, 4)));
        }
        if (voteAverage != null) {
            result.append(String.format(" - 评分: %.1f/10", voteAverage));
        }
        result.append("\n");
        
        if (StringUtils.isNotBlank(overview)) {
            result.append(String.format("   简介: %s\n", 
                    overview.length() > 100 ? overview.substring(0, 100) + "..." : overview));
        }
        
        if (StringUtils.isNotBlank(posterPath)) {
            result.append(String.format("   海报: https://image.tmdb.org/t/p/w500%s\n", posterPath));
        }
        result.append("\n");
    }
    
    private void formatPersonItem(Map<String, Object> person, int index, StringBuilder result) {
        String name = (String) person.get("name");
        String knownForDepartment = (String) person.get("known_for_department");
        Double popularity = (Double) person.get("popularity");
        String profilePath = (String) person.get("profile_path");
        
        result.append(String.format("%d. [人物] %s", index, name != null ? name : "未知姓名"));
        if (StringUtils.isNotBlank(knownForDepartment)) {
            result.append(String.format(" (%s)", knownForDepartment));
        }
        if (popularity != null) {
            result.append(String.format(" - 热度: %.1f", popularity));
        }
        result.append("\n");
        
        if (StringUtils.isNotBlank(profilePath)) {
            result.append(String.format("   头像: https://image.tmdb.org/t/p/w500%s\n", profilePath));
        }
        result.append("\n");
    }
}
