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

/**
 * YouTube 搜索配置类
 * 提供默认配置参数和常量定义
 */
public class YouTubeConfiguration {
    
    /**
     * YouTube 搜索基础URL
     */
    public static final String YOUTUBE_SEARCH_URL = "https://www.youtube.com/results";
    
    /**
     * YouTube 视频详情URL
     */
    public static final String YOUTUBE_VIDEO_URL = "https://www.youtube.com/watch";
    
    /**
     * YouTube 频道URL
     */
    public static final String YOUTUBE_CHANNEL_URL = "https://www.youtube.com/channel";
    
    /**
     * 默认用户代理
     */
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    /**
     * 默认超时时间（秒）
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;
    
    /**
     * 默认语言
     */
    public static final String DEFAULT_LANGUAGE = "en";
    
    /**
     * 默认国家/地区
     */
    public static final String DEFAULT_COUNTRY = "US";
    
    /**
     * 默认结果数量
     */
    public static final int DEFAULT_RESULT_COUNT = 10;
    
    /**
     * 最大结果数量
     */
    public static final int MAX_RESULT_COUNT = 50;
    
    /**
     * 最小结果数量
     */
    public static final int MIN_RESULT_COUNT = 1;
    
    /**
     * 支持的排序方式
     */
    public static final String[] SUPPORTED_SORT_OPTIONS = {
        "relevance",    // 相关性
        "date",         // 上传日期
        "rating",        // 评分
        "viewCount",    // 观看次数
        "title"         // 标题
    };
    
    /**
     * 支持的时间范围
     */
    public static final String[] SUPPORTED_TIME_RANGES = {
        "any",          // 任何时间
        "hour",         // 过去1小时
        "today",        // 今天
        "week",         // 过去一周
        "month",        // 过去一个月
        "year"          // 过去一年
    };
    
    /**
     * 支持的视频类型
     */
    public static final String[] SUPPORTED_VIDEO_TYPES = {
        "any",          // 任何类型
        "video",        // 视频
        "channel",      // 频道
        "playlist",     // 播放列表
        "movie",        // 电影
        "episode"       // 剧集
    };
    
    /**
     * 支持的视频时长
     */
    public static final String[] SUPPORTED_DURATION_TYPES = {
        "any",          // 任何时长
        "short",        // 短于4分钟
        "medium",       // 4-20分钟
        "long"          // 长于20分钟
    };
    
    /**
     * 支持的视频质量
     */
    public static final String[] SUPPORTED_QUALITY_TYPES = {
        "any",          // 任何质量
        "hd",           // 高清
        "4k",           // 4K
        "8k"            // 8K
    };
    
    /**
     * 获取默认用户代理
     */
    public static String getDefaultUserAgent() {
        return DEFAULT_USER_AGENT;
    }
    
    /**
     * 获取默认超时时间
     */
    public static int getDefaultTimeoutSeconds() {
        return DEFAULT_TIMEOUT_SECONDS;
    }
    
