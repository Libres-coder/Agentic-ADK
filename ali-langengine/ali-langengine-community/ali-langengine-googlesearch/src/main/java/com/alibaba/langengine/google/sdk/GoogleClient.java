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
package com.alibaba.langengine.google.sdk;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.google.GoogleSearchConfiguration.*;

/**
 * Google搜索客户端
 * 提供Google搜索功能，支持多种搜索类型和参数
 */
public class GoogleClient {
    
    private final String userAgent;
    private final int timeoutSeconds;
    private final String language;
    private final String country;
    private final OkHttpClient httpClient;
    
    /**
     * 默认构造函数
     */
    public GoogleClient() {
        this.userAgent = GOOGLE_USER_AGENT;
        this.timeoutSeconds = GOOGLE_TIMEOUT_SECONDS;
        this.language = GOOGLE_LANGUAGE;
        this.country = GOOGLE_COUNTRY;
        this.httpClient = createHttpClient();
    }
    
    /**
     * 自定义构造函数
     */
    public GoogleClient(String userAgent, int timeoutSeconds, String language, String country) {
        this.userAgent = (userAgent == null || userAgent.trim().isEmpty()) ? GOOGLE_USER_AGENT : userAgent;
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : GOOGLE_TIMEOUT_SECONDS;
        this.language = (language == null || language.trim().isEmpty()) ? GOOGLE_LANGUAGE : language;
        this.country = (country == null || country.trim().isEmpty()) ? GOOGLE_COUNTRY : country;
        this.httpClient = createHttpClient();
    }
    
