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
package com.alibaba.langengine.pubmed;

import com.alibaba.langengine.pubmed.model.PubMedArticle;
import com.alibaba.langengine.pubmed.model.PubMedSearchRequest;
import com.alibaba.langengine.pubmed.model.PubMedSearchResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class PubMedClient {

    /**
     * HTTP状态码常量
     */
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_FORBIDDEN = 403;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    private static final int HTTP_BAD_GATEWAY = 502;
    private static final int HTTP_SERVICE_UNAVAILABLE = 503;

    private final PubMedConfiguration configuration;
    private final OkHttpClient httpClient;
    private final XmlMapper xmlMapper;
    private LocalDateTime lastRequestTime;

    /**
     * 构造函数
     *
     * @param configuration PubMed配置
     */
    public PubMedClient(PubMedConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("PubMed configuration cannot be null");
        }
        
        if (!configuration.validateConfiguration()) {
            throw new IllegalArgumentException("Invalid PubMed configuration");
        }

        this.configuration = configuration;
        this.httpClient = createHttpClient();
        this.xmlMapper = new XmlMapper();
        this.lastRequestTime = null;

        log.info("PubMed client initialized: {}", configuration.getConfigurationSummary());
    }

    /**
     * 创建HTTP客户端
     *
     * @return OkHttpClient实例
     */
    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(configuration.getTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(configuration.getTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(configuration.getTimeoutSeconds(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * 搜索PubMed文章
     *
     * @param request 搜索请求
     * @return 搜索响应
     * @throws PubMedClientException 搜索异常
     */
    public PubMedSearchResponse search(PubMedSearchRequest request) throws PubMedClientException {
        if (request == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }

        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid search request parameters");
        }

        try {
            log.info("Searching PubMed with query: {}", request.getQuery());

            // 执行搜索，获取ID列表
            PubMedSearchResponse searchResponse = executeESearch(request);
            
            if (!searchResponse.isSuccessful()) {
                log.warn("PubMed search failed: {}", searchResponse.getErrorMessage());
                return searchResponse;
            }

            if (!searchResponse.hasResults()) {
                log.info("No results found for query: {}", request.getQuery());
                return searchResponse;
            }

            // 获取文章详细信息
            List<PubMedArticle> articles = fetchArticleDetails(searchResponse.getIdList());
            searchResponse.addArticles(articles);

            log.info("PubMed search completed: {}", searchResponse.getSummary());
            return searchResponse;

        } catch (Exception e) {
            String errorMsg = "Failed to search PubMed: " + e.getMessage();
            log.error(errorMsg, e);
            throw new PubMedClientException(errorMsg, e);
        }
    }

    /**
     * 执行ESearch API调用
     *
     * @param request 搜索请求
     * @return 搜索响应
     * @throws PubMedClientException 搜索异常
     */
    private PubMedSearchResponse executeESearch(PubMedSearchRequest request) throws PubMedClientException {
        String url = buildESearchUrl(request);
        
        for (int attempt = 1; attempt <= configuration.getMaxRetries() + 1; attempt++) {
            try {
                // 应用速率限制
                enforceRateLimit();

                Request httpRequest = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", configuration.getUserAgent())
                        .build();

                try (Response response = httpClient.newCall(httpRequest).execute()) {
                    handleHttpResponse(response, "ESearch");
                    
                    ResponseBody body = response.body();
                    if (body == null) {
                        throw new PubMedClientException("Empty response from PubMed ESearch API");
                    }

                    String responseText = body.string();
                    return parseESearchResponse(responseText);
                }

            } catch (PubMedClientException e) {
                if (attempt > configuration.getMaxRetries()) {
                    throw e;
                }
                log.warn("ESearch attempt {} failed, retrying: {}", attempt, e.getMessage());
                sleep(1000 * attempt);
            } catch (Exception e) {
                if (attempt > configuration.getMaxRetries()) {
                    throw new PubMedClientException("ESearch failed after " + configuration.getMaxRetries() + " retries", e);
                }
                log.warn("ESearch attempt {} failed, retrying: {}", attempt, e.getMessage());
                sleep(1000 * attempt);
            }
        }
        
        throw new PubMedClientException("ESearch failed after all retry attempts");
    }

    /**
     * 构建ESearch URL
     *
     * @param request 搜索请求
     * @return 完整的ESearch URL
     */
    private String buildESearchUrl(PubMedSearchRequest request) {
        StringBuilder url = new StringBuilder(configuration.getESearchBaseUrl());
        url.append("?").append(request.getESearchParams());
        
        if (configuration.hasEmail()) {
            url.append("&email=").append(java.net.URLEncoder.encode(configuration.getEmail(), java.nio.charset.StandardCharsets.UTF_8));
        }
        
        if (configuration.hasApiKey()) {
            url.append("&api_key=").append(configuration.getApiKey());
        }
        
        return url.toString();
    }

    /**
     * 解析ESearch响应
     *
     * @param responseText 响应文本
     * @return 搜索响应对象
     * @throws PubMedClientException 解析异常
     */
    private PubMedSearchResponse parseESearchResponse(String responseText) throws PubMedClientException {
        try {
            // 解析XML响应
            PubMedSearchResponse response = xmlMapper.readValue(responseText, PubMedSearchResponse.class);
            
            // 如果解析失败，尝试手动解析关键信息
            if (response.getIdList() == null || response.getIdList().isEmpty()) {
                response = parseESearchResponseManually(responseText);
            }
            
            return response;
        } catch (Exception e) {
            log.error("Failed to parse ESearch response: {}", responseText);
            throw new PubMedClientException("Failed to parse ESearch response", e);
        }
    }

    /**
     * 手动解析ESearch响应（备用方法）
     *
     * @param responseText 响应文本
     * @return 搜索响应对象
     * @throws PubMedClientException 解析异常
     */
    private PubMedSearchResponse parseESearchResponseManually(String responseText) throws PubMedClientException {
        try {
            PubMedSearchResponse response = new PubMedSearchResponse();
            
            // 解析Count
            Pattern countPattern = Pattern.compile("<Count>(\\d+)</Count>");
            Matcher countMatcher = countPattern.matcher(responseText);
            if (countMatcher.find()) {
                response.setCount(Integer.parseInt(countMatcher.group(1)));
            }
            
            // 解析RetMax
            Pattern retMaxPattern = Pattern.compile("<RetMax>(\\d+)</RetMax>");
            Matcher retMaxMatcher = retMaxPattern.matcher(responseText);
            if (retMaxMatcher.find()) {
                response.setRetMax(Integer.parseInt(retMaxMatcher.group(1)));
            }
            
            // 解析RetStart
            Pattern retStartPattern = Pattern.compile("<RetStart>(\\d+)</RetStart>");
            Matcher retStartMatcher = retStartPattern.matcher(responseText);
            if (retStartMatcher.find()) {
                response.setRetStart(Integer.parseInt(retStartMatcher.group(1)));
            }
            
            // 解析ID列表
            Pattern idPattern = Pattern.compile("<Id>(\\d+)</Id>");
            Matcher idMatcher = idPattern.matcher(responseText);
            List<String> idList = new ArrayList<>();
            while (idMatcher.find()) {
                idList.add(idMatcher.group(1));
            }
            response.setIdList(idList);
            
            return response;
        } catch (Exception e) {
            throw new PubMedClientException("Failed to manually parse ESearch response", e);
        }
    }

    /**
     * 获取文章详细信息
     *
     * @param pmidList PubMed ID列表
     * @return 文章列表
     * @throws PubMedClientException 获取异常
     */
    private List<PubMedArticle> fetchArticleDetails(List<String> pmidList) throws PubMedClientException {
        if (pmidList == null || pmidList.isEmpty()) {
            return new ArrayList<>();
        }

        List<PubMedArticle> articles = new ArrayList<>();
        
        // 分批获取文章详情（每批最多200个）
        final int batchSize = 200;
        for (int i = 0; i < pmidList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, pmidList.size());
            List<String> batchIds = pmidList.subList(i, endIndex);
            
            List<PubMedArticle> batchArticles = fetchArticleDetailsBatch(batchIds);
            articles.addAll(batchArticles);
        }
        
        return articles;
    }

    /**
     * 批量获取文章详细信息
     *
     * @param pmidList PubMed ID列表
     * @return 文章列表
     * @throws PubMedClientException 获取异常
     */
    private List<PubMedArticle> fetchArticleDetailsBatch(List<String> pmidList) throws PubMedClientException {
        String url = buildEFetchUrl(pmidList);
        
        for (int attempt = 1; attempt <= configuration.getMaxRetries() + 1; attempt++) {
            try {
                // 应用速率限制
                enforceRateLimit();

                Request httpRequest = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", configuration.getUserAgent())
                        .build();

                try (Response response = httpClient.newCall(httpRequest).execute()) {
                    handleHttpResponse(response, "EFetch");
                    
                    ResponseBody body = response.body();
                    if (body == null) {
                        throw new PubMedClientException("Empty response from PubMed EFetch API");
                    }

                    String responseText = body.string();
                    return parseEFetchResponse(responseText, pmidList);
                }

            } catch (PubMedClientException e) {
                if (attempt > configuration.getMaxRetries()) {
                    throw e;
                }
                log.warn("EFetch attempt {} failed, retrying: {}", attempt, e.getMessage());
                sleep(1000 * attempt);
            } catch (Exception e) {
                if (attempt > configuration.getMaxRetries()) {
                    throw new PubMedClientException("EFetch failed after " + configuration.getMaxRetries() + " retries", e);
                }
                log.warn("EFetch attempt {} failed, retrying: {}", attempt, e.getMessage());
                sleep(1000 * attempt);
            }
        }
        
        throw new PubMedClientException("EFetch failed after all retry attempts");
    }

    /**
     * 构建EFetch URL
     *
     * @param pmidList PubMed ID列表
     * @return 完整的EFetch URL
     */
    private String buildEFetchUrl(List<String> pmidList) {
        StringBuilder url = new StringBuilder(configuration.getEFetchBaseUrl());
        url.append("?db=").append(configuration.getDefaultDatabase());
        url.append("&rettype=").append(configuration.getDefaultReturnType());
        url.append("&retmode=").append(configuration.getDefaultReturnMode());
        url.append("&id=").append(String.join(",", pmidList));
        
        if (configuration.hasEmail()) {
            url.append("&email=").append(java.net.URLEncoder.encode(configuration.getEmail(), java.nio.charset.StandardCharsets.UTF_8));
        }
        
        if (configuration.hasApiKey()) {
            url.append("&api_key=").append(configuration.getApiKey());
        }
        
        return url.toString();
    }

    /**
     * 解析EFetch响应
     *
     * @param responseText 响应文本
     * @param pmidList 请求的PMID列表
     * @return 文章列表
     * @throws PubMedClientException 解析异常
     */
    private List<PubMedArticle> parseEFetchResponse(String responseText, List<String> pmidList) throws PubMedClientException {
        try {
            List<PubMedArticle> articles = new ArrayList<>();
            
            // 简单的XML解析（提取基本信息）
            for (String pmid : pmidList) {
                PubMedArticle article = parseArticleFromXml(responseText, pmid);
                if (article != null) {
                    articles.add(article);
                }
            }
            
            return articles;
        } catch (Exception e) {
            log.error("Failed to parse EFetch response");
            throw new PubMedClientException("Failed to parse EFetch response", e);
        }
    }

    /**
     * 从XML中解析单个文章信息
     *
     * @param xml XML文本
     * @param pmid PubMed ID
     * @return 文章对象
     */
    private PubMedArticle parseArticleFromXml(String xml, String pmid) {
        try {
            PubMedArticle.PubMedArticleBuilder builder = PubMedArticle.builder().pmid(pmid);
            
            // 提取标题
            String title = extractXmlValue(xml, "ArticleTitle");
            if (StringUtils.isNotBlank(title)) {
                builder.title(cleanText(title));
            }
            
            // 提取摘要
            String abstractText = extractXmlValue(xml, "AbstractText");
            if (StringUtils.isNotBlank(abstractText)) {
                builder.abstractText(cleanText(abstractText));
            }
            
            // 提取期刊名称
            String journal = extractXmlValue(xml, "Title");
            if (StringUtils.isNotBlank(journal)) {
                builder.journal(cleanText(journal));
            }
            
            // 设置URL
            builder.url("https://pubmed.ncbi.nlm.nih.gov/" + pmid + "/");
            
            return builder.build();
        } catch (Exception e) {
            log.warn("Failed to parse article for PMID {}: {}", pmid, e.getMessage());
            return null;
        }
    }

    /**
     * 从XML中提取指定标签的值
     *
     * @param xml XML文本
     * @param tagName 标签名称
     * @return 标签值
     */
    private String extractXmlValue(String xml, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 清理文本内容
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    private String cleanText(String text) {
        if (text == null) {
            return null;
        }
        
        // 移除HTML标签
        text = text.replaceAll("<[^>]*>", "");
        
        // 移除多余的空白字符
        text = text.replaceAll("\\s+", " ");
        
        return text.trim();
    }

    /**
     * 处理HTTP响应
     *
     * @param response HTTP响应
     * @param operation 操作名称
     * @throws PubMedClientException 响应异常
     */
    private void handleHttpResponse(Response response, String operation) throws PubMedClientException {
        int statusCode = response.code();
        String statusMessage = response.message();

        if (statusCode == HTTP_OK) {
            return;
        }

        String errorMessage = String.format("%s failed with HTTP %d: %s", operation, statusCode, statusMessage);

        switch (statusCode) {
            case HTTP_BAD_REQUEST:
                throw new PubMedClientException("Bad request - check your search parameters: " + errorMessage);
            case HTTP_UNAUTHORIZED:
                throw new PubMedClientException("Unauthorized - check your API credentials: " + errorMessage);
            case HTTP_FORBIDDEN:
                throw new PubMedClientException("Forbidden - access denied: " + errorMessage);
            case HTTP_NOT_FOUND:
                throw new PubMedClientException("Resource not found: " + errorMessage);
            case HTTP_TOO_MANY_REQUESTS:
                throw new PubMedClientException("Rate limit exceeded - please slow down requests: " + errorMessage);
            case HTTP_INTERNAL_SERVER_ERROR:
            case HTTP_BAD_GATEWAY:
            case HTTP_SERVICE_UNAVAILABLE:
                throw new PubMedClientException("PubMed service temporarily unavailable: " + errorMessage);
            default:
                throw new PubMedClientException("Unexpected HTTP error: " + errorMessage);
        }
    }

    /**
     * 应用速率限制
     */
    private void enforceRateLimit() {
        if (lastRequestTime != null) {
            long timeSinceLastRequest = java.time.Duration.between(lastRequestTime, LocalDateTime.now()).toMillis();
            if (timeSinceLastRequest < configuration.getRequestIntervalMs()) {
                long sleepTime = configuration.getRequestIntervalMs() - timeSinceLastRequest;
                sleep(sleepTime);
            }
        }
        lastRequestTime = LocalDateTime.now();
    }

    /**
     * 线程休眠
     *
     * @param milliseconds 休眠时间（毫秒）
     */
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep interrupted", e);
        }
    }

    /**
     * 强制执行速率限制
     * 
     * @throws InterruptedException 如果线程在等待期间被中断
     */
    public synchronized void enforceRateLimit() throws InterruptedException {
        if (lastRequestTime != null) {
            long elapsedTime = System.currentTimeMillis() - lastRequestTime.toEpochSecond(ZoneOffset.UTC) * 1000;
            long minInterval = (long) (1000.0 / configuration.getRequestsPerSecond());
            
            if (elapsedTime < minInterval) {
                long waitTime = minInterval - elapsedTime;
                Thread.sleep(waitTime);
            }
        }
        lastRequestTime = LocalDateTime.now();
    }

    /**
     * 获取配置信息
     *
     * @return 配置对象
     */
    public PubMedConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 关闭客户端资源
     */
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
        log.info("PubMed client closed");
    }
}
