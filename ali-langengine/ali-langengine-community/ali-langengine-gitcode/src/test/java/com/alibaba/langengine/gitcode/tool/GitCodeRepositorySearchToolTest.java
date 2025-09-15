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
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.gitcode.sdk.GitCodeClient;
import com.alibaba.langengine.gitcode.sdk.GitCodeException;
import com.alibaba.langengine.gitcode.sdk.Repository;
import com.alibaba.langengine.gitcode.sdk.SearchRequest;
import com.alibaba.langengine.gitcode.sdk.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GitCodeRepositorySearchToolTest {

    @Mock
    private GitCodeClient mockGitCodeClient;

    private GitCodeRepositorySearchTool gitCodeRepositorySearchTool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gitCodeRepositorySearchTool = new GitCodeRepositorySearchTool(mockGitCodeClient);
    }

    @Test
    void testGetName() {
        assertEquals("GitCodeRepositorySearchTool", gitCodeRepositorySearchTool.getName());
    }

    @Test
    void testGetDescription() {
        String description = gitCodeRepositorySearchTool.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("GitCode repository search tool"));
        assertTrue(description.contains("query"));
        assertTrue(description.contains("sort"));
        assertTrue(description.contains("order"));
    }

    @Test
    void testRunWithEmptyQuery() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "");
        String toolInput = JSON.toJSONString(input);

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        assertEquals("Error: Search keywords cannot be empty", result.getOutput());
        verify(mockGitCodeClient, never()).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testRunWithNullQuery() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        String toolInput = JSON.toJSONString(input);

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        assertEquals("Error: Search keywords cannot be empty", result.getOutput());
        verify(mockGitCodeClient, never()).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testRunWithValidQueryAndNoResults() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test repository");
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchRepositories((SearchRequest) any()))
                .thenReturn(Collections.emptyList());

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        assertEquals("No related repositories found", result.getOutput());
        verify(mockGitCodeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testRunWithValidQueryAndResults() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "spring boot");
        input.put("sort", "stars_count");
        input.put("order", "desc");
        input.put("owner", "alibaba");
        input.put("language", "java");
        input.put("fork", "false");
        input.put("page", 1);
        input.put("perPage", 20);
        String toolInput = JSON.toJSONString(input);

        Repository repo1 = new Repository();
        repo1.setId(1);
        repo1.setName("spring-boot");
        repo1.setFullName("alibaba/spring-boot");
        repo1.setHumanName("Spring Boot Framework");
        repo1.setDescription("Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications");
        repo1.setWebUrl("https://gitcode.com/alibaba/spring-boot");
        repo1.setHttpUrlToRepo("https://gitcode.com/alibaba/spring-boot.git");
        repo1.setStargazersCount(1000);
        repo1.setForksCount(500);
        repo1.setOpenIssuesCount(10);
        repo1.setDefaultBranch("main");
        repo1.setCreatedAt("2024-01-01T10:00:00Z");
        repo1.setUpdatedAt("2025-01-01T10:00:00Z");
        repo1.setPushedAt("2025-01-02T10:00:00Z");
        repo1.setFork(false);
        repo1.setPrivateRepo(false);

        User owner = new User();
        owner.setLogin("alibaba");
        repo1.setOwner(owner);

        Repository.Namespace namespace = new Repository.Namespace();
        namespace.setName("alibaba");
        repo1.setNamespace(namespace);

        Repository repo2 = new Repository();
        repo2.setId(2);
        repo2.setName("spring-cloud");
        repo2.setFullName("alibaba/spring-cloud");
        repo2.setDescription("Spring Cloud for microservices");
        repo2.setWebUrl("https://gitcode.com/alibaba/spring-cloud");
        repo2.setStargazersCount(800);
        repo2.setForksCount(300);
        repo2.setOpenIssuesCount(5);
        repo2.setFork(true);
        repo2.setPrivateRepo(true);
        repo2.setOwner(owner);
        repo2.setNamespace(namespace);

        List<Repository> repositories = Arrays.asList(repo1, repo2);

        when(mockGitCodeClient.searchRepositories((SearchRequest) any()))
                .thenReturn(repositories);

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("Found 2 related repositories"));
        assertTrue(output.contains("Repository 1:"));
        assertTrue(output.contains("Repository 2:"));
        assertTrue(output.contains("Repository ID: 1"));
        assertTrue(output.contains("Repository ID: 2"));
        assertTrue(output.contains("Repository Name: spring-boot"));
        assertTrue(output.contains("Repository Name: spring-cloud"));
        assertTrue(output.contains("Full Name: alibaba/spring-boot"));
        assertTrue(output.contains("Full Name: alibaba/spring-cloud"));
        assertTrue(output.contains("Display Name: Spring Boot Framework"));
        assertTrue(output.contains("Description: Spring Boot makes it easy"));
        assertTrue(output.contains("Description: Spring Cloud for microservices"));
        assertTrue(output.contains("Project URL: https://gitcode.com/alibaba/spring-boot"));
        assertTrue(output.contains("Clone URL: https://gitcode.com/alibaba/spring-boot.git"));
        assertTrue(output.contains("Stars: 1000"));
        assertTrue(output.contains("Stars: 800"));
        assertTrue(output.contains("Forks: 500"));
        assertTrue(output.contains("Forks: 300"));
        assertTrue(output.contains("Open Issues: 10"));
        assertTrue(output.contains("Open Issues: 5"));
        assertTrue(output.contains("Default Branch: main"));
        assertTrue(output.contains("Created At: 2024-01-01T10:00:00Z"));
        assertTrue(output.contains("Updated At: 2025-01-01T10:00:00Z"));
        assertTrue(output.contains("Last Push: 2025-01-02T10:00:00Z"));
        assertTrue(output.contains("Is Fork: No"));
        assertTrue(output.contains("Is Fork: Yes"));
        assertTrue(output.contains("Is Private: No"));
        assertTrue(output.contains("Is Private: Yes"));
        assertTrue(output.contains("Owner: alibaba"));
        assertTrue(output.contains("Namespace: alibaba"));
        assertTrue(output.contains("Search Parameters:"));
        assertTrue(output.contains("Keywords: spring boot"));
        assertTrue(output.contains("Page: 1"));
        assertTrue(output.contains("Per Page: 20"));
        assertTrue(output.contains("Sort: stars_count desc"));
        assertTrue(output.contains("Owner: alibaba"));
        assertTrue(output.contains("Programming Language: java"));
        assertTrue(output.contains("Include Fork: false"));

        verify(mockGitCodeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testRunWithInvalidPage() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test repository");
        input.put("page", 0);
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchRepositories((SearchRequest) any()))
                .thenReturn(Collections.emptyList());

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        assertEquals("No related repositories found", result.getOutput());
        verify(mockGitCodeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testRunWithInvalidPerPage() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test repository");
        input.put("perPage", 0);
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchRepositories((SearchRequest) any()))
                .thenReturn(Collections.emptyList());

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        assertEquals("No related repositories found", result.getOutput());
        verify(mockGitCodeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testRunWithAuthenticationError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test repository");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Authentication failed", 401, "");
        when(mockGitCodeClient.searchRepositories((SearchRequest) any()))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode repository search failed"));
        assertTrue(output.contains("Authentication failed"));
        assertTrue(output.contains("GITCODE_ACCESS_TOKEN"));
    }

    @Test
    void testRunWithRateLimitError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test repository");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Rate limit exceeded", 429, "");
        when(mockGitCodeClient.searchRepositories((SearchRequest) any()))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode repository search failed"));
        assertTrue(output.contains("Rate limit exceeded"));
    }

    @Test
    void testRunWithForbiddenError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test repository");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Access forbidden", 403, "");
        when(mockGitCodeClient.searchRepositories((SearchRequest) any()))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode repository search failed"));
        assertTrue(output.contains("Access forbidden"));
    }

    @Test
    void testRunWithNotFoundError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test repository");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Resource not found", 404, "");
        when(mockGitCodeClient.searchRepositories((SearchRequest) any()))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode repository search failed"));
        assertTrue(output.contains("Resource not found"));
    }

    @Test
    void testRunWithGenericGitCodeException() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test repository");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Generic error");
        when(mockGitCodeClient.searchRepositories((SearchRequest) any()))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode repository search failed"));
        assertTrue(output.contains("Generic error"));
    }

    @Test
    void testRunWithGenericException() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test repository");
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchRepositories((SearchRequest) any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("Search failed"));
        assertTrue(output.contains("Unexpected error"));
    }

    @Test
    void testRunWithInvalidJson() {
        String invalidJson = "invalid json";

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(invalidJson);

        String output = result.getOutput();
        assertTrue(output.contains("Search failed"));
    }

    @Test
    void testRunWithMinimalRepositoryData() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "minimal repo");
        String toolInput = JSON.toJSONString(input);

        Repository repo = new Repository();
        repo.setId(1);
        repo.setName("minimal");
        repo.setFullName("user/minimal");

        List<Repository> repositories = Collections.singletonList(repo);

        when(mockGitCodeClient.searchRepositories((SearchRequest) any()))
                .thenReturn(repositories);

        ToolExecuteResult result = gitCodeRepositorySearchTool.run(toolInput);

        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("Found 1 related repositories"));
        assertTrue(output.contains("Repository ID: 1"));
        assertTrue(output.contains("Repository Name: minimal"));
        assertTrue(output.contains("Full Name: user/minimal"));

        verify(mockGitCodeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }
}