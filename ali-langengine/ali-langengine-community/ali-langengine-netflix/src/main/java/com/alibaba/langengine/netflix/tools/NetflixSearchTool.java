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
public class NetflixSearchTool extends BaseTool {
    
    private final NetflixConfiguration config;
    private final OkHttpClient httpClient;
    
    public NetflixSearchTool() {
        this(new NetflixConfiguration());
    }
    
    public NetflixSearchTool(NetflixConfiguration config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
        
        setName("netflix_search");
        setHumanName("Netflix影视搜索工具");
        setDescription("搜索Netflix电影、电视剧和纪录片");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"搜索关键词\"\n" +
                "    },\n" +
                "    \"type\": {\n" +
                "    \"type\": \"string\",\n" +
                "      \"description\": \"搜索类型：movie(电影), tv(电视剧), person(人物)，多个用逗号分隔\",\n" +
                "      \"default\": \"movie,tv\"\n" +
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
                "    },\n" +
                "    \"year\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"发行年份\"\n" +
                "    },\n" +
                "    \"genre\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"类型，如action, comedy, drama\"\n" +
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
            String type = (String) params.getOrDefault("type", "movie,tv");
            Integer page = (Integer) params.getOrDefault("page", config.getDefaultPage());
            String language = (String) params.getOrDefault("language", config.getDefaultLanguage());
            String region = (String) params.getOrDefault("region", config.getDefaultRegion());
            Integer year = (Integer) params.get("year");
            String genre = (String) params.get("genre");
            
            if (StringUtils.isBlank(query)) {
                return new ToolExecuteResult("错误：搜索关键词不能为空");
            }
            
            if (StringUtils.isBlank(config.getApiKey())) {
                return new ToolExecuteResult("错误：Netflix API密钥未配置，请设置netflix.api.key系统属性");
            }
            
            // 执行搜索
            String searchResult = performSearch(query, type, page, language, region, year, genre);
            
            ToolExecuteResult toolResult = new ToolExecuteResult(searchResult);
            onToolEnd(this, toolInput, toolResult, executionContext);
            return toolResult;
            
        } catch (Exception e) {
            log.error("Netflix搜索异常", e);
            return new ToolExecuteResult("搜索异常: " + e.getMessage());
        }
    }
    
    private String performSearch(String query, String type, int page, String language, String region, Integer year, String genre) {
        try {
            StringBuilder urlBuilder = new StringBuilder(config.getSearchUrl());
            urlBuilder.append("?api_key=").append(config.getApiKey());
            urlBuilder.append("&query=").append(java.net.URLEncoder.encode(query, "UTF-8"));
            urlBuilder.append("&page=").append(page);
            urlBuilder.append("&language=").append(language);
            urlBuilder.append("&region=").append(region);
            
            if (year != null) {
                urlBuilder.append("&year=").append(year);
            }
            
            if (StringUtils.isNotBlank(genre)) {
                urlBuilder.append("&with_genres=").append(java.net.URLEncoder.encode(genre, "UTF-8"));
            }
            
            Request request = new Request.Builder()
                    .url(urlBuilder.toString())
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
            log.error("执行Netflix搜索失败", e);
            return "搜索异常: " + e.getMessage();
        }
    }
    
    private String formatSearchResults(String responseBody, String type) {
        try {
            Map<String, Object> searchData = JSON.parseObject(responseBody, Map.class);
            StringBuilder result = new StringBuilder();
            
            // 处理电影结果
            if (type.contains("movie")) {
                java.util.List<Map<String, Object>> movies = (java.util.List<Map<String, Object>>) searchData.get("results");
                if (movies != null && !movies.isEmpty()) {
                    result.append("=== 电影搜索结果 ===\n");
                    formatMovies(movies, result);
                }
            }
            
            // 处理电视剧结果
            if (type.contains("tv")) {
                java.util.List<Map<String, Object>> tvShows = (java.util.List<Map<String, Object>>) searchData.get("results");
                if (tvShows != null && !tvShows.isEmpty()) {
                    result.append("\n=== 电视剧搜索结果 ===\n");
                    formatTvShows(tvShows, result);
                }
            }
            
            // 处理人物结果
            if (type.contains("person")) {
                java.util.List<Map<String, Object>> people = (java.util.List<Map<String, Object>>) searchData.get("results");
                if (people != null && !people.isEmpty()) {
                    result.append("\n=== 人物搜索结果 ===\n");
                    formatPeople(people, result);
                }
            }
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("格式化搜索结果失败", e);
            return "搜索结果格式化失败: " + e.getMessage();
        }
    }
    
    private void formatMovies(java.util.List<Map<String, Object>> movies, StringBuilder result) {
        for (int i = 0; i < Math.min(movies.size(), 10); i++) {
            Map<String, Object> movie = movies.get(i);
            String title = (String) movie.get("title");
            String overview = (String) movie.get("overview");
            String releaseDate = (String) movie.get("release_date");
            Double voteAverage = (Double) movie.get("vote_average");
            String posterPath = (String) movie.get("poster_path");
            
            result.append(String.format("%d. %s", i + 1, title != null ? title : "未知标题"));
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
    }
    
    private void formatTvShows(java.util.List<Map<String, Object>> tvShows, StringBuilder result) {
        for (int i = 0; i < Math.min(tvShows.size(), 10); i++) {
            Map<String, Object> tvShow = tvShows.get(i);
            String name = (String) tvShow.get("name");
            String overview = (String) tvShow.get("overview");
            String firstAirDate = (String) tvShow.get("first_air_date");
            Double voteAverage = (Double) tvShow.get("vote_average");
            String posterPath = (String) tvShow.get("poster_path");
            
            result.append(String.format("%d. %s", i + 1, name != null ? name : "未知标题"));
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
    }
    
    private void formatPeople(java.util.List<Map<String, Object>> people, StringBuilder result) {
        for (int i = 0; i < Math.min(people.size(), 10); i++) {
            Map<String, Object> person = people.get(i);
            String name = (String) person.get("name");
            String knownForDepartment = (String) person.get("known_for_department");
            Double popularity = (Double) person.get("popularity");
            String profilePath = (String) person.get("profile_path");
            
            result.append(String.format("%d. %s", i + 1, name != null ? name : "未知姓名"));
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
}
