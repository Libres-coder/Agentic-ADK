package com.alibaba.langengine.netflix;

import lombok.Data;

@Data
public class NetflixConfiguration {
    
    // Netflix API配置 (使用第三方API服务)
    public static final String NETFLIX_API_KEY = System.getProperty("netflix.api.key");
    public static final String NETFLIX_API_BASE_URL = System.getProperty("netflix.api.base.url", "https://api.themoviedb.org/3");
    public static final String NETFLIX_SEARCH_URL = System.getProperty("netflix.search.url", "https://api.themoviedb.org/3/search/multi");
    public static final String NETFLIX_TRENDING_URL = System.getProperty("netflix.trending.url", "https://api.themoviedb.org/3/trending/all/day");
    public static final String NETFLIX_DISCOVER_URL = System.getProperty("netflix.discover.url", "https://api.themoviedb.org/3/discover/movie");
    
    // 通用配置
    public static final int NETFLIX_TIMEOUT = Integer.parseInt(System.getProperty("netflix.timeout", "30"));
    public static final int DEFAULT_PAGE = Integer.parseInt(System.getProperty("netflix.default.page", "1"));
    public static final int DEFAULT_PAGE_SIZE = Integer.parseInt(System.getProperty("netflix.default.page.size", "20"));
    public static final int MAX_PAGE_SIZE = Integer.parseInt(System.getProperty("netflix.max.page.size", "100"));
    
    // 默认语言和地区
    public static final String DEFAULT_LANGUAGE = System.getProperty("netflix.default.language", "zh-CN");
    public static final String DEFAULT_REGION = System.getProperty("netflix.default.region", "CN");
    
    private String apiKey;
    private String apiBaseUrl;
    private String searchUrl;
    private String trendingUrl;
    private String discoverUrl;
    private int timeout;
    private int defaultPage;
    private int defaultPageSize;
    private int maxPageSize;
    private String defaultLanguage;
    private String defaultRegion;
    
    public NetflixConfiguration() {
        this.apiKey = NETFLIX_API_KEY;
        this.apiBaseUrl = NETFLIX_API_BASE_URL;
        this.searchUrl = NETFLIX_SEARCH_URL;
        this.trendingUrl = NETFLIX_TRENDING_URL;
        this.discoverUrl = NETFLIX_DISCOVER_URL;
        this.timeout = NETFLIX_TIMEOUT;
        this.defaultPage = DEFAULT_PAGE;
        this.defaultPageSize = DEFAULT_PAGE_SIZE;
        this.maxPageSize = MAX_PAGE_SIZE;
        this.defaultLanguage = DEFAULT_LANGUAGE;
        this.defaultRegion = DEFAULT_REGION;
    }
}
