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
package com.alibaba.langengine.googlescholar.sdk;

import com.alibaba.langengine.googlescholar.GoogleScholarConfiguration;
import com.alibaba.langengine.googlescholar.model.GoogleScholarPaper;
import com.alibaba.langengine.googlescholar.model.GoogleScholarSearchRequest;
import com.alibaba.langengine.googlescholar.model.GoogleScholarSearchResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.langengine.googlescholar.GoogleScholarConfiguration.*;

/**
 * Google Scholar 搜索客户端
 * 提供 Google Scholar 搜索功能
 */
public class GoogleScholarClient {
    
    private final String userAgent;
    private final int timeoutSeconds;
    private final String language;
    private final String country;
    private final OkHttpClient httpClient;
    
    /**
     * 默认构造函数
     */
    public GoogleScholarClient() {
        this.userAgent = getDefaultUserAgent();
        this.timeoutSeconds = getDefaultTimeoutSeconds();
        this.language = getDefaultLanguage();
        this.country = getDefaultCountry();
        this.httpClient = createHttpClient();
    }
    
    /**
     * 自定义构造函数
     */
    public GoogleScholarClient(String userAgent, int timeoutSeconds, String language, String country) {
        this.userAgent = (userAgent == null || userAgent.trim().isEmpty()) ? getDefaultUserAgent() : userAgent;
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : getDefaultTimeoutSeconds();
        this.language = (language == null || language.trim().isEmpty()) ? getDefaultLanguage() : language;
        this.country = (country == null || country.trim().isEmpty()) ? getDefaultCountry() : country;
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
    public GoogleScholarSearchResponse search(GoogleScholarSearchRequest request) throws GoogleScholarException {
        if (request == null || !request.isValid()) {
            throw new GoogleScholarException("Search request must not be null and query must not be blank");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 构建搜索URL
            String searchUrl = request.buildSearchUrl();
            
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
                    throw new GoogleScholarException("HTTP request failed with status: " + response.code());
                }
                
                String html = response.body().string();
                if (html == null || html.trim().isEmpty()) {
                    throw new GoogleScholarException("Empty response body");
                }
                
                // 解析HTML响应
                Document doc = Jsoup.parse(html, searchUrl);
                
                // 创建搜索响应
                GoogleScholarSearchResponse searchResponse = new GoogleScholarSearchResponse(request.getQuery())
                        .setSearchDuration(System.currentTimeMillis() - startTime)
                        .setSearchUrl(searchUrl);
                
                // 解析搜索结果
                List<GoogleScholarPaper> papers = parseSearchResults(doc, request.getValidCount());
                searchResponse.setPapers(papers);
                searchResponse.setReturnedCount(papers.size());
                
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
            GoogleScholarSearchResponse errorResponse = new GoogleScholarSearchResponse(request.getQuery())
                    .setError("Google Scholar search failed: " + e.getMessage())
                    .setSearchDuration(duration);
            
            if (e instanceof GoogleScholarException) {
                throw (GoogleScholarException) e;
            } else {
                throw new GoogleScholarException("Google Scholar search failed: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 简单搜索方法
     */
    public GoogleScholarSearchResponse search(String query) throws GoogleScholarException {
        GoogleScholarSearchRequest request = new GoogleScholarSearchRequest(query);
        return search(request);
    }
    
    /**
     * 带结果数量的搜索方法
     */
    public GoogleScholarSearchResponse search(String query, int count) throws GoogleScholarException {
        GoogleScholarSearchRequest request = new GoogleScholarSearchRequest(query)
                .setCount(count);
        return search(request);
    }
    
    /**
     * 解析搜索结果
     */
    private List<GoogleScholarPaper> parseSearchResults(Document doc, int maxCount) {
        List<GoogleScholarPaper> papers = new ArrayList<>();
        
        try {
            // Google Scholar 搜索结果的选择器
            Elements resultElements = doc.select("div.gs_ri, div.gs_r");
            
            for (Element element : resultElements) {
                if (papers.size() >= maxCount) {
                    break;
                }
                
                GoogleScholarPaper paper = parseSearchResult(element);
                if (paper != null && paper.isValid()) {
                    papers.add(paper);
                }
            }
            
        } catch (Exception e) {
            // 记录解析错误但不中断搜索
            System.err.println("Error parsing search results: " + e.getMessage());
        }
        
        return papers;
    }
    
    /**
     * 解析单个搜索结果
     */
    private GoogleScholarPaper parseSearchResult(Element element) {
        try {
            GoogleScholarPaper paper = new GoogleScholarPaper();
            
            // 解析标题
            Element titleElement = element.selectFirst("h3.gs_rt a, h3.gs_rt");
            if (titleElement != null) {
                String title = titleElement.text().trim();
                paper.setTitle(title);
                
                // 解析详情链接
                Element linkElement = titleElement.selectFirst("a");
                if (linkElement != null) {
                    String detailUrl = linkElement.attr("abs:href");
                    if (detailUrl.isEmpty()) {
                        detailUrl = linkElement.attr("href");
                    }
                    paper.setDetailUrl(detailUrl);
                }
            }
            
            // 解析作者和期刊信息
            Element authorElement = element.selectFirst("div.gs_a");
            if (authorElement != null) {
                String authorText = authorElement.text().trim();
                parseAuthorAndVenue(paper, authorText);
            }
            
            // 解析摘要
            Element abstractElement = element.selectFirst("div.gs_rs");
            if (abstractElement != null) {
                String abstractText = abstractElement.text().trim();
                paper.setAbstractText(abstractText);
            }
            
            // 解析引用信息
            Element citationElement = element.selectFirst("div.gs_fl a[href*='cites']");
            if (citationElement != null) {
                String citationText = citationElement.text().trim();
                Integer citations = parseCitationCount(citationText);
                if (citations != null) {
                    paper.setCitations(citations);
                }
                
                String citationsUrl = citationElement.attr("abs:href");
                if (citationsUrl.isEmpty()) {
                    citationsUrl = citationElement.attr("href");
                }
                paper.setCitationsUrl(citationsUrl);
            }
            
            // 解析相关文章
            Element relatedElement = element.selectFirst("div.gs_fl a[href*='related']");
            if (relatedElement != null) {
                String relatedText = relatedElement.text().trim();
                Integer relatedCount = parseRelatedCount(relatedText);
                if (relatedCount != null) {
                    paper.setRelatedArticles(relatedCount);
                }
                
                String relatedUrl = relatedElement.attr("abs:href");
                if (relatedUrl.isEmpty()) {
                    relatedUrl = relatedElement.attr("href");
                }
                paper.setRelatedUrl(relatedUrl);
            }
            
            // 解析版本信息
            Element versionElement = element.selectFirst("div.gs_fl a[href*='versions']");
            if (versionElement != null) {
                String versionText = versionElement.text().trim();
                Integer versionCount = parseVersionCount(versionText);
                if (versionCount != null) {
                    paper.setVersions(versionCount);
                }
                
                String versionsUrl = versionElement.attr("abs:href");
                if (versionsUrl.isEmpty()) {
                    versionsUrl = versionElement.attr("href");
                }
                paper.setVersionsUrl(versionsUrl);
            }
            
            // 解析PDF链接
            Element pdfElement = element.selectFirst("div.gs_ggs a[href*='.pdf'], div.gs_ggs a[href*='pdf']");
            if (pdfElement != null) {
                String pdfUrl = pdfElement.attr("abs:href");
                if (pdfUrl.isEmpty()) {
                    pdfUrl = pdfElement.attr("href");
                }
                paper.setPdfUrl(pdfUrl);
            }
            
            return paper;
            
        } catch (Exception e) {
            // 记录解析错误但不中断搜索
            System.err.println("Error parsing individual search result: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析作者和期刊信息
     */
    private void parseAuthorAndVenue(GoogleScholarPaper paper, String authorText) {
        try {
            // 使用正则表达式解析作者、期刊和年份
            Pattern pattern = Pattern.compile("([^\\-]+)\\s*-\\s*([^,]+),?\\s*(\\d{4})?");
            Matcher matcher = pattern.matcher(authorText);
            
            if (matcher.find()) {
                String authorsStr = matcher.group(1).trim();
                String venueStr = matcher.group(2).trim();
                String yearStr = matcher.group(3);
                
                // 解析作者
                if (authorsStr != null && !authorsStr.isEmpty()) {
                    String[] authors = authorsStr.split(",");
                    for (String author : authors) {
                        paper.addAuthor(author.trim());
                    }
                }
                
                // 设置期刊/会议
                if (venueStr != null && !venueStr.isEmpty()) {
                    paper.setVenue(venueStr);
                }
                
                // 设置年份
                if (yearStr != null && !yearStr.isEmpty()) {
                    try {
                        paper.setYear(Integer.parseInt(yearStr));
                    } catch (NumberFormatException e) {
                        // 忽略年份解析错误
                    }
                }
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
    }
    
    /**
     * 解析引用次数
     */
    private Integer parseCitationCount(String citationText) {
        try {
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(citationText);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return null;
    }
    
    /**
     * 解析相关文章数量
     */
    private Integer parseRelatedCount(String relatedText) {
        try {
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(relatedText);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return null;
    }
    
    /**
     * 解析版本数量
     */
    private Integer parseVersionCount(String versionText) {
        try {
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(versionText);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return null;
    }
    
    /**
     * 解析总结果数
     */
    private Long parseTotalResults(Document doc) {
        try {
            Element statsElement = doc.selectFirst("#gs_ab_md, .gs_ab_md");
            if (statsElement != null) {
                String statsText = statsElement.text();
                // 提取数字，例如："About 1,000,000 results"
                Pattern pattern = Pattern.compile("([\\d,]+)");
                Matcher matcher = pattern.matcher(statsText);
                if (matcher.find()) {
                    String numberText = matcher.group(1).replace(",", "");
                    return Long.parseLong(numberText);
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
            Elements suggestionElements = doc.select(".gs_suggestions a, .gs_suggestions span");
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
            Elements relatedElements = doc.select(".gs_related_searches a, .related-searches a");
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
