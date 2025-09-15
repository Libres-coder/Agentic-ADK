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
import com.alibaba.langengine.gitcode.sdk.Issue;
import com.alibaba.langengine.gitcode.sdk.SearchRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class GitCodeIssueSearchTool extends DefaultTool {

    private GitCodeClient gitCodeClient;

    public GitCodeIssueSearchTool() {
        this.gitCodeClient = new GitCodeClient();
        init();
    }

    public GitCodeIssueSearchTool(GitCodeClient gitCodeClient) {
        this.gitCodeClient = gitCodeClient;
        init();
    }

    private void init() {
        setName("GitCodeIssueSearchTool");
        setDescription("GitCode Issues search tool for searching issues and problems on the GitCode platform. Input parameters: query(search keywords), sort(sort field), order(sort order), repo(repository path), state(status), page(page number), perPage(results per page)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Search keywords for searching issue titles, content, etc.\"\n" +
                "    },\n" +
                "    \"sort\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Sort field, options: created_at(creation time), last_push_at(update time), defaults to best match\",\n" +
                "      \"enum\": [\"created_at\", \"last_push_at\"]\n" +
                "    },\n" +
                "    \"order\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Sort order, options: asc(ascending) or desc(descending), defaults to desc\",\n" +
                "      \"enum\": [\"asc\", \"desc\"]\n" +
                "    },\n" +
                "    \"repo\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Repository path, format: owner/repository, e.g.: alibaba/spring-boot\"\n" +
                "    },\n" +
                "    \"state\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Filter issues by specified status, options: open(open), closed(closed)\",\n" +
                "      \"enum\": [\"open\", \"closed\"]\n" +
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
        log.info("GitCode Issues search tool input: {}", toolInput);

        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String query = (String) inputMap.get("query");
            String sort = (String) inputMap.get("sort");
            String order = (String) inputMap.get("order");
            String repo = (String) inputMap.get("repo");
            String state = (String) inputMap.get("state");
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

            SearchRequest request = SearchRequest.forIssues(query)
                    .withPagination(page, perPage);

            if (StringUtils.isNotBlank(sort)) {
                request.withSort(sort, StringUtils.isNotBlank(order) ? order : "desc");
            }

            if (StringUtils.isNotBlank(repo)) {
                request.withRepo(repo);
            }

            if (StringUtils.isNotBlank(state)) {
                request.withState(state);
            }

            List<Issue> issues = gitCodeClient.searchIssues(request);

            if (issues == null || issues.isEmpty()) {
                return new ToolExecuteResult("No related issues found");
            }

            StringBuilder result = new StringBuilder();
            result.append("Found ").append(issues.size()).append(" related issues:\n\n");

            for (int i = 0; i < issues.size(); i++) {
                Issue issue = issues.get(i);
                result.append("Issue ").append(i + 1).append(":\n");
                result.append("Issue ID: ").append(issue.getId()).append("\n");
                result.append("Number: ").append(issue.getNumber()).append("\n");
                result.append("Title: ").append(issue.getTitle()).append("\n");
                result.append("Status: ").append(issue.getState()).append("\n");

                if (StringUtils.isNotBlank(issue.getBody())) {
                    String body = issue.getBody();
                    if (body.length() > 200) {
                        body = body.substring(0, 200) + "...";
                    }
                    result.append("Content: ").append(body).append("\n");
                }

                if (StringUtils.isNotBlank(issue.getHtmlUrl())) {
                    result.append("Issue URL: ").append(issue.getHtmlUrl()).append("\n");
                }

                if (issue.getRepository() != null) {
                    result.append("Repository: ").append(issue.getRepository().getFullName()).append("\n");
                    if (StringUtils.isNotBlank(issue.getRepository().getUrl())) {
                        result.append("Repository URL: ").append(issue.getRepository().getUrl()).append("\n");
                    }
                }

                if (StringUtils.isNotBlank(issue.getCreatedAt())) {
                    result.append("Created At: ").append(issue.getCreatedAt()).append("\n");
                }

                if (StringUtils.isNotBlank(issue.getUpdatedAt())) {
                    result.append("Updated At: ").append(issue.getUpdatedAt()).append("\n");
                }

                if (issue.getComments() != null) {
                    result.append("Comments: ").append(issue.getComments()).append("\n");
                }

                if (issue.getPriority() != null) {
                    result.append("Priority: ").append(issue.getPriority()).append("\n");
                }

                if (issue.getLabels() != null && !issue.getLabels().isEmpty()) {
                    result.append("Labels Count: ").append(issue.getLabels().size()).append("\n");
                }

                if (issue.getParentId() != null && issue.getParentId() > 0) {
                    result.append("Parent ID: ").append(issue.getParentId()).append("\n");
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
            if (StringUtils.isNotBlank(repo)) {
                result.append("Repository: ").append(repo).append("\n");
            }
            if (StringUtils.isNotBlank(state)) {
                result.append("State: ").append(state).append("\n");
            }

            return new ToolExecuteResult(result.toString());

        } catch (GitCodeException e) {
            log.error("GitCode Issues search failed", e);
            String errorMsg = "GitCode Issues search failed: ";

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
            log.error("GitCode Issues search tool execution failed", e);
            return new ToolExecuteResult("Search failed: " + e.getMessage());
        }
    }
}