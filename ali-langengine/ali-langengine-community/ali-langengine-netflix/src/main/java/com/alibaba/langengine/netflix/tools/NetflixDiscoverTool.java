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
public class NetflixDiscoverTool extends BaseTool {
    
    private final NetflixConfiguration config;
    private final OkHttpClient httpClient;
    
    public NetflixDiscoverTool() {
        this(new NetflixConfiguration());
    }
    
    public NetflixDiscoverTool(NetflixConfiguration config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
        
        setName("netflix_discover");
        setHumanName("Netflix内容发现工具");
        setDescription("根据条件发现Netflix电影和电视剧");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"type\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"内容类型：movie(电影), tv(电视剧)\",\n" +
                "      \"default\": \"movie\"\n" +
                "    },\n" +
                "    \"genre\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"类型ID，如28(action), 35(comedy), 18(drama)\"\n" +
                "    },\n" +
                "    \"year\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"发行年份\"\n" +
                "    },\n" +
                "    \"minYear\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"最小发行年份\"\n" +
                "    },\n" +
                "    \"maxYear\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"最大发行年份\"\n" +
                "    },\n" +
                "    \"minRating\": {\n" +
                "      \"type\": \"number\",\n" +
                "      \"description\": \"最小评分(0-10)\"\n" +
                "    },\n" +
                "    \"sortBy\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"排序方式：popularity.desc, vote_average.desc, release_date.desc\",\n" +
                "      \"default\": \"popularity.desc\"\n" +
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
            String type = (String) params.getOrDefault("type", "movie");
            String genre = (String) params.get("genre");
            Integer year = (Integer) params.get("year");
            Integer minYear = (Integer) params.get("minYear");
            Integer maxYear = (Integer) params.get("maxYear");
            Double minRating = (Double) params.get("minRating");
            String sortBy = (String) params.getOrDefault("sortBy", "popularity.desc");
            Integer page = (Integer) params.getOrDefault("page", config.getDefaultPage());
            String language = (String) params.getOrDefault("language", config.getDefaultLanguage());
            String region = (String) params.getOrDefault("region", config.getDefaultRegion());
            
            if (StringUtils.isBlank(config.getApiKey())) {
                return new ToolExecuteResult("错误：Netflix API密钥未配置，请设置netflix.api.key系统属性");
            }
            
            // 发现内容
            String discoverResult = discoverContent(type, genre, year, minYear, maxYear, minRating, sortBy, page, language, region);
            
            ToolExecuteResult toolResult = new ToolExecuteResult(discoverResult);
            onToolEnd(this, toolInput, toolResult, executionContext);
            return toolResult;
            
        } catch (Exception e) {
            log.error("Netflix内容发现异常", e);
            return new ToolExecuteResult("内容发现异常: " + e.getMessage());
        }
    }
    
    private String discoverContent(String type, String genre, Integer year, Integer minYear, Integer maxYear, 
                                 Double minRating, String sortBy, int page, String language, String region) {
        try {
            StringBuilder urlBuilder = new StringBuilder(config.getDiscoverUrl());
            urlBuilder.append("?api_key=").append(config.getApiKey());
            urlBuilder.append("&page=").append(page);
            urlBuilder.append("&language=").append(language);
            urlBuilder.append("&region=").append(region);
            urlBuilder.append("&sort_by=").append(sortBy);
            
            if (StringUtils.isNotBlank(genre)) {
                urlBuilder.append("&with_genres=").append(genre);
            }
            
            if (year != null) {
                urlBuilder.append("&year=").append(year);
            } else {
                if (minYear != null) {
                    urlBuilder.append("&primary_release_date.gte=").append(minYear).append("-01-01");
                }
                if (maxYear != null) {
                    urlBuilder.append("&primary_release_date.lte=").append(maxYear).append("-12-31");
                }
            }
            
            if (minRating != null) {
                urlBuilder.append("&vote_average.gte=").append(minRating);
            }
            
            Request request = new Request.Builder()
                    .url(urlBuilder.toString())
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return formatDiscoverResults(responseBody, type);
                } else {
                    return "内容发现失败: HTTP " + response.code() + " - " + response.message();
                }
            }
        } catch (IOException e) {
            log.error("执行Netflix内容发现失败", e);
            return "内容发现异常: " + e.getMessage();
        }
    }
    
    private String formatDiscoverResults(String responseBody, String type) {
        try {
            Map<String, Object> discoverData = JSON.parseObject(responseBody, Map.class);
            StringBuilder result = new StringBuilder();
            
            String typeText = "movie".equals(type) ? "电影" : "电视剧";
            result.append(String.format("=== Netflix %s发现结果 ===\n", typeText));
            
            java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) discoverData.get("results");
            if (results != null && !results.isEmpty()) {
                for (int i = 0; i < Math.min(results.size(), 20); i++) {
                    Map<String, Object> item = results.get(i);
                    
                    if ("movie".equals(type)) {
                        formatMovieItem(item, i + 1, result);
                    } else {
                        formatTvItem(item, i + 1, result);
                    }
                }
            } else {
                result.append("未找到符合条件的内容。\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("格式化发现结果失败", e);
            return "发现结果格式化失败: " + e.getMessage();
        }
    }
    
    private void formatMovieItem(Map<String, Object> movie, int index, StringBuilder result) {
        String title = (String) movie.get("title");
        String overview = (String) movie.get("overview");
        String releaseDate = (String) movie.get("release_date");
        Double voteAverage = (Double) movie.get("vote_average");
        Integer voteCount = (Integer) movie.get("vote_count");
        String posterPath = (String) movie.get("poster_path");
        
        result.append(String.format("%d. %s", index, title != null ? title : "未知标题"));
        if (releaseDate != null) {
            result.append(String.format(" (%s)", releaseDate.substring(0, 4)));
        }
        if (voteAverage != null) {
            result.append(String.format(" - 评分: %.1f/10", voteAverage));
        }
        if (voteCount != null) {
            result.append(String.format(" (%d票)", voteCount));
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
        Integer voteCount = (Integer) tvShow.get("vote_count");
        String posterPath = (String) tvShow.get("poster_path");
        
        result.append(String.format("%d. %s", index, name != null ? name : "未知标题"));
        if (firstAirDate != null) {
            result.append(String.format(" (%s)", firstAirDate.substring(0, 4)));
        }
        if (voteAverage != null) {
            result.append(String.format(" - 评分: %.1f/10", voteAverage));
        }
        if (voteCount != null) {
            result.append(String.format(" (%d票)", voteCount));
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
