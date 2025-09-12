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
package com.alibaba.langengine.reddit.sdk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedditPost {

    /**
     * 帖子ID
     */
    private String id;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    @JsonProperty("selftext")
    private String content;

    /**
     * 作者
     */
    private String author;

    /**
     * 子论坛
     */
    private String subreddit;

    /**
     * 子论坛前缀
     */
    @JsonProperty("subreddit_name_prefixed")
    private String subredditPrefixed;

    /**
     * 创建时间戳
     */
    @JsonProperty("created_utc")
    private Long createdUtc;

    /**
     * 评分
     */
    private Integer score;

    /**
     * 点赞率
     */
    @JsonProperty("upvote_ratio")
    private Double upvoteRatio;

    /**
     * 评论数
     */
    @JsonProperty("num_comments")
    private Integer numComments;

    /**
     * 帖子URL
     */
    private String url;

    /**
     * 永久链接
     */
    private String permalink;

    /**
     * 是否置顶
     */
    private Boolean stickied;

    /**
     * 是否NSFW
     */
    @JsonProperty("over_18")
    private Boolean over18;

    /**
     * 帖子类型
     */
    @JsonProperty("post_hint")
    private String postHint;

    /**
     * 预览图片
     */
    private Preview preview;

    /**
     * 媒体信息
     */
    private Media media;

    /**
     * 标签列表
     */
    @JsonProperty("link_flair_text")
    private String linkFlairText;

    /**
     * 域名
     */
    private String domain;

    /**
     * 是否被删除
     */
    private Boolean removed;

    /**
     * 获取创建时间的Instant对象
     *
     * @return Instant对象，如果createdUtc为null则返回null
     */
    @JsonIgnore
    public Instant getCreatedInstant() {
        return createdUtc != null ? Instant.ofEpochSecond(createdUtc) : null;
    }

    /**
     * 获取创建时间的LocalDateTime对象（系统默认时区）
     *
     * @return LocalDateTime对象，如果createdUtc为null则返回null
     */
    @JsonIgnore
    public LocalDateTime getCreatedDateTime() {
        Instant instant = getCreatedInstant();
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }

    /**
     * 获取创建时间的Date对象（兼容旧API）
     *
     * @return Date对象，如果createdUtc为null则返回null
     */
    @JsonIgnore
    public Date getCreatedDate() {
        return createdUtc != null ? new Date(createdUtc * 1000) : null;
    }

    /**
     * 预览图片类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Preview {
        private List<Image> images;
        private Boolean enabled;
    }

    /**
     * 图片类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private Source source;
        private String id;
    }

    /**
     * 图片源类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String url;
        private Integer width;
        private Integer height;
    }

    /**
     * 媒体类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Media {
        @JsonProperty("reddit_video")
        private RedditVideo redditVideo;
    }

    /**
     * Reddit视频类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RedditVideo {
        @JsonProperty("fallback_url")
        private String fallbackUrl;
        private Integer height;
        private Integer width;
        @JsonProperty("scrubber_media_url")
        private String scrubberMediaUrl;
        @JsonProperty("dash_url")
        private String dashUrl;
        private Integer duration;
        @JsonProperty("hls_url")
        private String hlsUrl;
        @JsonProperty("is_gif")
        private Boolean isGif;
        @JsonProperty("transcoding_status")
        private String transcodingStatus;
    }
}
