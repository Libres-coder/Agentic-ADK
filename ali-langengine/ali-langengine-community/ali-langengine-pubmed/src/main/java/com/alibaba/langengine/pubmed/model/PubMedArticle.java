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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * PubMed文章信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "PubmedArticle")
public class PubMedArticle {

    /**
     * PubMed ID
     */
    @JsonProperty("pmid")
    private String pmid;

    /**
     * 标题
     */
    @JsonProperty("title")
    private String title;

    /**
     * 摘要
     */
    @JsonProperty("abstract")
    private String abstractText;

    /**
     * 作者列表
     */
    @JsonProperty("authors")
    @JacksonXmlElementWrapper(localName = "AuthorList")
    @JacksonXmlProperty(localName = "Author")
    @Builder.Default
    private List<Author> authors = new ArrayList<>();

    /**
     * 期刊名称
     */
    @JsonProperty("journal")
    private String journal;

    /**
     * 发表日期
     */
    @JsonProperty("publishDate")
    private String publishDate;

    /**
     * DOI
     */
    @JsonProperty("doi")
    private String doi;

    /**
     * 关键词列表
     */
    @JsonProperty("keywords")
    @JacksonXmlElementWrapper(localName = "KeywordList")
    @JacksonXmlProperty(localName = "Keyword")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    /**
     * MeSH术语列表
     */
    @JsonProperty("meshTerms")
    @JacksonXmlElementWrapper(localName = "MeshHeadingList")
    @JacksonXmlProperty(localName = "MeshHeading")
    @Builder.Default
    private List<String> meshTerms = new ArrayList<>();

    /**
     * 文章类型
     */
    @JsonProperty("articleType")
    private String articleType;

    /**
     * 语言
     */
    @JsonProperty("language")
    private String language;

    /**
     * 国家/地区
     */
    @JsonProperty("country")
    private String country;

    /**
     * PubMed URL
     */
    @JsonProperty("url")
    private String url;

    /**
     * 作者信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        /**
         * 姓
         */
        @JsonProperty("lastName")
        private String lastName;

        /**
         * 名
         */
        @JsonProperty("firstName")
        private String firstName;

        /**
         * 缩写名
         */
        @JsonProperty("initials")
        private String initials;

        /**
         * 机构信息
         */
        @JsonProperty("affiliation")
        private String affiliation;

        /**
         * 获取完整姓名
         *
         * @return 完整姓名
         */
        public String getFullName() {
            if (lastName != null && firstName != null) {
                return firstName + " " + lastName;
            } else if (lastName != null && initials != null) {
                return initials + " " + lastName;
            } else if (lastName != null) {
                return lastName;
            } else if (firstName != null) {
                return firstName;
            } else {
                return "Unknown Author";
            }
        }
    }

    /**
     * 获取格式化的发表日期
     *
     * @return LocalDate对象，解析失败返回null
     */
    public LocalDate getParsedPublishDate() {
        if (publishDate == null || publishDate.trim().isEmpty()) {
            return null;
        }

        // 尝试不同的日期格式
        String[] dateFormats = {
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "yyyy-MM",
            "yyyy/MM",
            "yyyy"
        };

        for (String format : dateFormats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                if (format.equals("yyyy-MM") || format.equals("yyyy/MM")) {
                    // 对于年-月格式，添加日期为1号
                    return LocalDate.parse(publishDate + "-01", DateTimeFormatter.ofPattern(format + "-dd"));
                } else if (format.equals("yyyy")) {
                    // 对于年份格式，添加月份和日期为1月1号
                    return LocalDate.parse(publishDate + "-01-01", DateTimeFormatter.ofPattern(format + "-MM-dd"));
                } else {
                    return LocalDate.parse(publishDate, formatter);
                }
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }

        return null;
    }

    /**
     * 获取作者名称列表
     *
     * @return 作者名称字符串列表
     */
    public List<String> getAuthorNames() {
        List<String> names = new ArrayList<>();
        if (authors != null) {
            for (Author author : authors) {
                names.add(author.getFullName());
            }
        }
        return names;
    }

    /**
     * 获取PubMed URL
     *
     * @return PubMed文章URL
     */
    public String getPubMedUrl() {
        if (url != null && !url.trim().isEmpty()) {
            return url;
        }
        if (pmid != null && !pmid.trim().isEmpty()) {
            return "https://pubmed.ncbi.nlm.nih.gov/" + pmid + "/";
        }
        return null;
    }

    /**
     * 检查文章是否包含完整信息
     *
     * @return true如果包含基本的标题和摘要信息
     */
    public boolean hasCompleteInformation() {
        return title != null && !title.trim().isEmpty() &&
               abstractText != null && !abstractText.trim().isEmpty() &&
               pmid != null && !pmid.trim().isEmpty();
    }

    /**
     * 获取格式化的引用信息
     *
     * @return 格式化的引用字符串
     */
    public String getFormattedCitation() {
        StringBuilder citation = new StringBuilder();
        
        // 添加作者
        List<String> authorNames = getAuthorNames();
        if (!authorNames.isEmpty()) {
            if (authorNames.size() == 1) {
                citation.append(authorNames.get(0));
            } else if (authorNames.size() <= 3) {
                citation.append(String.join(", ", authorNames));
            } else {
                citation.append(authorNames.get(0)).append(" et al.");
            }
            citation.append(". ");
        }

        // 添加标题
        if (title != null && !title.trim().isEmpty()) {
            citation.append(title);
            if (!title.endsWith(".")) {
                citation.append(".");
            }
            citation.append(" ");
        }

        // 添加期刊
        if (journal != null && !journal.trim().isEmpty()) {
            citation.append(journal).append(". ");
        }

        // 添加日期
        if (publishDate != null && !publishDate.trim().isEmpty()) {
            citation.append(publishDate).append(". ");
        }

        // 添加PMID
        if (pmid != null && !pmid.trim().isEmpty()) {
            citation.append("PMID: ").append(pmid).append(".");
        }

        return citation.toString().trim();
    }

    /**
     * 获取搜索用的文本内容
     *
     * @return 包含标题、摘要、关键词等的搜索文本
     */
    public String getSearchableText() {
        StringBuilder text = new StringBuilder();
        
        if (title != null && !title.trim().isEmpty()) {
            text.append(title).append(" ");
        }
        
        if (abstractText != null && !abstractText.trim().isEmpty()) {
            text.append(abstractText).append(" ");
        }
        
        if (keywords != null && !keywords.isEmpty()) {
            text.append(String.join(" ", keywords)).append(" ");
        }
        
        if (meshTerms != null && !meshTerms.isEmpty()) {
            text.append(String.join(" ", meshTerms)).append(" ");
        }
        
        List<String> authorNames = getAuthorNames();
        if (!authorNames.isEmpty()) {
            text.append(String.join(" ", authorNames)).append(" ");
        }
        
        if (journal != null && !journal.trim().isEmpty()) {
            text.append(journal).append(" ");
        }
        
        return text.toString().trim();
    }
}
