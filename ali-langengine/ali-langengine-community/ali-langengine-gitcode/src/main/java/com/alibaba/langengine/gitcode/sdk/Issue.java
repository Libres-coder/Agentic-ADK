/**
 * Copyright (C) 2025 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.gitcode.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Issue {

    /**
     * Issue ID
     */
    private Integer id;

    /**
     * Issue HTML URL
     */
    @JsonProperty("html_url")
    private String htmlUrl;

    /**
     * Issue number
     */
    private String number;

    /**
     * Issue state
     */
    private String state;

    /**
     * Issue title
     */
    private String title;

    /**
     * Issue content
     */
    private String body;

    /**
     * Repository information
     */
    private Repository repository;

    /**
     * Creation time
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * Update time
     */
    @JsonProperty("updated_at")
    private String updatedAt;

    /**
     * Label list
     */
    private List<Object> labels;

    /**
     * Priority
     */
    private Integer priority;

    /**
     * Comment count
     */
    private Integer comments;

    /**
     * Parent ID
     */
    @JsonProperty("parent_id")
    private Integer parentId;
}