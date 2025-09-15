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
package com.alibaba.langengine.youtube.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * YouTube 搜索响应模型
 * 封装搜索结果和元数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeSearchResponse {
    
    /**
     * 搜索查询
     */
    private String query;
    
    /**
     * 搜索结果列表
     */
    private List<YouTubeVideo> videos;
    
    /**
     * 总结果数
     */
    private Long totalResults;
    
    /**
     * 返回结果数
     */
    private Integer returnedCount;
    
    /**
     * 搜索耗时（毫秒）
     */
    private Long searchDuration;
    
    /**
     * 是否成功
     */
    private Boolean successful;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 搜索建议
     */
    private List<String> suggestions;
    
    /**
     * 相关搜索
     */
    private List<String> relatedQueries;
    
    /**
     * 搜索URL
     */
    private String searchUrl;
    
    /**
     * 是否有更多结果
     */
    private Boolean hasMoreResults;
    
    /**
     * 下一页URL
     */
    private String nextPageUrl;
    
    /**
     * 搜索时间戳
     */
    private Long timestamp;
    
    /**
     * 构造函数
     */
    public YouTubeSearchResponse(String query) {
        this.query = query;
        this.videos = new ArrayList<>();
        this.suggestions = new ArrayList<>();
        this.relatedQueries = new ArrayList<>();
        this.successful = true;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 添加视频
     */
    public void addVideo(YouTubeVideo video) {
        if (videos == null) {
            videos = new ArrayList<>();
        }
        if (video != null && video.isValid()) {
            videos.add(video);
        }
    }
    
    /**
     * 添加搜索建议
     */
    public void addSuggestion(String suggestion) {
        if (suggestions == null) {
            suggestions = new ArrayList<>();
        }
        if (suggestion != null && !suggestion.trim().isEmpty()) {
            suggestions.add(suggestion.trim());
        }
    }
    
    /**
     * 添加相关搜索
     */
    public void addRelatedQuery(String query) {
        if (relatedQueries == null) {
            relatedQueries = new ArrayList<>();
        }
        if (query != null && !query.trim().isEmpty()) {
            relatedQueries.add(query.trim());
        }
    }
    
    /**
     * 检查是否为空结果
     */
    public boolean isEmpty() {
        return videos == null || videos.isEmpty();
    }
    
    /**
     * 检查是否成功
     */
    public boolean isSuccessful() {
        return successful != null && successful;
    }
    
    /**
     * 设置错误信息
     */
    public void setError(String errorMessage) {
        this.errorMessage = errorMessage;
        this.successful = false;
    }
    
    /**
     * 获取结果摘要
     */
    public String getSummary() {
        if (!isSuccessful()) {
            return "搜索失败: " + (errorMessage != null ? errorMessage : "未知错误");
        }
        
        if (isEmpty()) {
            return "未找到相关视频: " + query;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(returnedCount != null ? returnedCount : videos.size());
        
        if (totalResults != null && totalResults > 0) {
            sb.append(" 个视频（共 ").append(totalResults).append(" 个）");
        } else {
            sb.append(" 个视频");
        }
        
        if (searchDuration != null) {
            sb.append("，耗时 ").append(searchDuration).append(" 毫秒");
        }
        
        return sb.toString();
    }
    
    /**
     * 获取格式化结果
     */
    public String getFormattedResults() {
        if (!isSuccessful()) {
            return "搜索失败: " + (errorMessage != null ? errorMessage : "未知错误");
        }
        
        if (isEmpty()) {
            return "未找到相关视频: " + query;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== YouTube 搜索结果 ===\n");
        sb.append(getSummary()).append("\n\n");
        
        for (int i = 0; i < videos.size(); i++) {
            YouTubeVideo video = videos.get(i);
            sb.append("[").append(i + 1).append("] ").append(video.getTitle()).append("\n");
            
            if (video.getChannelName() != null && !video.getChannelName().trim().isEmpty()) {
                sb.append("频道: ").append(video.getChannelName()).append("\n");
            }
            
            if (video.getDuration() != null && video.getDuration() > 0) {
                sb.append("时长: ").append(video.getFormattedDuration());
            }
            
            if (video.getViewCount() != null && video.getViewCount() > 0) {
                sb.append(" | 观看: ").append(video.getFormattedViewCount());
            }
            
            if (video.getUploadDate() != null) {
                sb.append(" | 上传: ").append(video.getUploadDate().toLocalDate());
            }
            
            sb.append("\n");
            
            if (video.getDescription() != null && !video.getDescription().trim().isEmpty()) {
                sb.append("描述: ").append(video.getShortDescription()).append("\n");
            }
            
            if (video.getVideoUrl() != null && !video.getVideoUrl().trim().isEmpty()) {
                sb.append("视频链接: ").append(video.getVideoUrl()).append("\n");
            }
            
            if (video.getChannelUrl() != null && !video.getChannelUrl().trim().isEmpty()) {
                sb.append("频道链接: ").append(video.getChannelUrl()).append("\n");
            }
            
            sb.append("\n");
        }
        
        // 添加搜索建议
        if (suggestions != null && !suggestions.isEmpty()) {
            sb.append("搜索建议:\n");
            for (String suggestion : suggestions) {
                sb.append("- ").append(suggestion).append("\n");
            }
            sb.append("\n");
        }
        
        // 添加相关搜索
        if (relatedQueries != null && !relatedQueries.isEmpty()) {
            sb.append("相关搜索:\n");
            for (String query : relatedQueries) {
                sb.append("- ").append(query).append("\n");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 获取简化的结果列表
     */
    public String getSimpleResults() {
        if (!isSuccessful()) {
            return "搜索失败: " + (errorMessage != null ? errorMessage : "未知错误");
        }
        
        if (isEmpty()) {
            return "未找到相关视频: " + query;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(returnedCount != null ? returnedCount : videos.size()).append(" 个视频:\n\n");
        
        for (int i = 0; i < videos.size(); i++) {
            YouTubeVideo video = videos.get(i);
            sb.append("[").append(i + 1).append("] ").append(video.getTitle()).append("\n");
            
            if (video.getChannelName() != null && !video.getChannelName().trim().isEmpty()) {
                sb.append("    频道: ").append(video.getChannelName());
            }
            
            if (video.getDuration() != null && video.getDuration() > 0) {
                sb.append(" | 时长: ").append(video.getFormattedDuration());
            }
            
            if (video.getViewCount() != null && video.getViewCount() > 0) {
                sb.append(" | 观看: ").append(video.getFormattedViewCount());
            }
            
            if (video.getUploadDate() != null) {
                sb.append(" | 上传: ").append(video.getUploadDate().toLocalDate());
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 获取JSON格式结果
     */
    public String getJsonResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"query\": \"").append(query != null ? query : "").append("\",\n");
        sb.append("  \"successful\": ").append(isSuccessful()).append(",\n");
        sb.append("  \"totalResults\": ").append(totalResults != null ? totalResults : 0).append(",\n");
        sb.append("  \"returnedCount\": ").append(returnedCount != null ? returnedCount : 0).append(",\n");
        sb.append("  \"searchDuration\": ").append(searchDuration != null ? searchDuration : 0).append(",\n");
        
        if (errorMessage != null) {
            sb.append("  \"errorMessage\": \"").append(errorMessage).append("\",\n");
        }
        
        sb.append("  \"videos\": [\n");
        
        if (videos != null && !videos.isEmpty()) {
            for (int i = 0; i < videos.size(); i++) {
                YouTubeVideo video = videos.get(i);
                sb.append("    {\n");
                sb.append("      \"videoId\": \"").append(video.getVideoId() != null ? video.getVideoId() : "").append("\",\n");
                sb.append("      \"title\": \"").append(video.getTitle() != null ? video.getTitle() : "").append("\",\n");
                sb.append("      \"channelName\": \"").append(video.getChannelName() != null ? video.getChannelName() : "").append("\",\n");
                sb.append("      \"duration\": ").append(video.getDuration() != null ? video.getDuration() : 0).append(",\n");
                sb.append("      \"viewCount\": ").append(video.getViewCount() != null ? video.getViewCount() : 0).append(",\n");
                sb.append("      \"likeCount\": ").append(video.getLikeCount() != null ? video.getLikeCount() : 0).append(",\n");
                sb.append("      \"videoUrl\": \"").append(video.getVideoUrl() != null ? video.getVideoUrl() : "").append("\",\n");
                sb.append("      \"thumbnailUrl\": \"").append(video.getThumbnailUrl() != null ? video.getThumbnailUrl() : "").append("\"\n");
                
                if (i < videos.size() - 1) {
                    sb.append("    },\n");
                } else {
                    sb.append("    }\n");
                }
            }
        }
        
        sb.append("  ]\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
    /**
     * 获取按观看次数排序的结果
     */
    public String getTopViewedResults(int limit) {
        if (!isSuccessful() || isEmpty()) {
            return getSimpleResults();
        }
        
        List<YouTubeVideo> sortedVideos = new ArrayList<>(videos);
        sortedVideos.sort((v1, v2) -> {
            Long views1 = v1.getViewCount() != null ? v1.getViewCount() : 0L;
            Long views2 = v2.getViewCount() != null ? v2.getViewCount() : 0L;
            return views2.compareTo(views1); // 降序排列
        });
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== 热门视频（按观看次数排序）===\n\n");
        
        int count = Math.min(limit, sortedVideos.size());
        for (int i = 0; i < count; i++) {
            YouTubeVideo video = sortedVideos.get(i);
            sb.append("[").append(i + 1).append("] ").append(video.getTitle()).append("\n");
            sb.append("    频道: ").append(video.getChannelName() != null ? video.getChannelName() : "未知");
            sb.append(" | 观看: ").append(video.getFormattedViewCount());
            sb.append(" | 时长: ").append(video.getFormattedDuration());
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 获取按时长分类的结果
     */
    public String getResultsByDuration() {
        if (!isSuccessful() || isEmpty()) {
            return getSimpleResults();
        }
        
        List<YouTubeVideo> shortVideos = new ArrayList<>();
        List<YouTubeVideo> mediumVideos = new ArrayList<>();
        List<YouTubeVideo> longVideos = new ArrayList<>();
        
        for (YouTubeVideo video : videos) {
            if (video.getDuration() != null) {
                if (video.getDuration() < 240) { // 少于4分钟
                    shortVideos.add(video);
                } else if (video.getDuration() < 1200) { // 4-20分钟
                    mediumVideos.add(video);
                } else { // 超过20分钟
                    longVideos.add(video);
                }
            }
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== 按时长分类的视频 ===\n\n");
        
        if (!shortVideos.isEmpty()) {
            sb.append("短视频（<4分钟）: ").append(shortVideos.size()).append(" 个\n");
        }
        
        if (!mediumVideos.isEmpty()) {
            sb.append("中等视频（4-20分钟）: ").append(mediumVideos.size()).append(" 个\n");
        }
        
        if (!longVideos.isEmpty()) {
            sb.append("长视频（>20分钟）: ").append(longVideos.size()).append(" 个\n");
        }
        
        return sb.toString();
    }
}