    /**
     * 创建HTTP客户端
     */
    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }
    
    /**
     * 执行搜索
     */
    public SearchResponse search(SearchRequest request) throws GoogleException {
        if (request == null || !request.isValid()) {
            throw new GoogleException("Search request must not be null and query must not be blank");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 构建搜索URL
            HttpUrl.Builder urlBuilder = HttpUrl.parse(GoogleConstant.BASE_URL).newBuilder();
            
            // 添加基本查询参数
            String encodedQuery = URLEncoder.encode(request.getQuery(), StandardCharsets.UTF_8);
            urlBuilder.addQueryParameter("q", encodedQuery);
            
            // 添加搜索类型
            String searchType = request.getValidSearchType();
            if (!GoogleConstant.SEARCH_TYPE_WEB.equals(searchType)) {
                urlBuilder.addQueryParameter("tbm", searchType);
            }
            
            // 添加语言设置
            String lang = request.getValidLanguage();
            if (!"en".equals(lang)) {
                urlBuilder.addQueryParameter("lr", "lang_" + lang);
            }
            
            // 添加国家/地区设置
            if (request.getCountry() != null && !request.getCountry().trim().isEmpty()) {
                urlBuilder.addQueryParameter("gl", request.getCountry().toLowerCase());
            }
            
            // 添加排序方式
            if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
                urlBuilder.addQueryParameter("s", request.getSortBy().toLowerCase());
            }
            
            // 添加时间范围
            if (request.getTimeRange() != null && !request.getTimeRange().trim().isEmpty()) {
                urlBuilder.addQueryParameter("tbs", "qdr:" + request.getTimeRange().toLowerCase());
            }
            
            // 添加安全搜索
            if (request.getSafeSearch() != null) {
                urlBuilder.addQueryParameter("safe", request.getSafeSearch() ? "on" : "off");
            }
            
            // 添加结果数量
            int count = request.getValidCount();
            if (count != GoogleConstant.DEFAULT_RESULT_COUNT) {
                urlBuilder.addQueryParameter("num", String.valueOf(count));
            }
            
            // 添加自定义参数
            if (request.getCustomParams() != null) {
                for (String param : request.getCustomParams()) {
                    if (param != null && param.contains("=")) {
                        String[] parts = param.split("=", 2);
                        if (parts.length == 2) {
                            urlBuilder.addQueryParameter(parts[0], parts[1]);
                        }
                    }
                }
            }
            
            String searchUrl = urlBuilder.build().toString();
            
            // 执行HTTP请求
            Request httpRequest = new Request.Builder()
                    .url(searchUrl)
                    .header("User-Agent", userAgent)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", language + "," + language + "-" + country + ";q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("DNT", "1")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .build();
            
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new GoogleException("HTTP request failed with status: " + response.code());
                }
                
                String html = response.body().string();
                if (html == null || html.trim().isEmpty()) {
                    throw new GoogleException("Empty response body");
                }
                
                // 解析HTML响应
                Document doc = Jsoup.parse(html, searchUrl);
                
                // 创建搜索响应
                SearchResponse searchResponse = new SearchResponse(request.getQuery())
                        .setSearchType(searchType)
                        .setLanguage(lang)
                        .setCountry(request.getCountry())
                        .setSearchDuration(System.currentTimeMillis() - startTime);
                
                // 解析搜索结果
                List<SearchResult> results = parseSearchResults(doc, searchType, count);
                searchResponse.addResults(results);
                
                // 解析总结果数
                Long totalResults = parseTotalResults(doc);
                searchResponse.setTotalResults(totalResults);
                
                // 解析搜索建议
                List<String> suggestions = parseSuggestions(doc);
                searchResponse.setSuggestions(suggestions);
                
                // 解析相关搜索
                List<String> relatedQueries = parseRelatedQueries(doc);
                searchResponse.setRelatedQueries(relatedQueries);
                
                return searchResponse;
                
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            SearchResponse errorResponse = new SearchResponse(request.getQuery())
                    .setError("Google search failed: " + e.getMessage())
                    .setSearchDuration(duration);
            
            if (e instanceof GoogleException) {
                throw (GoogleException) e;
            } else {
                throw new GoogleException("Google search failed: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 简单搜索方法
     */
    public SearchResponse search(String query) throws GoogleException {
        SearchRequest request = new SearchRequest().setQuery(query);
        return search(request);
    }
    
    /**
     * 带结果数量的搜索方法
     */
    public SearchResponse search(String query, int count) throws GoogleException {
        SearchRequest request = new SearchRequest()
                .setQuery(query)
                .setCount(count);
        return search(request);
    }
    
    /**
     * 带搜索类型的搜索方法
     */
    public SearchResponse search(String query, String searchType) throws GoogleException {
        SearchRequest request = new SearchRequest()
                .setQuery(query)
                .setSearchType(searchType);
        return search(request);
    }
    
    /**
     * 解析搜索结果
     */
    private List<SearchResult> parseSearchResults(Document doc, String searchType, int maxCount) {
        List<SearchResult> results = new ArrayList<>();
        
        try {
            Elements resultElements;
            
            // 根据搜索类型选择不同的选择器
            switch (searchType) {
                case GoogleConstant.SEARCH_TYPE_IMAGES:
                    resultElements = doc.select("div[data-ved] img, .rg_i");
                    break;
                case GoogleConstant.SEARCH_TYPE_NEWS:
                    resultElements = doc.select("div[data-ved], .g, .rc");
                    break;
                case GoogleConstant.SEARCH_TYPE_VIDEOS:
                    resultElements = doc.select("div[data-ved], .g, .rc");
                    break;
                default: // web search
                    resultElements = doc.select("div[data-ved], .g, .rc, .result, .c-container");
                    break;
            }
            
            for (Element element : resultElements) {
                if (results.size() >= maxCount) {
                    break;
                }
                
                SearchResult result = parseSearchResult(element, searchType);
                if (result != null && result.isValid()) {
                    results.add(result);
                }
            }
            
        } catch (Exception e) {
            // 记录解析错误但不中断搜索
            System.err.println("Error parsing search results: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * 解析单个搜索结果
     */
    private SearchResult parseSearchResult(Element element, String searchType) {
        try {
            SearchResult result = new SearchResult();
            
            // 解析标题和URL
            Element titleElement = element.selectFirst("h3 a, h3 > a, .t > a, .r > a");
            if (titleElement != null) {
                result.setTitle(titleElement.text().trim());
                String url = titleElement.attr("abs:href");
                if (url.isEmpty()) {
                    url = titleElement.attr("href");
                }
                result.setUrl(url);
            }
            
            // 解析描述
            Element descElement = element.selectFirst(".s, .st, .c-abstract, .c-line-clamp3");
            if (descElement != null) {
                result.setDescription(descElement.text().trim());
            }
            
            // 解析域名
            Element domainElement = element.selectFirst(".s cite, .st cite, .c-abstract cite");
            if (domainElement != null) {
                result.setDomain(domainElement.text().trim());
            }
            
            // 解析发布时间
            Element timeElement = element.selectFirst(".s time, .st time, .c-abstract time");
            if (timeElement != null) {
                String timeText = timeElement.attr("datetime");
                if (!timeText.isEmpty()) {
                    try {
                        LocalDateTime publishTime = LocalDateTime.parse(timeText, DateTimeFormatter.ISO_DATE_TIME);
                        result.setPublishDate(publishTime);
                    } catch (Exception e) {
                        // 忽略时间解析错误
                    }
                }
            }
            
            // 解析缩略图（图片搜索）
            if (GoogleConstant.SEARCH_TYPE_IMAGES.equals(searchType)) {
                Element imgElement = element.selectFirst("img");
                if (imgElement != null) {
                    result.setThumbnailUrl(imgElement.attr("abs:src"));
                }
            }
            
            // 解析评分（购物搜索）
            Element ratingElement = element.selectFirst(".s .rating, .st .rating");
            if (ratingElement != null) {
                String ratingText = ratingElement.text();
                try {
                    double rating = Double.parseDouble(ratingText.replaceAll("[^0-9.]", ""));
                    result.setRating(rating);
                } catch (Exception e) {
                    // 忽略评分解析错误
                }
            }
            
            // 设置类型
            result.setType(searchType);
            
            return result;
            
        } catch (Exception e) {
            // 记录解析错误但不中断搜索
            System.err.println("Error parsing individual search result: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析总结果数
     */
    private Long parseTotalResults(Document doc) {
        try {
            Element statsElement = doc.selectFirst("#result-stats, .sd");
            if (statsElement != null) {
                String statsText = statsElement.text();
                // 提取数字，例如："About 1,000,000,000 results (0.45 seconds)"
                String numberText = statsText.replaceAll("[^0-9,]", "");
                if (!numberText.isEmpty()) {
                    return Long.parseLong(numberText.replace(",", ""));
                }
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return null;
    }
    
    /**
     * 解析搜索建议
     */
    private List<String> parseSuggestions(Document doc) {
        List<String> suggestions = new ArrayList<>();
        try {
            Elements suggestionElements = doc.select(".suggestions a, .suggestions span");
            for (Element element : suggestionElements) {
                String suggestion = element.text().trim();
                if (!suggestion.isEmpty()) {
                    suggestions.add(suggestion);
                }
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return suggestions;
    }
    
    /**
     * 解析相关搜索
     */
    private List<String> parseRelatedQueries(Document doc) {
        List<String> relatedQueries = new ArrayList<>();
        try {
            Elements relatedElements = doc.select(".brs_col a, .related-searches a");
            for (Element element : relatedElements) {
                String query = element.text().trim();
                if (!query.isEmpty()) {
                    relatedQueries.add(query);
                }
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return relatedQueries;
    }
} 