    /**
     * 获取默认语言
     */
    public static String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }
    
    /**
     * 获取默认国家/地区
     */
    public static String getDefaultCountry() {
        return DEFAULT_COUNTRY;
    }
    
    /**
     * 获取默认结果数量
     */
    public static int getDefaultResultCount() {
        return DEFAULT_RESULT_COUNT;
    }
    
    /**
     * 验证结果数量
     */
    public static int validateResultCount(int count) {
        if (count < MIN_RESULT_COUNT) {
            return MIN_RESULT_COUNT;
        }
        if (count > MAX_RESULT_COUNT) {
            return MAX_RESULT_COUNT;
        }
        return count;
    }
    
    /**
     * 验证排序选项
     */
    public static String validateSortOption(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return SUPPORTED_SORT_OPTIONS[0]; // 默认相关性
        }
        
        String lowerSort = sort.toLowerCase().trim();
        for (String option : SUPPORTED_SORT_OPTIONS) {
            if (option.equals(lowerSort)) {
                return lowerSort;
            }
        }
        
        return SUPPORTED_SORT_OPTIONS[0]; // 默认相关性
    }
    
    /**
     * 验证时间范围
     */
    public static String validateTimeRange(String timeRange) {
        if (timeRange == null || timeRange.trim().isEmpty()) {
            return SUPPORTED_TIME_RANGES[0]; // 默认任何时间
        }
        
        String lowerTimeRange = timeRange.toLowerCase().trim();
        for (String range : SUPPORTED_TIME_RANGES) {
            if (range.equals(lowerTimeRange)) {
                return lowerTimeRange;
            }
        }
        
        return SUPPORTED_TIME_RANGES[0]; // 默认任何时间
    }
    
    /**
     * 验证视频类型
     */
    public static String validateVideoType(String videoType) {
        if (videoType == null || videoType.trim().isEmpty()) {
            return SUPPORTED_VIDEO_TYPES[0]; // 默认任何类型
        }
        
        String lowerVideoType = videoType.toLowerCase().trim();
        for (String type : SUPPORTED_VIDEO_TYPES) {
            if (type.equals(lowerVideoType)) {
                return lowerVideoType;
            }
        }
        
        return SUPPORTED_VIDEO_TYPES[0]; // 默认任何类型
    }
    
    /**
     * 验证视频时长
     */
    public static String validateDurationType(String durationType) {
        if (durationType == null || durationType.trim().isEmpty()) {
            return SUPPORTED_DURATION_TYPES[0]; // 默认任何时长
        }
        
        String lowerDurationType = durationType.toLowerCase().trim();
        for (String type : SUPPORTED_DURATION_TYPES) {
            if (type.equals(lowerDurationType)) {
                return lowerDurationType;
            }
        }
        
        return SUPPORTED_DURATION_TYPES[0]; // 默认任何时长
    }
    
    /**
     * 验证视频质量
     */
    public static String validateQualityType(String qualityType) {
        if (qualityType == null || qualityType.trim().isEmpty()) {
            return SUPPORTED_QUALITY_TYPES[0]; // 默认任何质量
        }
        
        String lowerQualityType = qualityType.toLowerCase().trim();
        for (String type : SUPPORTED_QUALITY_TYPES) {
            if (type.equals(lowerQualityType)) {
                return lowerQualityType;
            }
        }
        
        return SUPPORTED_QUALITY_TYPES[0]; // 默认任何质量
    }
    
    /**
     * 格式化时长（秒转换为 HH:MM:SS 格式）
     */
    public static String formatDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }
    
    /**
     * 解析时长字符串为秒数
     */
    public static int parseDurationToSeconds(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            return 0;
        }
        
        try {
            String[] parts = duration.trim().split(":");
            if (parts.length == 3) {
                // HH:MM:SS
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);
                return hours * 3600 + minutes * 60 + seconds;
            } else if (parts.length == 2) {
                // MM:SS
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return minutes * 60 + seconds;
            } else if (parts.length == 1) {
                // SS
                return Integer.parseInt(parts[0]);
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }
        
        return 0;
    }
    
    /**
     * 格式化观看次数
     */
    public static String formatViewCount(long viewCount) {
        if (viewCount < 1000) {
            return String.valueOf(viewCount);
        } else if (viewCount < 1000000) {
            return String.format("%.1fK", viewCount / 1000.0);
        } else if (viewCount < 1000000000) {
            return String.format("%.1fM", viewCount / 1000000.0);
        } else {
            return String.format("%.1fB", viewCount / 1000000000.0);
        }
    }
    
    /**
     * 解析观看次数字符串
     */
    public static long parseViewCount(String viewCountStr) {
        if (viewCountStr == null || viewCountStr.trim().isEmpty()) {
            return 0;
        }
        
        try {
            String cleaned = viewCountStr.trim().toLowerCase()
                    .replaceAll("[^0-9.,kmb]", "");
            
            if (cleaned.endsWith("b")) {
                return (long) (Double.parseDouble(cleaned.substring(0, cleaned.length() - 1)) * 1000000000);
            } else if (cleaned.endsWith("m")) {
                return (long) (Double.parseDouble(cleaned.substring(0, cleaned.length() - 1)) * 1000000);
            } else if (cleaned.endsWith("k")) {
                return (long) (Double.parseDouble(cleaned.substring(0, cleaned.length() - 1)) * 1000);
            } else {
                return Long.parseLong(cleaned.replace(",", ""));
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
