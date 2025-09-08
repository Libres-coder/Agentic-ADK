package com.alibaba.langengine.zhihu.model;

import lombok.Data;
import java.util.List;

/**
 * 知乎问题模型
 */
@Data
public class ZhihuQuestion {
    private String id;
    private String title;
    private String content;
    private String url;
    private Integer answerCount;
    private Integer followerCount;
    private Integer viewCount;
    private String createdTime;
    private String updatedTime;
    private ZhihuAuthor author;
    private List<String> topics;
    private Boolean isFollowed;
    private Boolean isAnswered;
}