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
package com.alibaba.langengine.pubmed.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "eSearchResult")
public class PubMedSearchResponse {

    /**
     * 查询字符串
     */
    @JsonProperty("QueryTranslation")
    private String queryTranslation;

    /**
     * 总结果数量
     */
    @JsonProperty("Count")
    private int count;

    /**
     * 返回结果数量
     */
    @JsonProperty("RetMax")
    private int retMax;

    /**
     * 起始位置
     */
    @JsonProperty("RetStart")
    private int retStart;

    /**
     * PubMed ID列表
     */
    @JsonProperty("IdList")
    @JacksonXmlElementWrapper(localName = "IdList")
    @JacksonXmlProperty(localName = "Id")
    @Builder.Default
    private List<String> idList = new ArrayList<>();

    /**
     * 翻译查询键
     */
    @JsonProperty("QueryKey")
    private String queryKey;

    /**
     * Web环境
     */
    @JsonProperty("WebEnv")
    private String webEnv;

    /**
     * 错误信息列表
     */
    @JsonProperty("ErrorList")
    @JacksonXmlElementWrapper(localName = "ErrorList")
    @JacksonXmlProperty(localName = "PhraseNotFound")
    @Builder.Default
    private List<String> errorList = new ArrayList<>();

    /**
     * 警告信息列表
     */
    @JsonProperty("WarningList")
    @JacksonXmlElementWrapper(localName = "WarningList")
    @JacksonXmlProperty(localName = "PhraseIgnored")
    @Builder.Default
    private List<String> warningList = new ArrayList<>();

    /**
     * 文章详细信息列表（通过EFetch获取）
     */
    @JsonProperty("articles")
    @Builder.Default
    private List<PubMedArticle> articles = new ArrayList<>();

    /**
     * 检查搜索是否成功
     *
     * @return true如果搜索成功
     */
    public boolean isSuccessful() {
        return errorList == null || errorList.isEmpty();
    }

    /**
     * 检查是否有结果
     *
     * @return true如果有搜索结果
     */
    public boolean hasResults() {
        return count > 0 && idList != null && !idList.isEmpty();
    }

    /**
     * 获取错误信息字符串
     *
     * @return 格式化的错误信息
     */
    public String getErrorMessage() {
        if (errorList == null || errorList.isEmpty()) {
            return null;
        }
        return String.join("; ", errorList);
    }

    /**
     * 获取警告信息字符串
     *
     * @return 格式化的警告信息
     */
    public String getWarningMessage() {
        if (warningList == null || warningList.isEmpty()) {
            return null;
        }
        return String.join("; ", warningList);
    }

    /**
     * 检查是否有更多结果
     *
     * @return true如果还有更多结果可以获取
     */
    public boolean hasMoreResults() {
        return (retStart + retMax) < count;
    }

    /**
     * 获取下一页的起始位置
     *
     * @return 下一页的起始位置
     */
    public int getNextPageStart() {
        return retStart + retMax;
    }

    /**
     * 获取当前页码（从1开始）
     *
     * @return 当前页码
     */
    public int getCurrentPage() {
        if (retMax == 0) {
            return 1;
        }
        return (retStart / retMax) + 1;
    }

    /**
     * 获取总页数
     *
     * @return 总页数
     */
    public int getTotalPages() {
        if (retMax == 0 || count == 0) {
            return 1;
        }
        return (int) Math.ceil((double) count / retMax);
    }

    /**
     * 获取分页信息摘要
     *
     * @return 分页信息字符串
     */
    public String getPaginationSummary() {
        return String.format("Page %d of %d (showing %d-%d of %d total results)",
                getCurrentPage(), getTotalPages(),
                retStart + 1, Math.min(retStart + retMax, count), count);
    }

    /**
     * 创建空的搜索响应
     *
     * @return 空的搜索响应对象
     */
    public static PubMedSearchResponse empty() {
        return PubMedSearchResponse.builder()
                .count(0)
                .retMax(0)
                .retStart(0)
                .idList(new ArrayList<>())
                .articles(new ArrayList<>())
                .build();
    }

    /**
     * 创建错误响应
     *
     * @param errorMessage 错误信息
     * @return 错误响应对象
     */
    public static PubMedSearchResponse error(String errorMessage) {
        List<String> errors = new ArrayList<>();
        errors.add(errorMessage);
        return PubMedSearchResponse.builder()
                .count(0)
                .retMax(0)
                .retStart(0)
                .idList(new ArrayList<>())
                .articles(new ArrayList<>())
                .errorList(errors)
                .build();
    }

    /**
     * 从ID列表创建搜索响应
     *
     * @param ids PubMed ID列表
     * @param totalCount 总数量
     * @param retStart 起始位置
     * @return 搜索响应对象
     */
    public static PubMedSearchResponse fromIds(List<String> ids, int totalCount, int retStart) {
        return PubMedSearchResponse.builder()
                .count(totalCount)
                .retMax(ids.size())
                .retStart(retStart)
                .idList(new ArrayList<>(ids))
                .articles(new ArrayList<>())
                .build();
    }

    /**
     * 添加文章到响应中
     *
     * @param article 文章对象
     */
    public void addArticle(PubMedArticle article) {
        if (articles == null) {
            articles = new ArrayList<>();
        }
        articles.add(article);
    }

    /**
     * 批量添加文章到响应中
     *
     * @param articleList 文章列表
     */
    public void addArticles(List<PubMedArticle> articleList) {
        if (articles == null) {
            articles = new ArrayList<>();
        }
        if (articleList != null) {
            articles.addAll(articleList);
        }
    }

    /**
     * 获取响应摘要信息
     *
     * @return 响应摘要字符串
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("PubMed Search Response: ");
        
        if (!isSuccessful()) {
            summary.append("FAILED - ").append(getErrorMessage());
        } else if (!hasResults()) {
            summary.append("No results found");
        } else {
            summary.append(getPaginationSummary());
            if (articles != null && !articles.isEmpty()) {
                summary.append(" (").append(articles.size()).append(" articles loaded)");
            }
        }
        
        if (warningList != null && !warningList.isEmpty()) {
            summary.append(" [Warnings: ").append(getWarningMessage()).append("]");
        }
        
        return summary.toString();
    }
}
