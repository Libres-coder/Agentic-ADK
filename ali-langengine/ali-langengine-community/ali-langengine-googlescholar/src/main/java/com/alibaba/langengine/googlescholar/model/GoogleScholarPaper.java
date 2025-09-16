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

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * Google Scholar 论文模型
 * 表示从 Google Scholar 搜索结果中解析出的论文信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleScholarPaper {
    
    /**
     * 论文标题
     */
    private String title;
    
    /**
     * 作者列表
     */
    private List<String> authors;
    
    /**
     * 发表年份
     */
    private Integer year;
    
    /**
     * 发表期刊/会议
     */
    private String venue;
    
    /**
     * 出版商
     */
    private String publisher;
    
    /**
     * 摘要
     */
    private String abstractText;
    
    /**
     * 引用次数
     */
    private Integer citations;
    
    /**
     * 相关文章数量
     */
    private Integer relatedArticles;
    
    /**
     * 版本数量
     */
    private Integer versions;
    
    /**
     * PDF 链接
     */
    private String pdfUrl;
    
    /**
     * 论文详情页面链接
     */
    private String detailUrl;
    
    /**
     * 引用链接
     */
    private String citationsUrl;
    
    /**
     * 相关文章链接
     */
    private String relatedUrl;
    
    /**
     * 版本链接
     */
    private String versionsUrl;
    
    /**
     * DOI
     */
    private String doi;
    
    /**
     * ISBN
     */
    private String isbn;
    
    /**
     * 论文类型（文章、会议论文、书籍等）
     */
    private String type;
    
    /**
     * 语言
     */
    private String language;
    
    /**
     * 关键词
     */
    private List<String> keywords;
    
    /**
     * 学科分类
     */
    private List<String> categories;
    
    /**
     * 是否开放获取
     */
    private Boolean openAccess;
    
    /**
     * 是否同行评议
     */
    private Boolean peerReviewed;
    
    /**
     * 影响因子
     */
    private Double impactFactor;
    
    /**
     * 期刊排名
     */
    private String journalRanking;
    
    /**
     * 创建时间
     */
    private LocalDate createdDate;
    
    /**
     * 最后更新时间
     */
    private LocalDate lastUpdated;
    
    /**
     * 默认构造函数
     */
    public GoogleScholarPaper(String title) {
        this.title = title;
        this.authors = new ArrayList<>();
        this.keywords = new ArrayList<>();
        this.categories = new ArrayList<>();
    }
    
    /**
     * 添加作者
     */
    public void addAuthor(String author) {
        if (authors == null) {
            authors = new ArrayList<>();
        }
        if (author != null && !author.trim().isEmpty()) {
            authors.add(author.trim());
        }
    }
    
    /**
     * 添加关键词
     */
    public void addKeyword(String keyword) {
        if (keywords == null) {
            keywords = new ArrayList<>();
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            keywords.add(keyword.trim());
        }
    }
    
    /**
     * 添加学科分类
     */
    public void addCategory(String category) {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        if (category != null && !category.trim().isEmpty()) {
            categories.add(category.trim());
        }
    }
    
    /**
     * 获取作者字符串（格式化显示）
     */
    public String getAuthorsString() {
        if (authors == null || authors.isEmpty()) {
            return "未知作者";
        }
        
        if (authors.size() <= 3) {
            return String.join(", ", authors);
        } else {
            return String.join(", ", authors.subList(0, 3)) + " et al.";
        }
    }
    
    /**
     * 获取关键词字符串
     */
    public String getKeywordsString() {
        if (keywords == null || keywords.isEmpty()) {
            return "";
        }
        return String.join(", ", keywords);
    }
    
    /**
     * 获取学科分类字符串
     */
    public String getCategoriesString() {
        if (categories == null || categories.isEmpty()) {
            return "";
        }
        return String.join(", ", categories);
    }
    
    /**
     * 获取短摘要（截取前200字符）
     */
    public String getShortAbstract() {
        if (abstractText == null || abstractText.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = abstractText.trim();
        if (trimmed.length() <= 200) {
            return trimmed;
        }
        
        return trimmed.substring(0, 200) + "...";
    }
    
    /**
     * 检查是否有引用信息
     */
    public boolean hasCitations() {
        return citations != null && citations > 0;
    }
    
    /**
     * 检查是否有PDF链接
     */
    public boolean hasPdfUrl() {
        return pdfUrl != null && !pdfUrl.trim().isEmpty();
    }
    
    /**
     * 检查是否有DOI
     */
    public boolean hasDoi() {
        return doi != null && !doi.trim().isEmpty();
    }
    
    /**
     * 检查是否开放获取
     */
    public boolean isOpenAccess() {
        return openAccess != null && openAccess;
    }
    
    /**
     * 检查是否同行评议
     */
    public boolean isPeerReviewed() {
        return peerReviewed != null && peerReviewed;
    }
    
    /**
     * 验证论文信息是否有效
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty();
    }
    
    /**
     * 获取论文的完整信息字符串
     */
    public String getFullInfo() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("标题: ").append(title).append("\n");
        
        if (authors != null && !authors.isEmpty()) {
            sb.append("作者: ").append(getAuthorsString()).append("\n");
        }
        
        if (year != null) {
            sb.append("年份: ").append(year).append("\n");
        }
        
        if (venue != null && !venue.trim().isEmpty()) {
            sb.append("期刊/会议: ").append(venue).append("\n");
        }
        
        if (publisher != null && !publisher.trim().isEmpty()) {
            sb.append("出版商: ").append(publisher).append("\n");
        }
        
        if (citations != null && citations > 0) {
            sb.append("引用次数: ").append(citations).append("\n");
        }
        
        if (abstractText != null && !abstractText.trim().isEmpty()) {
            sb.append("摘要: ").append(getShortAbstract()).append("\n");
        }
        
        if (doi != null && !doi.trim().isEmpty()) {
            sb.append("DOI: ").append(doi).append("\n");
        }
        
        if (pdfUrl != null && !pdfUrl.trim().isEmpty()) {
            sb.append("PDF: ").append(pdfUrl).append("\n");
        }
        
        if (detailUrl != null && !detailUrl.trim().isEmpty()) {
            sb.append("详情: ").append(detailUrl).append("\n");
        }
        
        return sb.toString();
    }
}
