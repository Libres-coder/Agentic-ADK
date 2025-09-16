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
package com.alibaba.langengine.youtube;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.model.BaseTool;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.alibaba.langengine.youtube.model.YouTubeSearchRequest;
import com.alibaba.langengine.youtube.model.YouTubeSearchResponse;
import com.alibaba.langengine.youtube.sdk.YouTubeClient;
import com.alibaba.langengine.youtube.sdk.YouTubeException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * YouTube 搜索工具
 * 提供视频搜索功能
 */
@Slf4j
public class YouTubeSearchTool extends BaseTool {
    
    private YouTubeClient client;
    
    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"query\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(required) The search query for YouTube videos\"\n" +
            "\t\t},\n" +
            "\t\t\"count\": {\n" +
            "\t\t\t\"type\": \"integer\",\n" +
            "\t\t\t\"description\": \"(optional) The number of results to return. Default is 10, max is 50.\",\n" +
            "\t\t\t\"default\": 10,\n" +
            "\t\t\t\"minimum\": 1,\n" +
            "\t\t\t\"maximum\": 50\n" +
            "\t\t},\n" +
            "\t\t\"sortBy\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Sort results by relevance, date, rating, viewCount, or title. Default is relevance.\",\n" +
            "\t\t\t\"enum\": [\"relevance\", \"date\", \"rating\", \"viewCount\", \"title\"],\n" +
            "\t\t\t\"default\": \"relevance\"\n" +
            "\t\t},\n" +
            "\t\t\"timeRange\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Time range filter: any, hour, today, week, month, year. Default is any.\",\n" +
            "\t\t\t\"enum\": [\"any\", \"hour\", \"today\", \"week\", \"month\", \"year\"],\n" +
            "\t\t\t\"default\": \"any\"\n" +
            "\t\t},\n" +
            "\t\t\"videoType\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Video type filter: any, video, channel, playlist, movie, episode. Default is any.\",\n" +
            "\t\t\t\"enum\": [\"any\", \"video\", \"channel\", \"playlist\", \"movie\", \"episode\"],\n" +
            "\t\t\t\"default\": \"any\"\n" +
            "\t\t},\n" +
            "\t\t\"duration\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Video duration filter: any, short (<4min), medium (4-20min), long (>20min). Default is any.\",\n" +
            "\t\t\t\"enum\": [\"any\", \"short\", \"medium\", \"long\"],\n" +
            "\t\t\t\"default\": \"any\"\n" +
            "\t\t},\n" +
            "\t\t\"quality\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Video quality filter: any, hd, 4k, 8k. Default is any.\",\n" +
            "\t\t\t\"enum\": [\"any\", \"hd\", \"4k\", \"8k\"],\n" +
            "\t\t\t\"default\": \"any\"\n" +
            "\t\t},\n" +
            "\t\t\"channel\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Filter by specific channel name\"\n" +
            "\t\t},\n" +
            "\t\t\"language\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Language filter (e.g., en, zh, fr, de). Default is en.\",\n" +
            "\t\t\t\"default\": \"en\"\n" +
            "\t\t},\n" +
            "\t\t\"country\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Country/region filter (e.g., US, CN, UK, DE). Default is US.\",\n" +
            "\t\t\t\"default\": \"US\"\n" +
            "\t\t},\n" +
            "\t\t\"includeLive\": {\n" +
            "\t\t\t\"type\": \"boolean\",\n" +
            "\t\t\t\"description\": \"(optional) Include live streams in results. Default is false.\",\n" +
            "\t\t\t\"default\": false\n" +
            "\t\t},\n" +
            "\t\t\"includeSubtitles\": {\n" +
            "\t\t\t\"type\": \"boolean\",\n" +
            "\t\t\t\"description\": \"(optional) Include only videos with subtitles. Default is false.\",\n" +
            "\t\t\t\"default\": false\n" +
            "\t\t},\n" +
            "\t\t\"includeHD\": {\n" +
            "\t\t\t\"type\": \"boolean\",\n" +
            "\t\t\t\"description\": \"(optional) Include only HD videos. Default is false.\",\n" +
            "\t\t\t\"default\": false\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"query\"]\n" +
            "}";
    
    public YouTubeSearchTool() {
        setName("youtube_search");
        setDescription("Search for videos on YouTube. " +
                "Use this tool when you need to find videos, channels, playlists, or live streams. " +
                "The tool returns detailed information including title, channel, duration, view count, upload date, description, and video links.");
        
        setParameters(PARAMETERS);
        
        // 初始化客户端
        this.client = new YouTubeClient();
    }
    
