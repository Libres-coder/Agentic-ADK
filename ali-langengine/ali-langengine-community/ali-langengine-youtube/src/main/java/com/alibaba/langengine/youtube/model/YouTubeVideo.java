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

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * YouTube 视频模型
 * 表示从 YouTube 搜索结果中解析出的视频信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeVideo {
    
    /**
     * 视频ID
     */
    private String videoId;
    
    /**
     * 视频标题
     */
    private String title;
    
    /**
     * 视频描述
     */
    private String description;
    
    /**
     * 频道名称
     */
    private String channelName;
    
    /**
     * 频道ID
     */
    private String channelId;
    
    /**
     * 频道URL
     */
    private String channelUrl;
    
    /**
     * 视频时长（秒）
     */
    private Integer duration;
    
    /**
     * 观看次数
     */
    private Long viewCount;
    
    /**
     * 点赞数
     */
    private Long likeCount;
    
    /**
     * 点踩数
     */
    private Long dislikeCount;
    
    /**
     * 评论数
     */
    private Long commentCount;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadDate;
    
    /**
     * 视频URL
     */
    private String videoUrl;
    
    /**
     * 缩略图URL
     */
    private String thumbnailUrl;
    
    /**
     * 高分辨率缩略图URL
     */
    private String highResThumbnailUrl;
    
    /**
     * 视频分类
     */
    private String category;
    
    /**
     * 视频标签
     */
    private List<String> tags;
    
    /**
     * 视频语言
     */
    private String language;
    
    /**
     * 视频质量
     */
    private String quality;
    
    /**
     * 视频分辨率
     */
    private String resolution;
    
    /**
     * 是否直播
     */
    private Boolean isLive;
    
    /**
     * 是否私有
     */
    private Boolean isPrivate;
    
    /**
     * 是否年龄限制
     */
    private Boolean isAgeRestricted;
    
    /**
     * 是否可嵌入
     */
    private Boolean isEmbeddable;
    
    /**
     * 视频类型
     */
    private String videoType;
    
    /**
     * 播放列表ID（如果适用）
     */
    private String playlistId;
    
    /**
     * 播放列表位置（如果适用）
     */
    private Integer playlistPosition;
    
    /**
     * 视频状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdDate;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdated;
    
    /**
     * 默认构造函数
     */
    public YouTubeVideo(String videoId, String title) {
        this.videoId = videoId;
        this.title = title;
        this.tags = new ArrayList<>();
    }
    
    /**
     * 添加标签
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (tag != null && !tag.trim().isEmpty()) {
            tags.add(tag.trim());
        }
    }
    
    /**
     * 获取标签字符串
     */
    public String getTagsString() {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(", ", tags);
    }
    
    /**
     * 获取格式化时长
     */
    public String getFormattedDuration() {
        if (duration == null || duration <= 0) {
            return "未知";
        }
        
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
    
    /**
     * 获取格式化观看次数
     */
    public String getFormattedViewCount() {
        if (viewCount == null || viewCount <= 0) {
            return "未知";
        }
        
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
     * 获取格式化点赞数
     */
    public String getFormattedLikeCount() {
        if (likeCount == null || likeCount <= 0) {
            return "未知";
        }
        
        if (likeCount < 1000) {
            return String.valueOf(likeCount);
        } else if (likeCount < 1000000) {
            return String.format("%.1fK", likeCount / 1000.0);
        } else if (likeCount < 1000000000) {
            return String.format("%.1fM", likeCount / 1000000.0);
        } else {
            return String.format("%.1fB", likeCount / 1000000000.0);
        }
    }
    
    /**
     * 获取短描述（截取前200字符）
     */
    public String getShortDescription() {
        if (description == null || description.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = description.trim();
        if (trimmed.length() <= 200) {
            return trimmed;
        }
        
        return trimmed.substring(0, 200) + "...";
    }
    
    /**
     * 检查是否有观看数据
     */
    public boolean hasViewData() {
        return viewCount != null && viewCount > 0;
    }
    
    /**
     * 检查是否有互动数据
     */
    public boolean hasEngagementData() {
        return (likeCount != null && likeCount > 0) || 
               (commentCount != null && commentCount > 0);
    }
    
    /**
     * 检查是否是直播
     */
    public boolean isLiveStream() {
        return isLive != null && isLive;
    }
    
    /**
     * 检查是否是私有视频
     */
    public boolean isPrivateVideo() {
        return isPrivate != null && isPrivate;
    }
    
    /**
     * 检查是否有年龄限制
     */
    public boolean hasAgeRestriction() {
        return isAgeRestricted != null && isAgeRestricted;
    }
    
    /**
     * 验证视频信息是否有效
     */
    public boolean isValid() {
        return videoId != null && !videoId.trim().isEmpty() &&
               title != null && !title.trim().isEmpty();
    }
    
    /**
     * 获取视频的完整信息字符串
     */
    public String getFullInfo() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("标题: ").append(title).append("\n");
        
        if (channelName != null && !channelName.trim().isEmpty()) {
            sb.append("频道: ").append(channelName).append("\n");
        }
        
        if (duration != null && duration > 0) {
            sb.append("时长: ").append(getFormattedDuration()).append("\n");
        }
        
        if (viewCount != null && viewCount > 0) {
            sb.append("观看次数: ").append(getFormattedViewCount()).append("\n");
        }
        
        if (likeCount != null && likeCount > 0) {
            sb.append("点赞数: ").append(getFormattedLikeCount()).append("\n");
        }
        
        if (uploadDate != null) {
            sb.append("上传时间: ").append(uploadDate.toLocalDate()).append("\n");
        }
        
        if (description != null && !description.trim().isEmpty()) {
            sb.append("描述: ").append(getShortDescription()).append("\n");
        }
        
        if (videoUrl != null && !videoUrl.trim().isEmpty()) {
            sb.append("视频链接: ").append(videoUrl).append("\n");
        }
        
        if (channelUrl != null && !channelUrl.trim().isEmpty()) {
            sb.append("频道链接: ").append(channelUrl).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 获取视频的简化信息
     */
    public String getSimpleInfo() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("标题: ").append(title).append("\n");
        
        if (channelName != null && !channelName.trim().isEmpty()) {
            sb.append("频道: ").append(channelName);
        }
        
        if (duration != null && duration > 0) {
            sb.append(" | 时长: ").append(getFormattedDuration());
        }
        
        if (viewCount != null && viewCount > 0) {
            sb.append(" | 观看: ").append(getFormattedViewCount());
        }
        
        if (uploadDate != null) {
            sb.append(" | 上传: ").append(uploadDate.toLocalDate());
        }
        
        sb.append("\n");
        
        if (videoUrl != null && !videoUrl.trim().isEmpty()) {
            sb.append("链接: ").append(videoUrl).append("\n");
        }
        
        return sb.toString();
    }
}
