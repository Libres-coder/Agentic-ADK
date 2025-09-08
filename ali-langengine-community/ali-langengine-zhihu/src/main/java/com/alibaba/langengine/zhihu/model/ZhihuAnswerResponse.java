package com.alibaba.langengine.zhihu.model;

import lombok.Data;
import java.util.List;

/**
 * 知乎回答响应模型
 */
@Data
public class ZhihuAnswerResponse {
    private List<ZhihuAnswer> data;
    private Paging paging;
    private String error;
    
    @Data
    public static class Paging {
        private Boolean isEnd;
        private String next;
        private String previous;
        private Integer totals;
    }
}