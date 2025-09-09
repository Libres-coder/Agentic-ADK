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
package com.alibaba.langengine.google.sdk;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Google搜索结果类
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SearchResult {
    
    /**
     * 搜索结果标题
     */
    private String title;
    
    /**
     * 搜索结果URL
     */
    private String url;
    
    /**
     * 搜索结果描述/摘要
     */
    private String description;
    
    /**
     * 搜索结果类型
     */
    private String type;
    
    /**
     * 搜索结果来源域名
     */
    private String domain;
    
    /**
     * 搜索结果发布时间
     */
    private LocalDateTime publishDate;
    
    /**
     * 搜索结果缩略图URL（图片搜索）
     */
    private String thumbnailUrl;
    
    /**
     * 搜索结果文件大小（文件搜索）
     */
    private String fileSize;
    
    /**
     * 搜索结果文件类型（文件搜索）
     */
    private String fileType;
    
    /**
     * 搜索结果评分/星级
     */
    private Double rating;
    
    /**
     * 搜索结果评论数量
     */
    private Integer reviewCount;
    
    /**
     * 搜索结果价格（购物搜索）
     */
    private String price;
    
    /**
     * 搜索结果货币（购物搜索）
     */
    private String currency;
    
    /**
     * 搜索结果位置信息（本地搜索）
     */
    private String location;
    
    /**
     * 搜索结果电话号码（本地搜索）
     */
    private String phoneNumber;
    
    /**
     * 搜索结果营业时间（本地搜索）
     */
    private String businessHours;
    
    /**
     * 搜索结果标签
     */
    private List<String> tags;
    
    /**
     * 搜索结果元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 搜索结果相关性得分
     */
    private Double relevanceScore;
    
    /**
     * 搜索结果是否被索引
     */
    private Boolean isIndexed;
    
    /**
     * 搜索结果是否被缓存
     */
    private Boolean isCached;
    
    /**
     * 获取有效的标题
     */
    public String getValidTitle() {
        return title != null ? title.trim() : "";
    }
    
    /**
     * 获取有效的URL
     */
    public String getValidUrl() {
        return url != null ? url.trim() : "";
    }
    
    /**
     * 获取有效的描述
     */
    public String getValidDescription() {
        return description != null ? description.trim() : "";
    }
    
    /**
     * 获取有效的域名
     */
    public String getValidDomain() {
        if (domain != null && !domain.trim().isEmpty()) {
            return domain;
        }
        if (url != null && !url.trim().isEmpty()) {
            try {
                java.net.URL urlObj = new java.net.URL(url);
                return urlObj.getHost();
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }
    
    /**
     * 检查是否为有效结果
     */
    public boolean isValid() {
        return getValidTitle().length() > 0 && getValidUrl().length() > 0;
    }
} 