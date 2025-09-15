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
package com.alibaba.langengine.googlescholar.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * Google Scholar 搜索响应模型
 * 封装搜索结果和元数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleScholarSearchResponse {
    
    /**
     * 搜索查询
     */
    private String query;
    
    /**
     * 搜索结果列表
     */
    private List<GoogleScholarPaper> papers;
    
    /**
     * 总结果数
     */
    private Long totalResults;
    
    /**
     * 返回结果数
     */
    private Integer returnedCount;
    
    /**
     * 搜索耗时（毫秒）
     */
    private Long searchDuration;
    
    /**
     * 是否成功
     */
    private Boolean successful;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 搜索建议
     */
    private List<String> suggestions;
    
    /**
     * 相关搜索
     */
    private List<String> relatedQueries;
    
    /**
     * 搜索URL
     */
    private String searchUrl;
    
    /**
     * 是否有更多结果
     */
    private Boolean hasMoreResults;
    
    /**
     * 下一页URL
     */
    private String nextPageUrl;
    
    /**
     * 搜索时间戳
     */
    private Long timestamp;
    
    /**
     * 构造函数
     */
    public GoogleScholarSearchResponse(String query) {
        this.query = query;
        this.papers = new ArrayList<>();
        this.suggestions = new ArrayList<>();
        this.relatedQueries = new ArrayList<>();
        this.successful = true;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 添加论文
     */
    public void addPaper(GoogleScholarPaper paper) {
        if (papers == null) {
            papers = new ArrayList<>();
        }
        if (paper != null && paper.isValid()) {
            papers.add(paper);
        }
    }
    
    /**
     * 添加搜索建议
     */
    public void addSuggestion(String suggestion) {
        if (suggestions == null) {
            suggestions = new ArrayList<>();
        }
        if (suggestion != null && !suggestion.trim().isEmpty()) {
            suggestions.add(suggestion.trim());
        }
    }
    
    /**
     * 添加相关搜索
     */
    public void addRelatedQuery(String query) {
        if (relatedQueries == null) {
            relatedQueries = new ArrayList<>();
        }
        if (query != null && !query.trim().isEmpty()) {
            relatedQueries.add(query.trim());
        }
    }
    
    /**
     * 检查是否为空结果
     */
    public boolean isEmpty() {
        return papers == null || papers.isEmpty();
    }
    
    /**
     * 检查是否成功
     */
    public boolean isSuccessful() {
        return successful != null && successful;
    }
    
    /**
     * 设置错误信息
     */
    public void setError(String errorMessage) {
        this.errorMessage = errorMessage;
        this.successful = false;
    }
    
    /**
     * 获取结果摘要
     */
    public String getSummary() {
        if (!isSuccessful()) {
            return "搜索失败: " + (errorMessage != null ? errorMessage : "未知错误");
        }
        
        if (isEmpty()) {
            return "未找到相关论文: " + query;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(returnedCount != null ? returnedCount : papers.size());
        
        if (totalResults != null && totalResults > 0) {
            sb.append(" 条结果（共 ").append(totalResults).append(" 条）");
        } else {
            sb.append(" 条结果");
        }
        
        if (searchDuration != null) {
            sb.append("，耗时 ").append(searchDuration).append(" 毫秒");
        }
        
        return sb.toString();
    }
    
    /**
     * 获取格式化结果
     */
    public String getFormattedResults() {
        if (!isSuccessful()) {
            return "搜索失败: " + (errorMessage != null ? errorMessage : "未知错误");
        }
        
        if (isEmpty()) {
            return "未找到相关论文: " + query;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Google Scholar 搜索结果 ===\n");
        sb.append(getSummary()).append("\n\n");
        
        for (int i = 0; i < papers.size(); i++) {
            GoogleScholarPaper paper = papers.get(i);
            sb.append("[").append(i + 1).append("] ").append(paper.getTitle()).append("\n");
            
            if (paper.getAuthors() != null && !paper.getAuthors().isEmpty()) {
                sb.append("作者: ").append(paper.getAuthorsString()).append("\n");
            }
            
            if (paper.getYear() != null) {
                sb.append("年份: ").append(paper.getYear()).append("\n");
            }
            
            if (paper.getVenue() != null && !paper.getVenue().trim().isEmpty()) {
                sb.append("期刊/会议: ").append(paper.getVenue()).append("\n");
            }
            
            if (paper.getCitations() != null && paper.getCitations() > 0) {
                sb.append("引用次数: ").append(paper.getCitations()).append("\n");
            }
            
            if (paper.getAbstractText() != null && !paper.getAbstractText().trim().isEmpty()) {
                sb.append("摘要: ").append(paper.getShortAbstract()).append("\n");
            }
            
            if (paper.getPdfUrl() != null && !paper.getPdfUrl().trim().isEmpty()) {
                sb.append("PDF: ").append(paper.getPdfUrl()).append("\n");
            }
            
            if (paper.getDetailUrl() != null && !paper.getDetailUrl().trim().isEmpty()) {
                sb.append("详情: ").append(paper.getDetailUrl()).append("\n");
            }
            
            sb.append("\n");
        }
        
        // 添加搜索建议
        if (suggestions != null && !suggestions.isEmpty()) {
            sb.append("搜索建议:\n");
            for (String suggestion : suggestions) {
                sb.append("- ").append(suggestion).append("\n");
            }
            sb.append("\n");
        }
        
        // 添加相关搜索
        if (relatedQueries != null && !relatedQueries.isEmpty()) {
            sb.append("相关搜索:\n");
            for (String query : relatedQueries) {
                sb.append("- ").append(query).append("\n");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 获取简化的结果列表
     */
    public String getSimpleResults() {
        if (!isSuccessful()) {
            return "搜索失败: " + (errorMessage != null ? errorMessage : "未知错误");
        }
        
        if (isEmpty()) {
            return "未找到相关论文: " + query;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(returnedCount != null ? returnedCount : papers.size()).append(" 条结果:\n\n");
        
        for (int i = 0; i < papers.size(); i++) {
            GoogleScholarPaper paper = papers.get(i);
            sb.append("[").append(i + 1).append("] ").append(paper.getTitle()).append("\n");
            
            if (paper.getAuthors() != null && !paper.getAuthors().isEmpty()) {
                sb.append("    作者: ").append(paper.getAuthorsString()).append("\n");
            }
            
            if (paper.getYear() != null) {
                sb.append("    年份: ").append(paper.getYear());
            }
            
            if (paper.getCitations() != null && paper.getCitations() > 0) {
                sb.append(" | 引用: ").append(paper.getCitations());
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 获取JSON格式结果
     */
    public String getJsonResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"query\": \"").append(query != null ? query : "").append("\",\n");
        sb.append("  \"successful\": ").append(isSuccessful()).append(",\n");
        sb.append("  \"totalResults\": ").append(totalResults != null ? totalResults : 0).append(",\n");
        sb.append("  \"returnedCount\": ").append(returnedCount != null ? returnedCount : 0).append(",\n");
        sb.append("  \"searchDuration\": ").append(searchDuration != null ? searchDuration : 0).append(",\n");
        
        if (errorMessage != null) {
            sb.append("  \"errorMessage\": \"").append(errorMessage).append("\",\n");
        }
        
        sb.append("  \"papers\": [\n");
        
        if (papers != null && !papers.isEmpty()) {
            for (int i = 0; i < papers.size(); i++) {
                GoogleScholarPaper paper = papers.get(i);
                sb.append("    {\n");
                sb.append("      \"title\": \"").append(paper.getTitle() != null ? paper.getTitle() : "").append("\",\n");
                sb.append("      \"authors\": ").append(paper.getAuthors() != null ? paper.getAuthors().toString() : "[]").append(",\n");
                sb.append("      \"year\": ").append(paper.getYear() != null ? paper.getYear() : 0).append(",\n");
                sb.append("      \"venue\": \"").append(paper.getVenue() != null ? paper.getVenue() : "").append("\",\n");
                sb.append("      \"citations\": ").append(paper.getCitations() != null ? paper.getCitations() : 0).append(",\n");
                sb.append("      \"pdfUrl\": \"").append(paper.getPdfUrl() != null ? paper.getPdfUrl() : "").append("\",\n");
                sb.append("      \"detailUrl\": \"").append(paper.getDetailUrl() != null ? paper.getDetailUrl() : "").append("\"\n");
                
                if (i < papers.size() - 1) {
                    sb.append("    },\n");
                } else {
                    sb.append("    }\n");
                }
            }
        }
        
        sb.append("  ]\n");
        sb.append("}\n");
        
        return sb.toString();
    }
}