    /**
     * 自定义构造函数
     */
    public YouTubeSearchTool(YouTubeClient client) {
        setName("youtube_search");
        setDescription("Search for videos on YouTube. " +
                "Use this tool when you need to find videos, channels, playlists, or live streams. " +
                "The tool returns detailed information including title, channel, duration, view count, upload date, description, and video links.");
        
        setParameters(PARAMETERS);
        this.client = client;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("YouTube 搜索开始，输入: {}", toolInput);
        
        try {
            // 解析输入参数
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            
            // 验证必需参数
            if (!inputMap.containsKey("query") || inputMap.get("query") == null) {
                return new ToolExecuteResult("错误: 缺少必需的查询参数 'query'");
            }
            
            // 构建搜索请求
            YouTubeSearchRequest request = buildSearchRequest(inputMap);
            
            // 执行搜索
            YouTubeSearchResponse response = client.search(request);
            
            // 格式化结果
            String result = formatSearchResults(response);
            
            log.info("YouTube 搜索完成，返回 {} 个结果", response.getReturnedCount());
            
            return new ToolExecuteResult(result);
            
        } catch (YouTubeException e) {
            log.error("YouTube 搜索失败: {}", e.getMessage(), e);
            return new ToolExecuteResult("YouTube 搜索失败: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("YouTube 搜索过程中发生未知错误: {}", e.getMessage(), e);
            return new ToolExecuteResult("搜索过程中发生未知错误，请稍后重试");
        }
    }
    
    /**
     * 构建搜索请求
     */
    private YouTubeSearchRequest buildSearchRequest(Map<String, Object> inputMap) {
        YouTubeSearchRequest request = new YouTubeSearchRequest(
                inputMap.get("query").toString()
        );
        
        // 设置可选参数
        if (inputMap.containsKey("count")) {
            try {
                int count = Integer.parseInt(inputMap.get("count").toString());
                request.setCount(count);
            } catch (NumberFormatException e) {
                log.warn("Invalid count parameter: {}", inputMap.get("count"));
            }
        }
        
        if (inputMap.containsKey("sortBy")) {
            request.setSortBy(inputMap.get("sortBy").toString());
        }
        
        if (inputMap.containsKey("timeRange")) {
            request.setTimeRange(inputMap.get("timeRange").toString());
        }
        
        if (inputMap.containsKey("videoType")) {
            request.setVideoType(inputMap.get("videoType").toString());
        }
        
        if (inputMap.containsKey("duration")) {
            request.setDuration(inputMap.get("duration").toString());
        }
        
        if (inputMap.containsKey("quality")) {
            request.setQuality(inputMap.get("quality").toString());
        }
        
        if (inputMap.containsKey("channel")) {
            request.setChannel(inputMap.get("channel").toString());
        }
        
        if (inputMap.containsKey("language")) {
            request.setLanguage(inputMap.get("language").toString());
        }
        
        if (inputMap.containsKey("country")) {
            request.setCountry(inputMap.get("country").toString());
        }
        
        if (inputMap.containsKey("includeLive")) {
            request.setIncludeLive(Boolean.parseBoolean(inputMap.get("includeLive").toString()));
        }
        
        if (inputMap.containsKey("includeSubtitles")) {
            request.setIncludeSubtitles(Boolean.parseBoolean(inputMap.get("includeSubtitles").toString()));
        }
        
        if (inputMap.containsKey("includeHD")) {
            request.setIncludeHD(Boolean.parseBoolean(inputMap.get("includeHD").toString()));
        }
        
        return request;
    }
    
    /**
     * 格式化搜索结果
     */
    private String formatSearchResults(YouTubeSearchResponse response) {
        if (!response.isSuccessful()) {
            return "搜索失败: " + (response.getErrorMessage() != null ? response.getErrorMessage() : "未知错误");
        }
        
        if (response.isEmpty()) {
            return "未找到相关视频: " + response.getQuery();
        }
        
        StringBuilder result = new StringBuilder();
        result.append("=== YouTube 搜索结果 ===\n");
        result.append(response.getSummary()).append("\n\n");
        
        // 添加视频列表
        for (int i = 0; i < response.getVideos().size(); i++) {
            var video = response.getVideos().get(i);
            result.append("[").append(i + 1).append("] ").append(video.getTitle()).append("\n");
            
            if (video.getChannelName() != null && !video.getChannelName().trim().isEmpty()) {
                result.append("频道: ").append(video.getChannelName());
            }
            
            if (video.getDuration() != null && video.getDuration() > 0) {
                result.append(" | 时长: ").append(video.getFormattedDuration());
            }
            
            if (video.getViewCount() != null && video.getViewCount() > 0) {
                result.append(" | 观看: ").append(video.getFormattedViewCount());
            }
            
            if (video.getUploadDate() != null) {
                result.append(" | 上传: ").append(video.getUploadDate().toLocalDate());
            }
            
            result.append("\n");
            
            if (video.getDescription() != null && !video.getDescription().trim().isEmpty()) {
                result.append("描述: ").append(video.getShortDescription()).append("\n");
            }
            
            if (video.getVideoUrl() != null && !video.getVideoUrl().trim().isEmpty()) {
                result.append("视频链接: ").append(video.getVideoUrl()).append("\n");
            }
            
            if (video.getChannelUrl() != null && !video.getChannelUrl().trim().isEmpty()) {
                result.append("频道链接: ").append(video.getChannelUrl()).append("\n");
            }
            
            result.append("\n");
        }
        
        // 添加搜索建议
        if (response.getSuggestions() != null && !response.getSuggestions().isEmpty()) {
            result.append("搜索建议:\n");
            for (String suggestion : response.getSuggestions()) {
                result.append("- ").append(suggestion).append("\n");
            }
            result.append("\n");
        }
        
        // 添加相关搜索
        if (response.getRelatedQueries() != null && !response.getRelatedQueries().isEmpty()) {
            result.append("相关搜索:\n");
            for (String query : response.getRelatedQueries()) {
                result.append("- ").append(query).append("\n");
            }
            result.append("\n");
        }
        
        return result.toString();
    }
    
    /**
     * 设置客户端（用于测试）
     */
    public void setClient(YouTubeClient client) {
        this.client = client;
    }
    
    /**
     * 获取客户端
     */
    public YouTubeClient getClient() {
        return client;
    }
}
