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

package com.alibaba.langengine.gitcode.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.gitcode.sdk.GitCodeClient;
import com.alibaba.langengine.gitcode.sdk.GitCodeException;
import com.alibaba.langengine.gitcode.sdk.SearchRequest;
import com.alibaba.langengine.gitcode.sdk.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class GitCodeUserSearchTool extends DefaultTool {

    private GitCodeClient gitCodeClient;

    public GitCodeUserSearchTool() {
        this.gitCodeClient = new GitCodeClient();
        init();
    }

    public GitCodeUserSearchTool(GitCodeClient gitCodeClient) {
        this.gitCodeClient = gitCodeClient;
        init();
    }

    private void init() {
        setName("GitCodeUserSearchTool");
        setDescription("GitCode user search tool for searching users on the GitCode platform. Input parameters: query(search keywords), sort(sort field), order(sort order), page(page number), perPage(results per page)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Search keywords for searching username, display name, etc.\"\n" +
                "    },\n" +
                "    \"sort\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Sort field, options: joined_at(registration time), defaults to best match\",\n" +
                "      \"enum\": [\"joined_at\"]\n" +
                "    },\n" +
                "    \"order\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Sort order, options: asc(ascending) or desc(descending), defaults to desc\",\n" +
                "      \"enum\": [\"asc\", \"desc\"]\n" +
                "    },\n" +
                "    \"page\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"Page number, default 1, maximum 100\",\n" +
                "      \"default\": 1,\n" +
                "      \"minimum\": 1,\n" +
                "      \"maximum\": 100\n" +
                "    },\n" +
                "    \"perPage\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"Results per page, default 20, maximum 50\",\n" +
                "      \"default\": 20,\n" +
                "      \"minimum\": 1,\n" +
                "      \"maximum\": 50\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"query\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("GitCode user search tool input: {}", toolInput);

        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String query = (String) inputMap.get("query");
            String sort = (String) inputMap.get("sort");
            String order = (String) inputMap.get("order");
            Integer page = (Integer) inputMap.getOrDefault("page", 1);
            Integer perPage = (Integer) inputMap.getOrDefault("perPage", 20);

            if (StringUtils.isBlank(query)) {
                return new ToolExecuteResult("Error: Search keywords cannot be empty");
            }

            if (page == null || page <= 0 || page > 100) {
                page = 1;
            }

            if (perPage == null || perPage <= 0 || perPage > 50) {
                perPage = 20;
            }

            SearchRequest request = SearchRequest.forUsers(query)
                    .withPagination(page, perPage);

            if (StringUtils.isNotBlank(sort)) {
                request.withSort(sort, StringUtils.isNotBlank(order) ? order : "desc");
            }

            List<User> users = gitCodeClient.searchUsers(request);

            if (users == null || users.isEmpty()) {
                return new ToolExecuteResult("No related users found");
            }

            StringBuilder result = new StringBuilder();
            result.append("Found ").append(users.size()).append(" related users:\n\n");

            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                result.append("User ").append(i + 1).append(":\n");
                result.append("User ID: ").append(user.getId()).append("\n");
                result.append("Login: ").append(user.getLogin()).append("\n");

                if (StringUtils.isNotBlank(user.getName())) {
                    result.append("Name: ").append(user.getName()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getHtmlUrl())) {
                    result.append("Profile URL: ").append(user.getHtmlUrl()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getAvatarUrl())) {
                    result.append("Avatar: ").append(user.getAvatarUrl()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getCreatedAt())) {
                    result.append("Created At: ").append(user.getCreatedAt()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getType())) {
                    result.append("User Type: ").append(user.getType()).append("\n");
                }

                result.append("---\n");
            }

            result.append("\nSearch Parameters:\n");
            result.append("Keywords: ").append(query).append("\n");
            result.append("Page: ").append(page).append("\n");
            result.append("Per Page: ").append(perPage).append("\n");
            if (StringUtils.isNotBlank(sort)) {
                result.append("Sort: ").append(sort).append(" ").append(StringUtils.isNotBlank(order) ? order : "desc").append("\n");
            }

            return new ToolExecuteResult(result.toString());

        } catch (GitCodeException e) {
            log.error("GitCode user search failed", e);
            String errorMsg = "GitCode user search failed: ";

            if (e.isAuthenticationError()) {
                errorMsg += "Authentication failed, please check GITCODE_ACCESS_TOKEN environment variable or ali.langengine.community.gitcode.access_token configuration in application.yml";
            } else if (e.isRateLimitError()) {
                errorMsg += "Rate limit exceeded, please try again later";
            } else if (e.isForbiddenError()) {
                errorMsg += "Access forbidden, possibly insufficient permissions";
            } else if (e.isNotFoundError()) {
                errorMsg += "Resource not found";
            } else {
                errorMsg += e.getMessage();
            }

            return new ToolExecuteResult(errorMsg);
        } catch (Exception e) {
            log.error("GitCode user search tool execution failed", e);
            return new ToolExecuteResult("Search failed: " + e.getMessage());
        }
    }
}