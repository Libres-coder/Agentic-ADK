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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PubMedSearchRequest {

    /**
     * 默认返回结果数量
     */
    public static final int DEFAULT_LIMIT = 20;

    /**
     * 最大返回结果数量
     */
    public static final int MAX_LIMIT = 200;

    /**
     * 搜索查询字符串
     */
    private String query;

    /**
     * 返回结果数量限制
     */
    @Builder.Default
    private int limit = DEFAULT_LIMIT;

    /**
     * 结果起始位置（用于分页）
     */
    @Builder.Default
    private int offset = 0;

    /**
     * 搜索字段类型
     */
    private String field;

    /**
     * 开始日期（格式：YYYY/MM/DD）
     */
    private String startDate;

    /**
     * 结束日期（格式：YYYY/MM/DD）
     */
    private String endDate;

    /**
     * 文章类型过滤
     */
    private String articleType;

    /**
     * 语言过滤
     */
    private String language;

    /**
     * 期刊名称过滤
     */
    private String journal;

    /**
     * 作者过滤
     */
    private String author;

    /**
     * 排序方式
     */
    @Builder.Default
    private String sort = "relevance";

    /**
     * 数据库名称
     */
    @Builder.Default
    private String database = "pubmed";

    /**
     * 验证请求参数
     *
     * @return 验证结果
     */
    public boolean isValid() {
        if (StringUtils.isBlank(query)) {
            return false;
        }

        if (limit <= 0 || limit > MAX_LIMIT) {
            return false;
        }

        if (offset < 0) {
            return false;
        }

        // 验证日期格式
        if (startDate != null && !isValidDateFormat(startDate)) {
            return false;
        }

        if (endDate != null && !isValidDateFormat(endDate)) {
            return false;
        }

        return true;
    }

    /**
     * 验证日期格式
     *
     * @param date 日期字符串
     * @return 验证结果
     */
    private boolean isValidDateFormat(String date) {
        if (StringUtils.isBlank(date)) {
            return true;
        }

        try {
            // 支持多种日期格式
            String[] formats = {"yyyy/MM/dd", "yyyy-MM-dd", "yyyy/MM", "yyyy-MM", "yyyy"};
            for (String format : formats) {
                try {
                    DateTimeFormatter.ofPattern(format).parse(date);
                    return true;
                } catch (Exception e) {
                    // 继续尝试下一个格式
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 构建完整的搜索查询字符串
     *
     * @return 完整的查询字符串
     */
    public String buildFullQuery() {
        StringBuilder fullQuery = new StringBuilder();
        
        // 基础查询
        if (StringUtils.isNotBlank(query)) {
            if (StringUtils.isNotBlank(field)) {
                fullQuery.append("(").append(query).append(")[").append(field).append("]");
            } else {
                fullQuery.append(query);
            }
        }

        // 添加作者过滤
        if (StringUtils.isNotBlank(author)) {
            if (fullQuery.length() > 0) {
                fullQuery.append(" AND ");
            }
            fullQuery.append("(").append(author).append(")[Author]");
        }

        // 添加期刊过滤
        if (StringUtils.isNotBlank(journal)) {
            if (fullQuery.length() > 0) {
                fullQuery.append(" AND ");
            }
            fullQuery.append("(").append(journal).append(")[Journal]");
        }

        // 添加文章类型过滤
        if (StringUtils.isNotBlank(articleType)) {
            if (fullQuery.length() > 0) {
                fullQuery.append(" AND ");
            }
            fullQuery.append("(").append(articleType).append(")[Publication Type]");
        }

        // 添加语言过滤
        if (StringUtils.isNotBlank(language)) {
            if (fullQuery.length() > 0) {
                fullQuery.append(" AND ");
            }
            fullQuery.append("(").append(language).append(")[Language]");
        }

        // 添加日期范围过滤
        if (StringUtils.isNotBlank(startDate) || StringUtils.isNotBlank(endDate)) {
            if (fullQuery.length() > 0) {
                fullQuery.append(" AND ");
            }
            
            String start = StringUtils.isNotBlank(startDate) ? startDate : "1900/01/01";
            String end = StringUtils.isNotBlank(endDate) ? endDate : "3000/12/31";
            fullQuery.append("(\"").append(start).append("\"[Date - Publication] : \"").append(end).append("\"[Date - Publication])");
        }

        return fullQuery.toString();
    }

    /**
     * 获取用于ESearch API的参数
     *
     * @return 参数字符串
     */
    public String getESearchParams() {
        StringBuilder params = new StringBuilder();
        params.append("db=").append(database);
        params.append("&term=").append(java.net.URLEncoder.encode(buildFullQuery(), java.nio.charset.StandardCharsets.UTF_8));
        params.append("&retstart=").append(offset);
        params.append("&retmax=").append(limit);
        params.append("&retmode=xml");
        
        if (StringUtils.isNotBlank(sort)) {
            params.append("&sort=").append(sort);
        }
        
        return params.toString();
    }

    /**
     * 创建简单的搜索请求
     *
     * @param query 查询字符串
     * @return 搜索请求对象
     */
    public static PubMedSearchRequest simple(String query) {
        return PubMedSearchRequest.builder()
                .query(query)
                .build();
    }

    /**
     * 创建带限制的搜索请求
     *
     * @param query 查询字符串
     * @param limit 结果数量限制
     * @return 搜索请求对象
     */
    public static PubMedSearchRequest withLimit(String query, int limit) {
        return PubMedSearchRequest.builder()
                .query(query)
                .limit(Math.min(limit, MAX_LIMIT))
                .build();
    }

    /**
     * 创建日期范围搜索请求
     *
     * @param query 查询字符串
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 搜索请求对象
     */
    public static PubMedSearchRequest withDateRange(String query, LocalDate startDate, LocalDate endDate) {
        PubMedSearchRequestBuilder builder = PubMedSearchRequest.builder().query(query);
        
        if (startDate != null) {
            builder.startDate(startDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        }
        
        if (endDate != null) {
            builder.endDate(endDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        }
        
        return builder.build();
    }

    /**
     * 创建作者搜索请求
     *
     * @param query 查询字符串
     * @param author 作者名称
     * @return 搜索请求对象
     */
    public static PubMedSearchRequest withAuthor(String query, String author) {
        return PubMedSearchRequest.builder()
                .query(query)
                .author(author)
                .build();
    }

    /**
     * 创建期刊搜索请求
     *
     * @param query 查询字符串
     * @param journal 期刊名称
     * @return 搜索请求对象
     */
    public static PubMedSearchRequest withJournal(String query, String journal) {
        return PubMedSearchRequest.builder()
                .query(query)
                .journal(journal)
                .build();
    }
}
