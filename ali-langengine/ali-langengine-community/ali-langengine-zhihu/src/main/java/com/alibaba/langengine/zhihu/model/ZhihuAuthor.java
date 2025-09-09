package com.alibaba.langengine.zhihu.model;

import lombok.Data;

/**
 * 知乎作者模型
 */
@Data
public class ZhihuAuthor {
    private String id;
    private String name;
    private String headline;
    private String description;
    private String avatarUrl;
    private String url;
    private String gender;
    private String location;
    private String business;
    private Integer followerCount;
    private Integer followingCount;
    private Integer answerCount;
    private Integer questionCount;
    private Integer articleCount;
    private Integer voteupCount;
    private Boolean isFollowed;
    private String badge;
}