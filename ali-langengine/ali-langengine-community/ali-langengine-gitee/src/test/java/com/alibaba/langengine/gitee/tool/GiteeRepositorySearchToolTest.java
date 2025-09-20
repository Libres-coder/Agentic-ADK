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

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.gitee.sdk.GiteeClient;
import com.alibaba.langengine.gitee.sdk.GiteeException;
import com.alibaba.langengine.gitee.sdk.Repository;
import com.alibaba.langengine.gitee.sdk.SearchRequest;
import com.alibaba.langengine.gitee.sdk.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiteeRepositorySearchToolTest {

    @Mock
    private GiteeClient mockGiteeClient;

    private GiteeRepositorySearchTool tool;

    @BeforeEach
    void setUp() {
        tool = new GiteeRepositorySearchTool(mockGiteeClient);
    }

    @Test
    void testToolConfiguration() {
        assertEquals("GiteeRepositorySearchTool", tool.getName());
        assertNotNull(tool.getDescription());
        assertNotNull(tool.getParameters());
        assertTrue(tool.getDescription().contains("Gitee repository search tool"));
    }

    @Test
    void testSearchRepositoriesSuccess() throws GiteeException {
        // Mock repository data
        Repository repo1 = createMockRepository(1, "test-repo-1", "owner/test-repo-1", "Test repository 1");
        Repository repo2 = createMockRepository(2, "test-repo-2", "owner/test-repo-2", "Test repository 2");

        List<Repository> mockRepositories = Arrays.asList(repo1, repo2);

        when(mockGiteeClient.searchRepositories(any(SearchRequest.class))).thenReturn(mockRepositories);

        String input = "{\"query\":\"test\",\"page\":1,\"perPage\":20}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().contains("Found 2 related repositories"));
        assertTrue(result.getOutput().contains("test-repo-1"));
        assertTrue(result.getOutput().contains("test-repo-2"));
        
        verify(mockGiteeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testSearchRepositoriesNoResults() throws GiteeException {
        when(mockGiteeClient.searchRepositories(any(SearchRequest.class))).thenReturn(Collections.emptyList());

        String input = "{\"query\":\"nonexistent\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertEquals("No related repositories found", result.getOutput());
        
        verify(mockGiteeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testSearchRepositoriesEmptyQuery() throws GiteeException {
        String input = "{\"query\":\"\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Error: Search keywords cannot be empty"));
        
        verify(mockGiteeClient, never()).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testSearchRepositoriesWithAllParameters() throws GiteeException {
        Repository repo = createMockRepository(1, "spring-boot", "alibaba/spring-boot", "Spring Boot framework");
        repo.setLanguage("Java");

        when(mockGiteeClient.searchRepositories(any(SearchRequest.class))).thenReturn(Arrays.asList(repo));

        String input = "{\"query\":\"spring\",\"owner\":\"alibaba\",\"language\":\"java\",\"fork\":true,\"sort\":\"stars_count\",\"order\":\"desc\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Owner: alibaba"));
        assertTrue(result.getOutput().contains("Programming Language: java"));
        assertTrue(result.getOutput().contains("Include Fork: true"));
        assertTrue(result.getOutput().contains("Sort: stars_count desc"));
        
        verify(mockGiteeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testSearchRepositoriesWithStats() throws GiteeException {
        Repository repo = createMockRepository(1, "popular-repo", "owner/popular-repo", "A popular repository");
        repo.setStargazersCount(1000);
        repo.setForksCount(200);
        repo.setWatchersCount(150);
        repo.setOpenIssuesCount(50);
        repo.setLanguage("JavaScript");
        repo.setLicense("MIT");
        repo.setHomepage("https://example.com");

        when(mockGiteeClient.searchRepositories(any(SearchRequest.class))).thenReturn(Arrays.asList(repo));

        String input = "{\"query\":\"popular\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Stars: 1000"));
        assertTrue(result.getOutput().contains("Forks: 200"));
        assertTrue(result.getOutput().contains("Watchers: 150"));
        assertTrue(result.getOutput().contains("Open Issues: 50"));
        assertTrue(result.getOutput().contains("Language: JavaScript"));
        assertTrue(result.getOutput().contains("License: MIT"));
        assertTrue(result.getOutput().contains("Homepage: https://example.com"));
        
        verify(mockGiteeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testSearchRepositoriesWithFlags() throws GiteeException {
        Repository repo = createMockRepository(1, "special-repo", "owner/special-repo", "A special repository");
        repo.setFork(true);
        repo.setPrivateRepo(false);
        repo.setPublicRepo(true);
        repo.setInternal(false);
        repo.setRecommend(true);
        repo.setGvp(true);

        when(mockGiteeClient.searchRepositories(any(SearchRequest.class))).thenReturn(Arrays.asList(repo));

        String input = "{\"query\":\"special\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Is Fork: Yes"));
        assertTrue(result.getOutput().contains("Is Private: No"));
        assertTrue(result.getOutput().contains("Is Public: Yes"));
        assertTrue(result.getOutput().contains("Is Internal: No"));
        assertTrue(result.getOutput().contains("Recommended: Yes"));
        assertTrue(result.getOutput().contains("GVP Project: Yes"));
        
        verify(mockGiteeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testSearchRepositoriesApiException() throws GiteeException {
        GiteeException exception = new GiteeException("API Error", 429, "Rate limit exceeded");
        when(mockGiteeClient.searchRepositories(any(SearchRequest.class))).thenThrow(exception);

        String input = "{\"query\":\"test\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Gitee repository search failed"));
        assertTrue(result.getOutput().contains("Rate limit exceeded"));
        
        verify(mockGiteeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testSearchRepositoriesGenericException() throws GiteeException {
        when(mockGiteeClient.searchRepositories(any(SearchRequest.class))).thenThrow(new RuntimeException("Generic error"));

        String input = "{\"query\":\"test\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Search failed: Generic error"));
        
        verify(mockGiteeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    @Test
    void testPageParameterValidation() throws GiteeException {
        Repository repo = createMockRepository(1, "test-repo", "owner/test-repo", "Test repository");

        when(mockGiteeClient.searchRepositories(any(SearchRequest.class))).thenReturn(Arrays.asList(repo));

        // Test with invalid page numbers
        String input = "{\"query\":\"test\",\"page\":101,\"perPage\":200}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Page: 1"));
        assertTrue(result.getOutput().contains("Per Page: 20"));
        
        verify(mockGiteeClient, times(1)).searchRepositories(any(SearchRequest.class));
    }

    private Repository createMockRepository(Integer id, String name, String fullName, String description) {
        Repository repository = new Repository();
        repository.setId(id);
        repository.setName(name);
        repository.setFullName(fullName);
        repository.setDescription(description);
        repository.setHtmlUrl("https://gitee.com/" + fullName);
        repository.setSshUrl("git@gitee.com:" + fullName + ".git");
        repository.setDefaultBranch("main");
        repository.setCreatedAt("2023-01-01T00:00:00Z");
        repository.setUpdatedAt("2023-01-01T00:00:00Z");
        repository.setPushedAt("2023-01-01T00:00:00Z");

        // Mock owner
        User owner = new User();
        owner.setLogin("owner");
        owner.setName("Owner Name");
        repository.setOwner(owner);

        // Mock namespace
        Repository.Namespace namespace = new Repository.Namespace();
        namespace.setName("owner");
        namespace.setType("User");
        repository.setNamespace(namespace);

        return repository;
    }
}