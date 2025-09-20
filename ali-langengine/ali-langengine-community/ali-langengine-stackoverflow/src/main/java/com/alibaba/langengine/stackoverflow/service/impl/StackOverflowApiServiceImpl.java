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
package com.alibaba.langengine.stackoverflow.service.impl;

import com.alibaba.langengine.stackoverflow.model.StackOverflowApiResponse;
import com.alibaba.langengine.stackoverflow.model.StackOverflowSearchRequest;
import com.alibaba.langengine.stackoverflow.model.StackOverflowSearchResult;
import com.alibaba.langengine.stackoverflow.service.StackOverflowApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static com.alibaba.langengine.stackoverflow.StackOverflowConfiguration.*;
import javax.annotation.PreDestroy;


public class StackOverflowApiServiceImpl implements StackOverflowApiService {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StackOverflowApiServiceImpl.class);
    
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final AtomicInteger remainingQuota;
    private final String apiKey;
    private final String baseUrl;
    private final String site;
    private final int timeout;
    private final boolean scrapingEnabled;
    
    public StackOverflowApiServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.apiKey = getApiKey();
        this.baseUrl = getApiBaseUrl();
        this.site = getDefaultSite();
        this.timeout = Integer.parseInt(getApiTimeout()) * 1000;
        this.scrapingEnabled = Boolean.parseBoolean(getEnableScraping());
        this.remainingQuota = new AtomicInteger(-1);
        
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(Integer.parseInt(getApiReadTimeout()) * 1000)
                .build();
                
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setUserAgent("Stack Overflow Search Client/1.0")
                .build();
    }
    
    @Override
    public List<StackOverflowSearchResult> searchQuestions(StackOverflowSearchRequest request) throws Exception {
        if (request == null) {
            throw new Exception("Search request cannot be null");
        }
        
        // Validate site parameter early with white list
        String targetSite = StringUtils.isNotBlank(request.getSite()) ? request.getSite() : site;
        if (StringUtils.isBlank(targetSite)) {
            throw new Exception("Site parameter is required");
        }
        
        // Use white list validation for security
        if (!isValidSite(targetSite)) {
            throw new Exception("Invalid site parameter: " + targetSite + 
                ". Allowed sites: " + getAllowedSites());
        }
        
        try {
            // Try API first
            if (isApiAvailable()) {
                StackOverflowApiResponse apiResponse = searchWithApi(request);
                if (apiResponse != null && apiResponse.getItems() != null) {
                    return apiResponse.getItems();
                }
            }
            
            // Fallback to scraping if enabled
            if (scrapingEnabled) {
                log.info("API unavailable, falling back to web scraping");
                return searchWithScraping(request);
            }
            
            throw new Exception("Both API and scraping are unavailable");
            
        } catch (Exception e) {
            log.error("Error searching Stack Overflow: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public StackOverflowSearchResult getQuestion(Long questionId, boolean includeAnswers) throws Exception {
        String url = buildApiUrl("/questions/" + questionId, null);
        if (includeAnswers) {
            url += (url.contains("?") ? "&" : "?") + "filter=withbody";
        }
        
        try {
            String response = executeHttpRequest(url);
            StackOverflowApiResponse apiResponse = objectMapper.readValue(response, StackOverflowApiResponse.class);
            
            if (apiResponse.getItems() != null && !apiResponse.getItems().isEmpty()) {
                return apiResponse.getItems().get(0);
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Error getting question {}: {}", questionId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public StackOverflowApiResponse searchWithApi(StackOverflowSearchRequest request) throws Exception {
        String url = buildApiUrl("/search/advanced", request);
        
        try {
            String response = executeHttpRequest(url);
            StackOverflowApiResponse apiResponse = objectMapper.readValue(response, StackOverflowApiResponse.class);
            
            // Update quota information
            if (apiResponse.getQuotaRemaining() != null) {
                remainingQuota.set(apiResponse.getQuotaRemaining());
            }
            
            // Check for API errors
            if (apiResponse.getErrorMessage() != null) {
                throw new Exception("API Error: " + apiResponse.getErrorMessage());
            }
            
            return apiResponse;
            
        } catch (Exception e) {
            log.error("Error calling Stack Exchange API: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<StackOverflowSearchResult> searchWithScraping(StackOverflowSearchRequest request) throws Exception {
        String searchUrl = buildSearchUrl(request);
        
        try {
            Document doc = Jsoup.connect(searchUrl)
                    .timeout(timeout)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
                    
            return parseSearchResults(doc);
            
        } catch (org.jsoup.HttpStatusException e) {
            if (e.getStatusCode() == 403) {
                log.warn("Stack Overflow blocked the request (403 Forbidden), likely due to rate limiting. Returning empty results.");
                return new ArrayList<>();
            }
            log.error("HTTP error scraping Stack Overflow: Status={}, URL={}", e.getStatusCode(), searchUrl, e);
            throw e;
        } catch (Exception e) {
            log.error("Error scraping Stack Overflow: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public boolean isApiAvailable() {
        if (remainingQuota.get() == 0) {
            return false;
        }
        
        try {
            String url = buildApiUrl("/info", null);
            executeHttpRequest(url);
            return true;
        } catch (Exception e) {
            log.warn("API availability check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public int getRemainingQuota() {
        return remainingQuota.get();
    }
    
    private String buildApiUrl(String endpoint, StackOverflowSearchRequest request) throws Exception {
        StringBuilder url = new StringBuilder(baseUrl).append(endpoint);
        
        // Use site from request if provided, otherwise use default site
        String targetSite = (request != null && StringUtils.isNotBlank(request.getSite())) ? 
                            request.getSite() : site;
        
        // Validate site parameter with white list
        if (StringUtils.isBlank(targetSite)) {
            throw new Exception("Site parameter is required");
        }
        
        // Use white list validation for security
        if (!isValidSite(targetSite)) {
            throw new Exception("Invalid site parameter: " + targetSite + 
                ". Allowed sites: " + getAllowedSites());
        }
        
        url.append("?site=").append(targetSite);
        
        if (StringUtils.isNotBlank(apiKey)) {
            url.append("&key=").append(apiKey);
        }
        
        if (request != null) {
            if (StringUtils.isNotBlank(request.getQuery())) {
                url.append("&q=").append(URLEncoder.encode(request.getQuery(), StandardCharsets.UTF_8.name()));
            }
            
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                String tags = request.getTags().stream()
                        .map(tag -> tag.startsWith(";") ? tag : ";" + tag)
                        .collect(Collectors.joining(""));
                url.append("&tagged=").append(URLEncoder.encode(tags, StandardCharsets.UTF_8.name()));
            }
            
            if (StringUtils.isNotBlank(request.getSort())) {
                url.append("&sort=").append(request.getSort());
            }
            
            if (StringUtils.isNotBlank(request.getOrder())) {
                url.append("&order=").append(request.getOrder());
            }
            
            if (request.getPageSize() != null) {
                url.append("&pagesize=").append(Math.min(request.getPageSize(), 100));
            }
            
            if (request.getPage() != null) {
                url.append("&page=").append(request.getPage());
            }
            
            if (request.getMinScore() != null) {
                url.append("&min=").append(request.getMinScore());
            }
            
            if (request.getMaxScore() != null) {
                url.append("&max=").append(request.getMaxScore());
            }
            
            if (request.getFromDate() != null) {
                url.append("&fromdate=").append(request.getFromDate());
            }
            
            if (request.getToDate() != null) {
                url.append("&todate=").append(request.getToDate());
            }
            
            if (Boolean.TRUE.equals(request.getAnsweredOnly())) {
                url.append("&answers=1");
            }
            
            if (Boolean.TRUE.equals(request.getAcceptedAnswerOnly())) {
                url.append("&accepted=True");
            }
        }
        
        return url.toString();
    }
    
    private String buildSearchUrl(StackOverflowSearchRequest request) throws Exception {
        StringBuilder url = new StringBuilder("https://stackoverflow.com/search?q=");
        
        if (StringUtils.isNotBlank(request.getQuery())) {
            url.append(URLEncoder.encode(request.getQuery(), StandardCharsets.UTF_8.name()));
        }
        
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tag : request.getTags()) {
                url.append(URLEncoder.encode(" [" + tag + "]", StandardCharsets.UTF_8.name()));
            }
        }
        
        return url.toString();
    }
    
    private String executeHttpRequest(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        request.addHeader("Accept-Encoding", "gzip");
        
        HttpResponse response = httpClient.execute(request);
        byte[] content = EntityUtils.toByteArray(response.getEntity());
        
        // Handle gzip compression
        String encoding = response.getFirstHeader("Content-Encoding") != null ? 
                response.getFirstHeader("Content-Encoding").getValue() : "";
                
        if ("gzip".equals(encoding)) {
            try (GZIPInputStream gzipStream = new GZIPInputStream(new ByteArrayInputStream(content))) {
                content = gzipStream.readAllBytes();
            }
        }
        
        return new String(content, StandardCharsets.UTF_8);
    }
    
    private List<StackOverflowSearchResult> parseSearchResults(Document doc) {
        List<StackOverflowSearchResult> results = new ArrayList<>();
        Elements questions = doc.select(".question-summary");
        
        for (Element question : questions) {
            try {
                StackOverflowSearchResult result = new StackOverflowSearchResult();
                
                // Extract title and link
                Element titleElement = question.select(".question-hyperlink").first();
                if (titleElement != null) {
                    result.setTitle(titleElement.text());
                    result.setLink("https://stackoverflow.com" + titleElement.attr("href"));
                    
                    // Extract question ID from URL
                    Pattern pattern = Pattern.compile("/questions/(\\d+)/");
                    Matcher matcher = pattern.matcher(titleElement.attr("href"));
                    if (matcher.find()) {
                        result.setQuestionId(Long.parseLong(matcher.group(1)));
                    }
                }
                
                // Extract excerpt
                Element excerptElement = question.select(".excerpt").first();
                if (excerptElement != null) {
                    result.setBody(excerptElement.text());
                }
                
                // Extract stats
                Element statsElement = question.select(".statscontainer").first();
                if (statsElement != null) {
                    Element votesElement = statsElement.select(".votes .vote-count-post").first();
                    if (votesElement != null) {
                        try {
                            result.setScore(Integer.parseInt(votesElement.text()));
                        } catch (NumberFormatException e) {
                            result.setScore(0);
                        }
                    }
                    
                    Element answersElement = statsElement.select(".answers").first();
                    if (answersElement != null) {
                        Element answerCount = answersElement.select(".answer-count").first();
                        if (answerCount != null) {
                            try {
                                result.setAnswerCount(Integer.parseInt(answerCount.text()));
                            } catch (NumberFormatException e) {
                                result.setAnswerCount(0);
                            }
                        }
                        
                        result.setIsAnswered(answersElement.hasClass("answered") || 
                                           answersElement.hasClass("answered-accepted"));
                    }
                    
                    Element viewsElement = statsElement.select(".views").first();
                    if (viewsElement != null) {
                        String viewText = viewsElement.attr("title");
                        if (StringUtils.isBlank(viewText)) {
                            viewText = viewsElement.text();
                        }
                        try {
                            String numericViews = viewText.replaceAll("[^0-9]", "");
                            if (!numericViews.isEmpty()) {
                                result.setViewCount(Integer.parseInt(numericViews));
                            }
                        } catch (NumberFormatException e) {
                            result.setViewCount(0);
                        }
                    }
                }
                
                // Extract tags
                Elements tagElements = question.select(".tags .post-tag");
                List<String> tags = new ArrayList<>();
                for (Element tagElement : tagElements) {
                    tags.add(tagElement.text());
                }
                result.setTags(tags);
                
                results.add(result);
                
            } catch (Exception e) {
                log.warn("Error parsing search result: {}", e.getMessage());
            }
        }
        
        return results;
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (httpClient != null) {
                httpClient.close();
                log.info("HTTP client closed successfully");
            }
        } catch (IOException e) {
            log.warn("Error closing HTTP client during cleanup", e);
        }
    }
}
