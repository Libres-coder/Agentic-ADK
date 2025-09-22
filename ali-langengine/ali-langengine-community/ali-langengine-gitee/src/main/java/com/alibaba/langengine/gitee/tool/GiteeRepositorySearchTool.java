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
import com.alibaba.langengine.gitee.sdk.Repository;
import com.alibaba.langengine.gitee.sdk.SearchRequest;
import com.alibaba.langengine.gitee.util.LanguageMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class GiteeRepositorySearchTool extends DefaultTool {

    private GiteeClient giteeClient;

    public GiteeRepositorySearchTool() {
        this.giteeClient = new GiteeClient();
        init();
    }

    public GiteeRepositorySearchTool(GiteeClient giteeClient) {
        this.giteeClient = giteeClient;
        init();
    }

    private void init() {
        setName("GiteeRepositorySearchTool");
        setDescription("Gitee repository search tool for searching code repositories on the Gitee platform. Input parameters: query(search keywords), sort(sort field), order(sort order), owner(owner), language(programming language), fork(include fork repositories), page(page number), perPage(results per page)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Search keywords for searching repository names, descriptions, etc.\"\n" +
                "    },\n" +
                "    \"sort\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Sort field, options: last_push_at(update time), stars_count(star count), forks_count(fork count), watches_count(watch count), defaults to best match\",\n" +
                "      \"enum\": [\"last_push_at\", \"stars_count\", \"forks_count\", \"watches_count\"]\n" +
                "    },\n" +
                "    \"order\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Sort order, options: asc(ascending) or desc(descending), defaults to desc\",\n" +
                "      \"enum\": [\"asc\", \"desc\"]\n" +
                "    },\n" +
                "    \"owner\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Repository owner (organization, group or personal address path)\"\n" +
                "    },\n" +
                "    \"language\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Filter repositories by specified programming language, e.g.: Java, Python, JavaScript (首字母大写)\"\n" +
                "    },\n" +
                "    \"fork\": {\n" +
                "      \"type\": \"boolean\",\n" +
                "      \"description\": \"Whether to search repositories including forks, default: false\"\n" +
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
        log.info("Gitee repository search tool input: {}", toolInput);

        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String query = (String) inputMap.get("query");
            String sort = (String) inputMap.get("sort");
            String order = (String) inputMap.get("order");
            String owner = (String) inputMap.get("owner");
            String language = (String) inputMap.get("language");
            Boolean fork = (Boolean) inputMap.get("fork");
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

            SearchRequest request = SearchRequest.forRepositories(query)
                    .withPagination(page, perPage);

            if (StringUtils.isNotBlank(sort)) {
                request.withSort(sort, StringUtils.isNotBlank(order) ? order : "desc");
            }

            if (StringUtils.isNotBlank(owner)) {
                request.withOwner(owner);
            }

            if (StringUtils.isNotBlank(language)) {
                // 使用LanguageMapper映射语言参数
                String mappedLanguage = LanguageMapper.mapLanguage(language);
                log.debug("Language mapping: '{}' -> '{}'", language, mappedLanguage);
                request.withLanguage(mappedLanguage);
            }

            if (fork != null) {
                request.withFork(fork);
            }

            List<Repository> repositories = giteeClient.searchRepositories(request);

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

                if (StringUtils.isNotBlank(repo.getHtmlUrl())) {
                    result.append("Project URL: ").append(repo.getHtmlUrl()).append("\n");
                }

                if (StringUtils.isNotBlank(repo.getSshUrl())) {
                    result.append("SSH Clone URL: ").append(repo.getSshUrl()).append("\n");
                }

                if (repo.getStargazersCount() != null) {
                    result.append("Stars: ").append(repo.getStargazersCount()).append("\n");
                }

                if (repo.getForksCount() != null) {
                    result.append("Forks: ").append(repo.getForksCount()).append("\n");
                }

                if (repo.getWatchersCount() != null) {
                    result.append("Watchers: ").append(repo.getWatchersCount()).append("\n");
                }

                if (repo.getOpenIssuesCount() != null) {
                    result.append("Open Issues: ").append(repo.getOpenIssuesCount()).append("\n");
                }

                if (StringUtils.isNotBlank(repo.getLanguage())) {
                    result.append("Language: ").append(repo.getLanguage()).append("\n");
                }

                if (StringUtils.isNotBlank(repo.getLicense())) {
                    result.append("License: ").append(repo.getLicense()).append("\n");
                }

                if (StringUtils.isNotBlank(repo.getHomepage())) {
                    result.append("Homepage: ").append(repo.getHomepage()).append("\n");
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

                if (repo.getPublicRepo() != null) {
                    result.append("Is Public: ").append(repo.getPublicRepo() ? "Yes" : "No").append("\n");
                }

                if (repo.getInternal() != null) {
                    result.append("Is Internal: ").append(repo.getInternal() ? "Yes" : "No").append("\n");
                }

                if (repo.getRecommend() != null && repo.getRecommend()) {
                    result.append("Recommended: Yes\n");
                }

                if (repo.getGvp() != null && repo.getGvp()) {
                    result.append("GVP Project: Yes\n");
                }

                if (repo.getOwner() != null) {
                    result.append("Owner: ").append(repo.getOwner().getLogin()).append("\n");
                    if (StringUtils.isNotBlank(repo.getOwner().getName())) {
                        result.append("Owner Name: ").append(repo.getOwner().getName()).append("\n");
                    }
                }

                if (repo.getNamespace() != null) {
                    result.append("Namespace: ").append(repo.getNamespace().getName()).append("\n");
                    result.append("Namespace Type: ").append(repo.getNamespace().getType()).append("\n");
                }

                if (repo.getHasIssues() != null) {
                    result.append("Has Issues: ").append(repo.getHasIssues() ? "Yes" : "No").append("\n");
                }

                if (repo.getHasWiki() != null) {
                    result.append("Has Wiki: ").append(repo.getHasWiki() ? "Yes" : "No").append("\n");
                }

                if (repo.getHasPage() != null) {
                    result.append("Has Pages: ").append(repo.getHasPage() ? "Yes" : "No").append("\n");
                }

                if (repo.getPullRequestsEnabled() != null) {
                    result.append("Pull Requests Enabled: ").append(repo.getPullRequestsEnabled() ? "Yes" : "No").append("\n");
                }

                if (StringUtils.isNotBlank(repo.getProjectCreator())) {
                    result.append("Project Creator: ").append(repo.getProjectCreator()).append("\n");
                }

                if (repo.getParent() != null) {
                    result.append("Parent Repository: ").append(repo.getParent().getFullName()).append("\n");
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
            if (fork != null) {
                result.append("Include Fork: ").append(fork).append("\n");
            }

            return new ToolExecuteResult(result.toString());

        } catch (GiteeException e) {
            log.error("Gitee repository search failed", e);
            String errorMsg = "Gitee repository search failed: ";

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
            log.error("Gitee repository search tool execution failed", e);
            return new ToolExecuteResult("Search failed: " + e.getMessage());
        }
    }
}