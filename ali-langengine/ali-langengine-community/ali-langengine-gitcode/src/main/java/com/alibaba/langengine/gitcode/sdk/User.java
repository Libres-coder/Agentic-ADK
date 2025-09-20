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

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    /**
     * User avatar URL
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;

    /**
     * Creation time
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * User homepage URL
     */
    @JsonProperty("html_url")
    private String htmlUrl;

    /**
     * User ID
     */
    private String id;

    /**
     * User login name
     */
    private String login;

    /**
     * User name
     */
    private String name;

    /**
     * User type
     */
    private String type;
}