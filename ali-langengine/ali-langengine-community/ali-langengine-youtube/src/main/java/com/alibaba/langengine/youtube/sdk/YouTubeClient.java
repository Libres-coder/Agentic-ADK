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
package com.alibaba.langengine.youtube.sdk;

import com.alibaba.langengine.youtube.YouTubeConfiguration;
import com.alibaba.langengine.youtube.model.YouTubeSearchRequest;
import com.alibaba.langengine.youtube.model.YouTubeSearchResponse;
import com.alibaba.langengine.youtube.model.YouTubeVideo;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.langengine.youtube.YouTubeConfiguration.*;

/**
 * YouTube 搜索客户端
 * 提供 YouTube 搜索功能
 */
public class YouTubeClient {
    
    private final String userAgent;
    private final int timeoutSeconds;
    private final String language;
    private final String country;
    private final OkHttpClient httpClient;
    
    /**
     * 默认构造函数
     */
    public YouTubeClient() {
        this.userAgent = getDefaultUserAgent();
        this.timeoutSeconds = getDefaultTimeoutSeconds();
        this.language = getDefaultLanguage();
        this.country = getDefaultCountry();
        this.httpClient = createHttpClient();
    }
    
    /**
     * 自定义构造函数
     */
    public YouTubeClient(String userAgent, int timeoutSeconds, String language, String country) {
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
    public YouTubeSearchResponse search(YouTubeSearchRequest request) throws YouTubeException {
        if (request == null || !request.isValid()) {
            throw new YouTubeException("Search request must not be null and query must not be blank");
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
                    throw new YouTubeException("HTTP request failed with status: " + response.code());
                }
                
                String html = response.body().string();
                if (html == null || html.trim().isEmpty()) {
                    throw new YouTubeException("Empty response body");
                }
                
                // 解析HTML响应
                Document doc = Jsoup.parse(html, searchUrl);
                
                // 创建搜索响应
                YouTubeSearchResponse searchResponse = new YouTubeSearchResponse(request.getQuery())
                        .setSearchDuration(System.currentTimeMillis() - startTime)
                        .setSearchUrl(searchUrl);
                
                // 解析搜索结果
                List<YouTubeVideo> videos = parseSearchResults(doc, request.getValidCount());
                searchResponse.setVideos(videos);
                searchResponse.setReturnedCount(videos.size());
                
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
            YouTubeSearchResponse errorResponse = new YouTubeSearchResponse(request.getQuery())
                    .setError("YouTube search failed: " + e.getMessage())
                    .setSearchDuration(duration);
            
            if (e instanceof YouTubeException) {
                throw (YouTubeException) e;
            } else {
                throw new YouTubeException("YouTube search failed: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 简单搜索方法
     */
    public YouTubeSearchResponse search(String query) throws YouTubeException {
        YouTubeSearchRequest request = new YouTubeSearchRequest(query);
        return search(request);
    }
    
    /**
     * 带结果数量的搜索方法
     */
    public YouTubeSearchResponse search(String query, int count) throws YouTubeException {
        YouTubeSearchRequest request = new YouTubeSearchRequest(query)
                .setCount(count);
        return search(request);
    }
    
    /**
     * 解析搜索结果
     */
    private List<YouTubeVideo> parseSearchResults(Document doc, int maxCount) {
        List<YouTubeVideo> videos = new ArrayList<>();
        
        try {
            // YouTube 搜索结果的选择器
            Elements resultElements = doc.select("div#contents ytd-video-renderer, div#contents ytd-playlist-renderer");
            
            for (Element element : resultElements) {
                if (videos.size() >= maxCount) {
                    break;
                }
                
                YouTubeVideo video = parseSearchResult(element);
                if (video != null && video.isValid()) {
                    videos.add(video);
                }
            }
            
        } catch (Exception e) {
            // 记录解析错误但不中断搜索
            System.err.println("Error parsing search results: " + e.getMessage());
        }
        
        return videos;
    }
    
    /**
     * 解析单个搜索结果
     */
    private YouTubeVideo parseSearchResult(Element element) {
        try {
            YouTubeVideo video = new YouTubeVideo();
            
            // 解析视频ID
            Element linkElement = element.selectFirst("a#video-title, a#playlist-title");
            if (linkElement != null) {
                String href = linkElement.attr("href");
                String videoId = extractVideoId(href);
                if (videoId != null) {
                    video.setVideoId(videoId);
                    video.setVideoUrl("https://www.youtube.com/watch?v=" + videoId);
                }
                
                // 解析标题
                String title = linkElement.attr("title");
                if (title.isEmpty()) {
                    title = linkElement.text().trim();
                }
                video.setTitle(title);
            }
            
            // 解析频道信息
            Element channelElement = element.selectFirst("a.yt-simple-endpoint.style-scope.yt-formatted-string");
            if (channelElement != null) {
                String channelName = channelElement.text().trim();
                video.setChannelName(channelName);
                
                String channelHref = channelElement.attr("href");
                if (channelHref.startsWith("/channel/")) {
                    video.setChannelId(channelHref.substring("/channel/".length()));
                    video.setChannelUrl("https://www.youtube.com" + channelHref);
                } else if (channelHref.startsWith("/@")) {
                    video.setChannelUrl("https://www.youtube.com" + channelHref);
                }
            }
            
            // 解析观看次数和上传时间
            Element metaElement = element.selectFirst("span.style-scope.ytd-video-meta-block");
            if (metaElement != null) {
                String metaText = metaElement.text().trim();
                parseVideoMetadata(video, metaText);
            }
            
            // 解析缩略图
            Element thumbnailElement = element.selectFirst("img");
            if (thumbnailElement != null) {
                String thumbnailUrl = thumbnailElement.attr("src");
                if (thumbnailUrl.startsWith("//")) {
                    thumbnailUrl = "https:" + thumbnailUrl;
                }
                video.setThumbnailUrl(thumbnailUrl);
                
                // 尝试获取高分辨率缩略图
                String highResUrl = thumbnailUrl.replace("hqdefault", "maxresdefault");
                video.setHighResThumbnailUrl(highResUrl);
            }
            
            // 解析描述
            Element descElement = element.selectFirst("span#description-text");
            if (descElement != null) {
                String description = descElement.text().trim();
                video.setDescription(description);
            }
            
            // 解析时长
            Element durationElement = element.selectFirst("span.style-scope.ytd-thumbnail-overlay-time-status-renderer");
            if (durationElement != null) {
                String durationText = durationElement.text().trim();
                int duration = parseDurationToSeconds(durationText);
                video.setDuration(duration);
            }
            
            return video;
            
        } catch (Exception e) {
            // 记录解析错误但不中断搜索
            System.err.println("Error parsing individual search result: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从URL中提取视频ID
     */
    private String extractVideoId(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        
        Pattern pattern = Pattern.compile("(?:v=|/v/|/embed/|/watch\\?.*v=)([a-zA-Z0-9_-]{11})");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * 解析视频元数据
     */
    private void parseVideoMetadata(YouTubeVideo video, String metaText) {
        try {
            // 解析观看次数和上传时间
            // 格式通常是: "观看次数 上传时间" 或 "观看次数 • 上传时间"
            String[] parts = metaText.split("•|\\s+");
            
            for (String part : parts) {
                part = part.trim();
                
                // 解析观看次数
                if (part.contains("观看") || part.contains("views") || part.contains("次")) {
                    Long viewCount = parseViewCount(part);
                    if (viewCount > 0) {
                        video.setViewCount(viewCount);
                    }
                }
                
                // 解析上传时间
                if (part.contains("前") || part.contains("ago") || 
                    part.contains("年") || part.contains("月") || part.contains("日") ||
                    part.contains("year") || part.contains("month") || part.contains("day")) {
                    LocalDateTime uploadDate = parseUploadDate(part);
                    if (uploadDate != null) {
                        video.setUploadDate(uploadDate);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
    }
    
    /**
     * 解析观看次数
     */
    private Long parseViewCount(String viewText) {
        try {
            // 移除所有非数字和单位字符
            String cleaned = viewText.replaceAll("[^0-9.,kmb]", "").toLowerCase();
            
            if (cleaned.endsWith("b")) {
                return (long) (Double.parseDouble(cleaned.substring(0, cleaned.length() - 1)) * 1000000000);
            } else if (cleaned.endsWith("m")) {
                return (long) (Double.parseDouble(cleaned.substring(0, cleaned.length() - 1)) * 1000000);
            } else if (cleaned.endsWith("k")) {
                return (long) (Double.parseDouble(cleaned.substring(0, cleaned.length() - 1)) * 1000);
            } else {
                return Long.parseLong(cleaned.replace(",", ""));
            }
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    /**
     * 解析上传时间
     */
    private LocalDateTime parseUploadDate(String dateText) {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            if (dateText.contains("小时") || dateText.contains("hour")) {
                Pattern pattern = Pattern.compile("(\\d+)");
                Matcher matcher = pattern.matcher(dateText);
                if (matcher.find()) {
                    int hours = Integer.parseInt(matcher.group(1));
                    return now.minusHours(hours);
                }
            } else if (dateText.contains("天") || dateText.contains("day")) {
                Pattern pattern = Pattern.compile("(\\d+)");
                Matcher matcher = pattern.matcher(dateText);
                if (matcher.find()) {
                    int days = Integer.parseInt(matcher.group(1));
                    return now.minusDays(days);
                }
            } else if (dateText.contains("周") || dateText.contains("week")) {
                Pattern pattern = Pattern.compile("(\\d+)");
                Matcher matcher = pattern.matcher(dateText);
                if (matcher.find()) {
                    int weeks = Integer.parseInt(matcher.group(1));
                    return now.minusWeeks(weeks);
                }
            } else if (dateText.contains("月") || dateText.contains("month")) {
                Pattern pattern = Pattern.compile("(\\d+)");
                Matcher matcher = pattern.matcher(dateText);
                if (matcher.find()) {
                    int months = Integer.parseInt(matcher.group(1));
                    return now.minusMonths(months);
                }
            } else if (dateText.contains("年") || dateText.contains("year")) {
                Pattern pattern = Pattern.compile("(\\d+)");
                Matcher matcher = pattern.matcher(dateText);
                if (matcher.find()) {
                    int years = Integer.parseInt(matcher.group(1));
                    return now.minusYears(years);
                }
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
            Element statsElement = doc.selectFirst("#result-count, .results-count");
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
            Elements relatedElements = doc.select(".related-searches a, .related-queries a");
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
