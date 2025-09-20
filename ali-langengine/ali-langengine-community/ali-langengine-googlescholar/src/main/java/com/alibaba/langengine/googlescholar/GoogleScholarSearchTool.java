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
package com.alibaba.langengine.googlescholar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.model.BaseTool;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.alibaba.langengine.googlescholar.model.GoogleScholarSearchRequest;
import com.alibaba.langengine.googlescholar.model.GoogleScholarSearchResponse;
import com.alibaba.langengine.googlescholar.sdk.GoogleScholarClient;
import com.alibaba.langengine.googlescholar.sdk.GoogleScholarException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Google Scholar 搜索工具
 * 提供学术论文搜索功能
 */
@Slf4j
public class GoogleScholarSearchTool extends BaseTool {
    
    private GoogleScholarClient client;
    
    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"query\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(required) The search query for academic papers\"\n" +
            "\t\t},\n" +
            "\t\t\"count\": {\n" +
            "\t\t\t\"type\": \"integer\",\n" +
            "\t\t\t\"description\": \"(optional) The number of results to return. Default is 10, max is 100.\",\n" +
            "\t\t\t\"default\": 10,\n" +
            "\t\t\t\"minimum\": 1,\n" +
            "\t\t\t\"maximum\": 100\n" +
            "\t\t},\n" +
            "\t\t\"sortBy\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Sort results by relevance, date, or citations. Default is relevance.\",\n" +
            "\t\t\t\"enum\": [\"relevance\", \"date\", \"citations\"],\n" +
            "\t\t\t\"default\": \"relevance\"\n" +
            "\t\t},\n" +
            "\t\t\"timeRange\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Time range filter: any, y (past year), 2y (past 2 years), 5y (past 5 years), 10y (past 10 years). Default is any.\",\n" +
            "\t\t\t\"enum\": [\"any\", \"y\", \"2y\", \"5y\", \"10y\"],\n" +
            "\t\t\t\"default\": \"any\"\n" +
            "\t\t},\n" +
            "\t\t\"documentType\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Document type filter: any, articles, theses, books, patents, case_law. Default is any.\",\n" +
            "\t\t\t\"enum\": [\"any\", \"articles\", \"theses\", \"books\", \"patents\", \"case_law\"],\n" +
            "\t\t\t\"default\": \"any\"\n" +
            "\t\t},\n" +
            "\t\t\"author\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Filter by author name\"\n" +
            "\t\t},\n" +
            "\t\t\"journal\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Filter by journal or conference name\"\n" +
            "\t\t},\n" +
            "\t\t\"language\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Language filter (e.g., en, zh, fr, de). Default is en.\",\n" +
            "\t\t\t\"default\": \"en\"\n" +
            "\t\t},\n" +
            "\t\t\"country\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(optional) Country/region filter (e.g., us, cn, uk, de). Default is us.\",\n" +
            "\t\t\t\"default\": \"us\"\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"query\"]\n" +
            "}";
    
    public GoogleScholarSearchTool() {
        setName("google_scholar_search");
        setDescription("Search for academic papers using Google Scholar. " +
                "Use this tool when you need to find scholarly articles, research papers, theses, books, or patents. " +
                "The tool returns detailed information including title, authors, publication venue, abstract, citation count, and links to PDFs and full texts.");
        
        setParameters(PARAMETERS);
        
        // 初始化客户端
        this.client = new GoogleScholarClient();
    }
    
