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

import static com.alibaba.langengine.youtube.YouTubeConfiguration.*;

/**
 * YouTube 搜索请求模型
 * 封装搜索参数和配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeSearchRequest {
    
    /**
     * 搜索查询词
     */
    private String query;
    
    /**
     * 结果数量
     */
    private Integer count;
    
    /**
     * 排序方式
     */
    private String sortBy;
    
    /**
     * 时间范围
     */
    private String timeRange;
    
    /**
     * 视频类型
     */
    private String videoType;
    
    /**
     * 视频时长
     */
    private String duration;
    
    /**
     * 视频质量
     */
    private String quality;
    
    /**
     * 频道过滤
     */
    private String channel;
    
    /**
     * 语言
     */
    private String language;
    
    /**
     * 国家/地区
     */
    private String country;
    
    /**
     * 是否包含直播
     */
    private Boolean includeLive;
    
    /**
     * 是否包含私有视频
     */
    private Boolean includePrivate;
    
    /**
     * 是否包含年龄限制内容
     */
    private Boolean includeAgeRestricted;
    
    /**
     * 是否包含字幕
     */
    private Boolean includeSubtitles;
    
    /**
     * 是否包含CC（创作共用）
     */
    private Boolean includeCC;
    
    /**
     * 是否包含HD
     */
    private Boolean includeHD;
    
    /**
     * 是否包含3D
     */
    private Boolean include3D;
    
    /**
     * 自定义参数列表
     */
    private List<String> customParams;
    
    /**
     * 构造函数
     */
    public YouTubeSearchRequest(String query) {
        this.query = query;
        this.count = getDefaultResultCount();
        this.sortBy = SUPPORTED_SORT_OPTIONS[0];
        this.timeRange = SUPPORTED_TIME_RANGES[0];
        this.videoType = SUPPORTED_VIDEO_TYPES[0];
        this.duration = SUPPORTED_DURATION_TYPES[0];
        this.quality = SUPPORTED_QUALITY_TYPES[0];
        this.language = getDefaultLanguage();
        this.country = getDefaultCountry();
        this.includeLive = false;
        this.includePrivate = false;
        this.includeAgeRestricted = false;
        this.includeSubtitles = false;
        this.includeCC = false;
        this.includeHD = false;
        this.include3D = false;
        this.customParams = new ArrayList<>();
    }
    
    /**
     * 验证请求是否有效
     */
    public boolean isValid() {
        return query != null && !query.trim().isEmpty();
    }
    
    /**
     * 获取有效的查询词
     */
    public String getValidQuery() {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return query.trim();
    }
    
    /**
     * 获取有效的结果数量
     */
    public int getValidCount() {
        return validateResultCount(count != null ? count : getDefaultResultCount());
    }
    
    /**
     * 获取有效的排序方式
     */
    public String getValidSortBy() {
        return validateSortOption(sortBy);
    }
    
    /**
     * 获取有效的时间范围
     */
    public String getValidTimeRange() {
        return validateTimeRange(timeRange);
    }
    
    /**
     * 获取有效的视频类型
     */
    public String getValidVideoType() {
        return validateVideoType(videoType);
    }
    
    /**
     * 获取有效的视频时长
     */
    public String getValidDuration() {
        return validateDurationType(duration);
    }
    
    /**
     * 获取有效的视频质量
     */
    public String getValidQuality() {
        return validateQualityType(quality);
    }
    
    /**
     * 获取有效的语言
     */
    public String getValidLanguage() {
        return language != null && !language.trim().isEmpty() ? language : getDefaultLanguage();
    }
    
    /**
     * 获取有效的国家/地区
     */
    public String getValidCountry() {
        return country != null && !country.trim().isEmpty() ? country : getDefaultCountry();
    }
    
    /**
     * 设置查询词
     */
    public YouTubeSearchRequest setQuery(String query) {
        this.query = query;
        return this;
    }
    
    /**
     * 设置结果数量
     */
    public YouTubeSearchRequest setCount(Integer count) {
        this.count = count;
        return this;
    }
    
    /**
     * 设置排序方式
     */
    public YouTubeSearchRequest setSortBy(String sortBy) {
        this.sortBy = sortBy;
        return this;
    }
    
    /**
     * 设置时间范围
     */
    public YouTubeSearchRequest setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        return this;
    }
    
    /**
     * 设置视频类型
     */
    public YouTubeSearchRequest setVideoType(String videoType) {
        this.videoType = videoType;
        return this;
    }
    
    /**
     * 设置视频时长
     */
    public YouTubeSearchRequest setDuration(String duration) {
        this.duration = duration;
        return this;
    }
    
    /**
     * 设置视频质量
     */
    public YouTubeSearchRequest setQuality(String quality) {
        this.quality = quality;
        return this;
    }
    
    /**
     * 设置频道过滤
     */
    public YouTubeSearchRequest setChannel(String channel) {
        this.channel = channel;
        return this;
    }
    
    /**
     * 设置语言
     */
    public YouTubeSearchRequest setLanguage(String language) {
        this.language = language;
        return this;
    }
    
    /**
     * 设置国家/地区
     */
    public YouTubeSearchRequest setCountry(String country) {
        this.country = country;
        return this;
    }
    
    /**
     * 设置是否包含直播
     */
    public YouTubeSearchRequest setIncludeLive(Boolean includeLive) {
        this.includeLive = includeLive;
        return this;
    }
    
    /**
     * 设置是否包含私有视频
     */
    public YouTubeSearchRequest setIncludePrivate(Boolean includePrivate) {
        this.includePrivate = includePrivate;
        return this;
    }
    
    /**
     * 设置是否包含年龄限制内容
     */
    public YouTubeSearchRequest setIncludeAgeRestricted(Boolean includeAgeRestricted) {
        this.includeAgeRestricted = includeAgeRestricted;
        return this;
    }
    
    /**
     * 设置是否包含字幕
     */
    public YouTubeSearchRequest setIncludeSubtitles(Boolean includeSubtitles) {
        this.includeSubtitles = includeSubtitles;
        return this;
    }
    
    /**
     * 设置是否包含CC
     */
    public YouTubeSearchRequest setIncludeCC(Boolean includeCC) {
        this.includeCC = includeCC;
        return this;
    }
    
    /**
     * 设置是否包含HD
     */
    public YouTubeSearchRequest setIncludeHD(Boolean includeHD) {
        this.includeHD = includeHD;
        return this;
    }
    
    /**
     * 设置是否包含3D
     */
    public YouTubeSearchRequest setInclude3D(Boolean include3D) {
        this.include3D = include3D;
        return this;
    }
    
    /**
     * 添加自定义参数
     */
    public YouTubeSearchRequest addCustomParam(String param) {
        if (customParams == null) {
            customParams = new ArrayList<>();
        }
        if (param != null && !param.trim().isEmpty()) {
            customParams.add(param.trim());
        }
        return this;
    }
    
    /**
     * 构建搜索URL参数
     */
    public String buildSearchUrl() {
        StringBuilder url = new StringBuilder(YOUTUBE_SEARCH_URL);
        url.append("?search_query=").append(java.net.URLEncoder.encode(getValidQuery(), java.nio.charset.StandardCharsets.UTF_8));
        
        // 添加排序参数
        String sort = getValidSortBy();
        if (!"relevance".equals(sort)) {
            url.append("&sp=").append(getSortParameter(sort));
        }
        
        // 添加时间范围参数
        String time = getValidTimeRange();
        if (!"any".equals(time)) {
            url.append("&sp=").append(getTimeRangeParameter(time));
        }
        
        // 添加视频类型参数
        String vType = getValidVideoType();
        if (!"any".equals(vType)) {
            url.append("&sp=").append(getVideoTypeParameter(vType));
        }
        
        // 添加时长参数
        String dur = getValidDuration();
        if (!"any".equals(dur)) {
            url.append("&sp=").append(getDurationParameter(dur));
        }
        
        // 添加质量参数
        String qual = getValidQuality();
        if (!"any".equals(qual)) {
            url.append("&sp=").append(getQualityParameter(qual));
        }
        
        // 添加频道过滤
        if (channel != null && !channel.trim().isEmpty()) {
            url.append("&channel=").append(java.net.URLEncoder.encode(channel.trim(), java.nio.charset.StandardCharsets.UTF_8));
        }
        
        // 添加语言参数
        String lang = getValidLanguage();
        if (!"en".equals(lang)) {
            url.append("&hl=").append(lang);
        }
        
        // 添加国家/地区参数
        String countryCode = getValidCountry();
        if (!"US".equals(countryCode)) {
            url.append("&gl=").append(countryCode);
        }
        
        // 添加功能参数
        if (includeSubtitles != null && includeSubtitles) {
            url.append("&sp=").append("EgIQAg%253D%253D"); // 包含字幕
        }
        
        if (includeCC != null && includeCC) {
            url.append("&sp=").append("EgIQBA%253D%253D"); // 创作共用
        }
        
        if (includeHD != null && includeHD) {
            url.append("&sp=").append("EgIQBQ%253D%253D"); // 高清
        }
        
        if (include3D != null && include3D) {
            url.append("&sp=").append("EgIQBg%253D%253D"); // 3D
        }
        
        // 添加自定义参数
        if (customParams != null) {
            for (String param : customParams) {
                if (param.contains("=")) {
                    String[] parts = param.split("=", 2);
                    if (parts.length == 2) {
                        url.append("&").append(parts[0]).append("=").append(parts[1]);
                    }
                }
            }
        }
        
        return url.toString();
    }
    
    /**
     * 获取排序参数
     */
    private String getSortParameter(String sort) {
        switch (sort) {
            case "date":
                return "CAI%253D"; // 按日期排序
            case "rating":
                return "CAE%253D"; // 按评分排序
            case "viewCount":
                return "CAM%253D"; // 按观看次数排序
            case "title":
                return "CAASAhAB"; // 按标题排序
            default:
                return ""; // 相关性排序
        }
    }
    
    /**
     * 获取时间范围参数
     */
    private String getTimeRangeParameter(String timeRange) {
        switch (timeRange) {
            case "hour":
                return "EgQIARAB"; // 过去1小时
            case "today":
                return "EgQIAhAB"; // 今天
            case "week":
                return "EgQIAxAB"; // 过去一周
            case "month":
                return "EgQIBBAB"; // 过去一个月
            case "year":
                return "EgQIBRAB"; // 过去一年
            default:
                return ""; // 任何时间
        }
    }
    
    /**
     * 获取视频类型参数
     */
    private String getVideoTypeParameter(String videoType) {
        switch (videoType) {
            case "channel":
                return "EgIQAg%253D%253D"; // 频道
            case "playlist":
                return "EgIQAw%253D%253D"; // 播放列表
            case "movie":
                return "EgIQBA%253D%253D"; // 电影
            case "episode":
                return "EgIQBQ%253D%253D"; // 剧集
            default:
                return ""; // 视频
        }
    }
    
    /**
     * 获取时长参数
     */
    private String getDurationParameter(String duration) {
        switch (duration) {
            case "short":
                return "EgIYAQ%253D%253D"; // 短于4分钟
            case "medium":
                return "EgIYAw%253D%253D"; // 4-20分钟
            case "long":
                return "EgIYAg%253D%253D"; // 长于20分钟
            default:
                return ""; // 任何时长
        }
    }
    
    /**
     * 获取质量参数
     */
    private String getQualityParameter(String quality) {
        switch (quality) {
            case "hd":
                return "EgIYAQ%253D%253D"; // 高清
            case "4k":
                return "EgIYAw%253D%253D"; // 4K
            case "8k":
                return "EgIYAg%253D%253D"; // 8K
            default:
                return ""; // 任何质量
        }
    }
    
    /**
     * 获取请求摘要
     */
    public String getRequestSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("搜索查询: ").append(getValidQuery()).append("\n");
        sb.append("结果数量: ").append(getValidCount()).append("\n");
        sb.append("排序方式: ").append(getValidSortBy()).append("\n");
        sb.append("时间范围: ").append(getValidTimeRange()).append("\n");
        sb.append("视频类型: ").append(getValidVideoType()).append("\n");
        sb.append("视频时长: ").append(getValidDuration()).append("\n");
        sb.append("视频质量: ").append(getValidQuality()).append("\n");
        
        if (channel != null && !channel.trim().isEmpty()) {
            sb.append("频道过滤: ").append(channel).append("\n");
        }
        
        sb.append("语言: ").append(getValidLanguage()).append("\n");
        sb.append("国家/地区: ").append(getValidCountry()).append("\n");
        
        if (includeLive != null && includeLive) {
            sb.append("包含直播: 是\n");
        }
        
        if (includeSubtitles != null && includeSubtitles) {
            sb.append("包含字幕: 是\n");
        }
        
        if (includeHD != null && includeHD) {
            sb.append("包含HD: 是\n");
        }
        
        return sb.toString();
    }
}
