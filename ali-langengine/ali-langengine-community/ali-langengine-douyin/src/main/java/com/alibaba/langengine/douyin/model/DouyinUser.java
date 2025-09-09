package com.alibaba.langengine.douyin.model;

import lombok.Data;

/**
 * 抖音用户模型
 */
@Data
public class DouyinUser {
    private String userId;
    private String nickname;
    private String signature;
    private String avatarUrl;
    private String shortId;
    private String uniqueId;
    private Integer followerCount;
    private Integer followingCount;
    private Integer awemeCount;
    private Integer totalFavorited;
    private String gender;
    private String birthday;
    private String location;
    private String school;
    private String enterprise;
    private Boolean isVerified;
    private String verificationType;
    private String verificationInfo;
    private String customVerify;
    private String constellation;
    private String roomId;
    private String liveUrl;
    private Boolean isLive;
    private String liveTitle;
    private Integer liveViewerCount;
}