package com.alibaba.langengine.zhihu.model;

import lombok.Data;
import java.util.List;

/**
 * 知乎回答模型
 */
@Data
public class ZhihuAnswer {
    private String id;
    private String content;
    private String excerpt;
    private String url;
    private Integer voteupCount;
    private Integer commentCount;
    private String createdTime;
    private String updatedTime;
    private ZhihuAuthor author;
    private String questionId;
    private String questionTitle;
    private Boolean isCollapsed;
    private List<String> images;
}