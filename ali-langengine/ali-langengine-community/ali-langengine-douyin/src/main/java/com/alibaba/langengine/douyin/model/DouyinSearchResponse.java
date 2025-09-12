package com.alibaba.langengine.douyin.model;

import lombok.Data;
import java.util.List;

/**
 * 抖音搜索响应模型
 */
@Data
public class DouyinSearchResponse {
    private List<DouyinVideo> videos;
    private List<DouyinUser> users;
    private Integer totalCount;
    private Boolean hasMore;
    private String cursor;
    private String error;
}