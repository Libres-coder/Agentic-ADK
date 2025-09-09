package com.alibaba.langengine.douyin.model;

import lombok.Data;
import java.util.List;

/**
 * 抖音视频模型
 */
@Data
public class DouyinVideo {
    private String awemeId;
    private String desc;
    private String createTime;
    private String author;
    private String authorId;
    private String authorAvatar;
    private String videoUrl;
    private String coverUrl;
    private Integer duration;
    private Integer playCount;
    private Integer diggCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer forwardCount;
    private String musicTitle;
    private String musicAuthor;
    private String musicUrl;
    private List<String> hashtags;
    private String location;
    private Boolean isOriginal;
    private String videoQuality;
    private String ratio;
}