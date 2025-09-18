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

package com.alibaba.langengine.gitee.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.gitee.sdk.GiteeClient;
import com.alibaba.langengine.gitee.sdk.GiteeException;
import com.alibaba.langengine.gitee.sdk.User;
import com.alibaba.langengine.gitee.sdk.SearchRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class GiteeUserSearchTool extends DefaultTool {

    private GiteeClient giteeClient;

    public GiteeUserSearchTool() {
        this.giteeClient = new GiteeClient();
        init();
    }

    public GiteeUserSearchTool(GiteeClient giteeClient) {
        this.giteeClient = giteeClient;
        init();
    }

    private void init() {
        setName("GiteeUserSearchTool");
        setDescription("Gitee user search tool for searching users on the Gitee platform. Input parameters: query(search keywords), sort(sort field), order(sort order), page(page number), perPage(results per page)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Search keywords for searching usernames, names, etc.\"\n" +
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
                "      \"description\": \"Results per page, default 20, maximum 100\",\n" +
                "      \"default\": 20,\n" +
                "      \"minimum\": 1,\n" +
                "      \"maximum\": 100\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"query\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("Gitee user search tool input: {}", toolInput);

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

            if (perPage == null || perPage <= 0 || perPage > 100) {
                perPage = 20;
            }

            SearchRequest request = SearchRequest.forUsers(query)
                    .withPagination(page, perPage);

            if (StringUtils.isNotBlank(sort)) {
                request.withSort(sort, StringUtils.isNotBlank(order) ? order : "desc");
            }

            List<User> users = giteeClient.searchUsers(request);

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
                    result.append("Avatar URL: ").append(user.getAvatarUrl()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getBio())) {
                    result.append("Bio: ").append(user.getBio()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getBlog())) {
                    result.append("Blog: ").append(user.getBlog()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getWeibo())) {
                    result.append("Weibo: ").append(user.getWeibo()).append("\n");
                }

                if (user.getFollowers() != null) {
                    result.append("Followers: ").append(user.getFollowers()).append("\n");
                }

                if (user.getFollowing() != null) {
                    result.append("Following: ").append(user.getFollowing()).append("\n");
                }

                if (user.getPublicRepos() != null) {
                    result.append("Public Repos: ").append(user.getPublicRepos()).append("\n");
                }

                if (user.getPublicGists() != null) {
                    result.append("Public Gists: ").append(user.getPublicGists()).append("\n");
                }

                if (user.getStared() != null) {
                    result.append("Starred Repos: ").append(user.getStared()).append("\n");
                }

                if (user.getWatched() != null) {
                    result.append("Watched Repos: ").append(user.getWatched()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getType())) {
                    result.append("Type: ").append(user.getType()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getMemberRole())) {
                    result.append("Member Role: ").append(user.getMemberRole()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getRemark())) {
                    result.append("Remark: ").append(user.getRemark()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getCreatedAt())) {
                    result.append("Created At: ").append(user.getCreatedAt()).append("\n");
                }

                if (StringUtils.isNotBlank(user.getUpdatedAt())) {
                    result.append("Updated At: ").append(user.getUpdatedAt()).append("\n");
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

        } catch (GiteeException e) {
            log.error("Gitee user search failed", e);
            String errorMsg = "Gitee user search failed: ";

            if (e.isAuthenticationError()) {
                errorMsg += "Authentication failed, please check GITEE_ACCESS_TOKEN environment variable or ali.langengine.community.gitee.access_token configuration in application.yml";
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
            log.error("Gitee user search tool execution failed", e);
            return new ToolExecuteResult("Search failed: " + e.getMessage());
        }
    }
}