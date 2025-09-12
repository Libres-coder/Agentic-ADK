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
import com.alibaba.langengine.gitcode.sdk.Repository;
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
public class GitCodeRepositorySearchTool extends DefaultTool {

    private GitCodeClient gitCodeClient;

    public GitCodeRepositorySearchTool() {
        this.gitCodeClient = new GitCodeClient();
        init();
    }

    public GitCodeRepositorySearchTool(GitCodeClient gitCodeClient) {
        this.gitCodeClient = gitCodeClient;
        init();
    }

    private void init() {
        setName("GitCodeRepositorySearchTool");
        setDescription("GitCode repository search tool for searching code repositories on the GitCode platform. Input parameters: query(search keywords), sort(sort field), order(sort order), owner(owner), language(programming language), fork(include fork repositories), page(page number), perPage(results per page)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Search keywords for searching repository names, descriptions, etc.\"\n" +
                "    },\n" +
                "    \"sort\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Sort field, options: last_push_at(update time), stars_count(star count), forks_count(fork count), defaults to best match\",\n" +
                "      \"enum\": [\"last_push_at\", \"stars_count\", \"forks_count\"]\n" +
                "    },\n" +
                "    \"order\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Sort order, options: asc(ascending) or desc(descending), defaults to desc\",\n" +
                "      \"enum\": [\"asc\", \"desc\"]\n" +
                "    },\n" +
                "    \"owner\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Repository owner (organization or personal address path)\"\n" +
                "    },\n" +
                "    \"language\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Filter repositories by specified programming language, e.g.: java, python, javascript\"\n" +
                "    },\n" +
                "    \"fork\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Whether to search repositories including forks, options: true or false\",\n" +
                "      \"enum\": [\"true\", \"false\"]\n" +
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
        log.info("GitCode repository search tool input: {}", toolInput);

        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String query = (String) inputMap.get("query");
            String sort = (String) inputMap.get("sort");
            String order = (String) inputMap.get("order");
            String owner = (String) inputMap.get("owner");
            String language = (String) inputMap.get("language");
            String fork = (String) inputMap.get("fork");
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

            SearchRequest request = SearchRequest.forRepositories(query)
                    .withPagination(page, perPage);

            if (StringUtils.isNotBlank(sort)) {
                request.withSort(sort, StringUtils.isNotBlank(order) ? order : "desc");
            }

            if (StringUtils.isNotBlank(owner)) {
                request.withOwner(owner);
            }

            if (StringUtils.isNotBlank(language)) {
                request.withLanguage(language);
            }

            if (StringUtils.isNotBlank(fork)) {
                request.withFork(fork);
            }

            List<Repository> repositories = gitCodeClient.searchRepositories(request);

            if (repositories == null || repositories.isEmpty()) {
                return new ToolExecuteResult("No related repositories found");
            }

            StringBuilder result = new StringBuilder();
            result.append("Found ").append(repositories.size()).append(" related repositories:\n\n");

            for (int i = 0; i < repositories.size(); i++) {
                Repository repo = repositories.get(i);
                result.append("Repository ").append(i + 1).append(":\n");
                result.append("Repository ID: ").append(repo.getId()).append("\n");
                result.append("Repository Name: ").append(repo.getName()).append("\n");
                result.append("Full Name: ").append(repo.getFullName()).append("\n");

                if (StringUtils.isNotBlank(repo.getHumanName())) {
                    result.append("Display Name: ").append(repo.getHumanName()).append("\n");
                }

                if (StringUtils.isNotBlank(repo.getDescription())) {
                    result.append("Description: ").append(repo.getDescription()).append("\n");
                }

                if (StringUtils.isNotBlank(repo.getWebUrl())) {
                    result.append("Project URL: ").append(repo.getWebUrl()).append("\n");
                }

                if (StringUtils.isNotBlank(repo.getHttpUrlToRepo())) {
                    result.append("Clone URL: ").append(repo.getHttpUrlToRepo()).append("\n");
                }

                if (repo.getStargazersCount() != null) {
                    result.append("Stars: ").append(repo.getStargazersCount()).append("\n");
                }

                if (repo.getForksCount() != null) {
                    result.append("Forks: ").append(repo.getForksCount()).append("\n");
                }

                if (repo.getOpenIssuesCount() != null) {
                    result.append("Open Issues: ").append(repo.getOpenIssuesCount()).append("\n");
                }

                if (StringUtils.isNotBlank(repo.getDefaultBranch())) {
                    result.append("Default Branch: ").append(repo.getDefaultBranch()).append("\n");
                }

                if (StringUtils.isNotBlank(repo.getCreatedAt())) {
                    result.append("Created At: ").append(repo.getCreatedAt()).append("\n");
                }

                if (StringUtils.isNotBlank(repo.getUpdatedAt())) {
                    result.append("Updated At: ").append(repo.getUpdatedAt()).append("\n");
                }

                if (StringUtils.isNotBlank(repo.getPushedAt())) {
                    result.append("Last Push: ").append(repo.getPushedAt()).append("\n");
                }

                if (repo.getFork() != null) {
                    result.append("Is Fork: ").append(repo.getFork() ? "Yes" : "No").append("\n");
                }

                if (repo.getPrivateRepo() != null) {
                    result.append("Is Private: ").append(repo.getPrivateRepo() ? "Yes" : "No").append("\n");
                }

                if (repo.getOwner() != null) {
                    result.append("Owner: ").append(repo.getOwner().getLogin()).append("\n");
                }

                if (repo.getNamespace() != null) {
                    result.append("Namespace: ").append(repo.getNamespace().getName()).append("\n");
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
            if (StringUtils.isNotBlank(owner)) {
                result.append("Owner: ").append(owner).append("\n");
            }
            if (StringUtils.isNotBlank(language)) {
                result.append("Programming Language: ").append(language).append("\n");
            }
            if (StringUtils.isNotBlank(fork)) {
                result.append("Include Fork: ").append(fork).append("\n");
            }

            return new ToolExecuteResult(result.toString());

        } catch (GitCodeException e) {
            log.error("GitCode repository search failed", e);
            String errorMsg = "GitCode repository search failed: ";

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
            log.error("GitCode repository search tool execution failed", e);
            return new ToolExecuteResult("Search failed: " + e.getMessage());
        }
    }
}