package com.alibaba.langengine.zhihu.model;

import lombok.Data;
import java.util.List;

/**
 * 知乎搜索响应模型
 */
@Data
public class ZhihuSearchResponse {
    private List<ZhihuQuestion> data;
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