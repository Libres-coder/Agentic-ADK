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
import com.alibaba.langengine.gitee.sdk.Issue;
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
public class GiteeIssueSearchTool extends DefaultTool {

    private GiteeClient giteeClient;

    public GiteeIssueSearchTool() {
        this.giteeClient = new GiteeClient();
        init();
    }

    public GiteeIssueSearchTool(GiteeClient giteeClient) {
        this.giteeClient = giteeClient;
        init();
    }

    private void init() {
        setName("GiteeIssueSearchTool");
        setDescription("Gitee Issues search tool for searching issues and problems on the Gitee platform. Input parameters: query(search keywords), sort(sort field), order(sort order), repo(repository path), state(status), language(language), label(label), author(author), assignee(assignee), page(page number), perPage(results per page)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Search keywords for searching issue titles, content, etc.\"\n" +
                "    },\n" +
                "    \"sort\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Sort field, options: created_at(creation time), last_push_at(update time), notes_count(comment count), defaults to best match\",\n" +
                "      \"enum\": [\"created_at\", \"last_push_at\", \"notes_count\"]\n" +
                "    },\n" +
                "    \"order\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Sort order, options: asc(ascending) or desc(descending), defaults to desc\",\n" +
                "      \"enum\": [\"asc\", \"desc\"]\n" +
                "    },\n" +
                "    \"repo\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Repository path, format: owner/repository, e.g.: oschina/git-osc\"\n" +
                "    },\n" +
                "    \"state\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Filter issues by specified status, options: open(open), closed(completed), rejected(rejected)\",\n" +
                "      \"enum\": [\"open\", \"closed\", \"rejected\"]\n" +
                "    },\n" +
                "    \"language\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Filter issues by specified language\"\n" +
                "    },\n" +
                "    \"label\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Filter issues by specified label\"\n" +
                "    },\n" +
                "    \"author\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Filter issues by specified creator (username/login)\"\n" +
                "    },\n" +
                "    \"assignee\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Filter issues by specified assignee (username/login)\"\n" +
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
        log.info("Gitee Issues search tool input: {}", toolInput);

        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String query = (String) inputMap.get("query");
            String sort = (String) inputMap.get("sort");
            String order = (String) inputMap.get("order");
            String repo = (String) inputMap.get("repo");
            String state = (String) inputMap.get("state");
            String language = (String) inputMap.get("language");
            String label = (String) inputMap.get("label");
            String author = (String) inputMap.get("author");
            String assignee = (String) inputMap.get("assignee");
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

            if (StringUtils.isNotBlank(language)) {
                request.withLanguage(language);
            }

            if (StringUtils.isNotBlank(label)) {
                request.withLabel(label);
            }

            if (StringUtils.isNotBlank(author)) {
                request.withAuthor(author);
            }

            if (StringUtils.isNotBlank(assignee)) {
                request.withAssignee(assignee);
            }

            List<Issue> issues = giteeClient.searchIssues(request);

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
                result.append("State: ").append(issue.getState()).append("\n");

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
                    if (StringUtils.isNotBlank(issue.getRepository().getHtmlUrl())) {
                        result.append("Repository URL: ").append(issue.getRepository().getHtmlUrl()).append("\n");
                    }
                }

                if (issue.getUser() != null) {
                    result.append("Creator: ").append(issue.getUser().getLogin()).append("\n");
                }

                if (issue.getAssignee() != null) {
                    result.append("Assignee: ").append(issue.getAssignee().getLogin()).append("\n");
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
                    String priorityText = "";
                    switch (issue.getPriority()) {
                        case 0: priorityText = "Not specified"; break;
                        case 1: priorityText = "Not important"; break;
                        case 2: priorityText = "Minor"; break;
                        case 3: priorityText = "Major"; break;
                        case 4: priorityText = "Critical"; break;
                        default: priorityText = String.valueOf(issue.getPriority()); break;
                    }
                    result.append("Priority: ").append(priorityText).append("\n");
                }

                if (issue.getLabels() != null && !issue.getLabels().isEmpty()) {
                    result.append("Labels: ");
                    for (int j = 0; j < issue.getLabels().size(); j++) {
                        if (j > 0) result.append(", ");
                        result.append(issue.getLabels().get(j).getName());
                    }
                    result.append("\n");
                }

                if (issue.getMilestone() != null && StringUtils.isNotBlank(issue.getMilestone().getTitle())) {
                    result.append("Milestone: ").append(issue.getMilestone().getTitle()).append("\n");
                }

                if (issue.getParentId() != null && issue.getParentId() > 0) {
                    result.append("Parent ID: ").append(issue.getParentId()).append("\n");
                }

                if (issue.getSecurityHole() != null && issue.getSecurityHole()) {
                    result.append("Security Issue: Yes\n");
                }

                if (StringUtils.isNotBlank(issue.getCveId())) {
                    result.append("CVE ID: ").append(issue.getCveId()).append("\n");
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
            if (StringUtils.isNotBlank(language)) {
                result.append("Language: ").append(language).append("\n");
            }
            if (StringUtils.isNotBlank(label)) {
                result.append("Label: ").append(label).append("\n");
            }
            if (StringUtils.isNotBlank(author)) {
                result.append("Author: ").append(author).append("\n");
            }
            if (StringUtils.isNotBlank(assignee)) {
                result.append("Assignee: ").append(assignee).append("\n");
            }

            return new ToolExecuteResult(result.toString());

        } catch (GiteeException e) {
            log.error("Gitee Issues search failed", e);
            String errorMsg = "Gitee Issues search failed: ";

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
            log.error("Gitee Issues search tool execution failed", e);
            return new ToolExecuteResult("Search failed: " + e.getMessage());
        }
    }
}