    /**
     * 自定义构造函数
     */
    public GoogleScholarSearchTool(GoogleScholarClient client) {
        setName("google_scholar_search");
        setDescription("Search for academic papers using Google Scholar. " +
                "Use this tool when you need to find scholarly articles, research papers, theses, books, or patents. " +
                "The tool returns detailed information including title, authors, publication venue, abstract, citation count, and links to PDFs and full texts.");
        
        setParameters(PARAMETERS);
        this.client = client;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("Google Scholar 搜索开始，输入: {}", toolInput);
        
        try {
            // 解析输入参数
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            
            // 验证必需参数
            if (!inputMap.containsKey("query") || inputMap.get("query") == null) {
                return new ToolExecuteResult("错误: 缺少必需的查询参数 'query'");
            }
            
            // 构建搜索请求
            GoogleScholarSearchRequest request = buildSearchRequest(inputMap);
            
            // 执行搜索
            GoogleScholarSearchResponse response = client.search(request);
            
            // 格式化结果
            String result = formatSearchResults(response);
            
            log.info("Google Scholar 搜索完成，返回 {} 个结果", response.getReturnedCount());
            
            return new ToolExecuteResult(result);
            
        } catch (GoogleScholarException e) {
            log.error("Google Scholar 搜索失败: {}", e.getMessage(), e);
            return new ToolExecuteResult("Google Scholar 搜索失败: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("Google Scholar 搜索过程中发生未知错误: {}", e.getMessage(), e);
            return new ToolExecuteResult("搜索过程中发生未知错误，请稍后重试");
        }
    }
    
    /**
     * 构建搜索请求
     */
    private GoogleScholarSearchRequest buildSearchRequest(Map<String, Object> inputMap) {
        GoogleScholarSearchRequest request = new GoogleScholarSearchRequest(
                inputMap.get("query").toString()
        );
        
        // 设置可选参数
        if (inputMap.containsKey("count")) {
            try {
                int count = Integer.parseInt(inputMap.get("count").toString());
                request.setCount(count);
            } catch (NumberFormatException e) {
                log.warn("Invalid count parameter: {}", inputMap.get("count"));
            }
        }
        
        if (inputMap.containsKey("sortBy")) {
            request.setSortBy(inputMap.get("sortBy").toString());
        }
        
        if (inputMap.containsKey("timeRange")) {
            request.setTimeRange(inputMap.get("timeRange").toString());
        }
        
        if (inputMap.containsKey("documentType")) {
            request.setDocumentType(inputMap.get("documentType").toString());
        }
        
        if (inputMap.containsKey("author")) {
            request.setAuthor(inputMap.get("author").toString());
        }
        
        if (inputMap.containsKey("journal")) {
            request.setJournal(inputMap.get("journal").toString());
        }
        
        if (inputMap.containsKey("language")) {
            request.setLanguage(inputMap.get("language").toString());
        }
        
        if (inputMap.containsKey("country")) {
            request.setCountry(inputMap.get("country").toString());
        }
        
        return request;
    }
    
    /**
     * 格式化搜索结果
     */
    private String formatSearchResults(GoogleScholarSearchResponse response) {
        if (!response.isSuccessful()) {
            return "搜索失败: " + (response.getErrorMessage() != null ? response.getErrorMessage() : "未知错误");
        }
        
        if (response.isEmpty()) {
            return "未找到相关论文: " + response.getQuery();
        }
        
        StringBuilder result = new StringBuilder();
        result.append("=== Google Scholar 搜索结果 ===\n");
        result.append(response.getSummary()).append("\n\n");
        
        // 添加论文列表
        for (int i = 0; i < response.getPapers().size(); i++) {
            var paper = response.getPapers().get(i);
            result.append("[").append(i + 1).append("] ").append(paper.getTitle()).append("\n");
            
            if (paper.getAuthors() != null && !paper.getAuthors().isEmpty()) {
                result.append("作者: ").append(paper.getAuthorsString()).append("\n");
            }
            
            if (paper.getYear() != null) {
                result.append("年份: ").append(paper.getYear());
            }
            
            if (paper.getVenue() != null && !paper.getVenue().trim().isEmpty()) {
                result.append(" | 期刊/会议: ").append(paper.getVenue());
            }
            
            if (paper.getCitations() != null && paper.getCitations() > 0) {
                result.append(" | 引用: ").append(paper.getCitations());
            }
            
            result.append("\n");
            
            if (paper.getAbstractText() != null && !paper.getAbstractText().trim().isEmpty()) {
                result.append("摘要: ").append(paper.getShortAbstract()).append("\n");
            }
            
            if (paper.getPdfUrl() != null && !paper.getPdfUrl().trim().isEmpty()) {
                result.append("PDF: ").append(paper.getPdfUrl()).append("\n");
            }
            
            if (paper.getDetailUrl() != null && !paper.getDetailUrl().trim().isEmpty()) {
                result.append("详情: ").append(paper.getDetailUrl()).append("\n");
            }
            
            result.append("\n");
        }
        
        // 添加搜索建议
        if (response.getSuggestions() != null && !response.getSuggestions().isEmpty()) {
            result.append("搜索建议:\n");
            for (String suggestion : response.getSuggestions()) {
                result.append("- ").append(suggestion).append("\n");
            }
            result.append("\n");
        }
        
        // 添加相关搜索
        if (response.getRelatedQueries() != null && !response.getRelatedQueries().isEmpty()) {
            result.append("相关搜索:\n");
            for (String query : response.getRelatedQueries()) {
                result.append("- ").append(query).append("\n");
            }
            result.append("\n");
        }
        
        return result.toString();
    }
    
    /**
     * 设置客户端（用于测试）
     */
    public void setClient(GoogleScholarClient client) {
        this.client = client;
    }
    
    /**
     * 获取客户端
     */
    public GoogleScholarClient getClient() {
        return client;
    }
}
