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
import com.alibaba.langengine.gitcode.sdk.Issue;
import com.alibaba.langengine.gitcode.sdk.Repository;
import com.alibaba.langengine.gitcode.sdk.SearchRequest;
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

class GitCodeIssueSearchToolTest {

    @Mock
    private GitCodeClient mockGitCodeClient;

    private GitCodeIssueSearchTool gitCodeIssueSearchTool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gitCodeIssueSearchTool = new GitCodeIssueSearchTool(mockGitCodeClient);
    }

    @Test
    void testGetName() {
        assertEquals("GitCodeIssueSearchTool", gitCodeIssueSearchTool.getName());
    }

    @Test
    void testGetDescription() {
        String description = gitCodeIssueSearchTool.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("GitCode Issues search tool"));
        assertTrue(description.contains("query"));
        assertTrue(description.contains("sort"));
        assertTrue(description.contains("order"));
    }

    @Test
    void testRunWithEmptyQuery() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "");
        String toolInput = JSON.toJSONString(input);

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        assertEquals("Error: Search keywords cannot be empty", result.getOutput());
        verify(mockGitCodeClient, never()).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testRunWithNullQuery() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        String toolInput = JSON.toJSONString(input);

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        assertEquals("Error: Search keywords cannot be empty", result.getOutput());
        verify(mockGitCodeClient, never()).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testRunWithValidQueryAndNoResults() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test issue");
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchIssues((SearchRequest) any()))
                .thenReturn(Collections.emptyList());

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        assertEquals("No related issues found", result.getOutput());
        verify(mockGitCodeClient, times(1)).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testRunWithValidQueryAndResults() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test issue");
        input.put("sort", "created_at");
        input.put("order", "asc");
        input.put("repo", "alibaba/spring-boot");
        input.put("state", "open");
        input.put("page", 1);
        input.put("perPage", 10);
        String toolInput = JSON.toJSONString(input);

        Issue issue1 = new Issue();
        issue1.setId(1);
        issue1.setNumber("101");
        issue1.setTitle("Test Issue 1");
        issue1.setState("open");
        issue1.setBody("This is a test issue body");
        issue1.setHtmlUrl("https://gitcode.com/alibaba/spring-boot/issues/101");
        issue1.setCreatedAt("2025-01-01T10:00:00Z");
        issue1.setUpdatedAt("2025-01-02T10:00:00Z");
        issue1.setComments(5);
        issue1.setPriority(1);

        Repository repository = new Repository();
        repository.setFullName("alibaba/spring-boot");
        repository.setUrl("https://gitcode.com/alibaba/spring-boot");
        issue1.setRepository(repository);

        Issue issue2 = new Issue();
        issue2.setId(2);
        issue2.setNumber("102");
        issue2.setTitle("Test Issue 2");
        issue2.setState("closed");
        issue2.setBody("This is another test issue with a very long body that should be truncated when it exceeds 200 characters. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.");
        issue2.setHtmlUrl("https://gitcode.com/alibaba/spring-boot/issues/102");

        List<Issue> issues = Arrays.asList(issue1, issue2);

        when(mockGitCodeClient.searchIssues((SearchRequest) any()))
                .thenReturn(issues);

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("Found 2 related issues"));
        assertTrue(output.contains("Issue 1:"));
        assertTrue(output.contains("Issue 2:"));
        assertTrue(output.contains("Test Issue 1"));
        assertTrue(output.contains("Test Issue 2"));
        assertTrue(output.contains("Issue ID: 1"));
        assertTrue(output.contains("Issue ID: 2"));
        assertTrue(output.contains("Number: 101"));
        assertTrue(output.contains("Number: 102"));
        assertTrue(output.contains("Status: open"));
        assertTrue(output.contains("Status: closed"));
        assertTrue(output.contains("Content: This is a test issue body"));
        assertTrue(output.contains("..."));
        assertTrue(output.contains("Repository: alibaba/spring-boot"));
        assertTrue(output.contains("Created At: 2025-01-01T10:00:00Z"));
        assertTrue(output.contains("Updated At: 2025-01-02T10:00:00Z"));
        assertTrue(output.contains("Comments: 5"));
        assertTrue(output.contains("Priority: 1"));
        assertTrue(output.contains("Search Parameters:"));
        assertTrue(output.contains("Keywords: test issue"));
        assertTrue(output.contains("Page: 1"));
        assertTrue(output.contains("Per Page: 10"));
        assertTrue(output.contains("Sort: created_at asc"));
        assertTrue(output.contains("Repository: alibaba/spring-boot"));
        assertTrue(output.contains("State: open"));

        verify(mockGitCodeClient, times(1)).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testRunWithInvalidPage() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test issue");
        input.put("page", -1);
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchIssues((SearchRequest) any()))
                .thenReturn(Collections.emptyList());

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        assertEquals("No related issues found", result.getOutput());
        verify(mockGitCodeClient, times(1)).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testRunWithInvalidPerPage() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test issue");
        input.put("perPage", 100);
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchIssues((SearchRequest) any()))
                .thenReturn(Collections.emptyList());

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        assertEquals("No related issues found", result.getOutput());
        verify(mockGitCodeClient, times(1)).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testRunWithAuthenticationError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test issue");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Authentication failed", 401, "");
        when(mockGitCodeClient.searchIssues((SearchRequest) any()))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode Issues search failed"));
        assertTrue(output.contains("Authentication failed"));
        assertTrue(output.contains("GITCODE_ACCESS_TOKEN"));
    }

    @Test
    void testRunWithRateLimitError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test issue");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Rate limit exceeded", 429, "");
        when(mockGitCodeClient.searchIssues((SearchRequest) any()))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode Issues search failed"));
        assertTrue(output.contains("Rate limit exceeded"));
    }

    @Test
    void testRunWithForbiddenError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test issue");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Access forbidden", 403, "");
        when(mockGitCodeClient.searchIssues((SearchRequest) any()))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode Issues search failed"));
        assertTrue(output.contains("Access forbidden"));
    }

    @Test
    void testRunWithNotFoundError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test issue");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Resource not found", 404, "");
        when(mockGitCodeClient.searchIssues((SearchRequest) any()))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode Issues search failed"));
        assertTrue(output.contains("Resource not found"));
    }

    @Test
    void testRunWithGenericGitCodeException() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test issue");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Generic error");
        when(mockGitCodeClient.searchIssues((SearchRequest) any()))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode Issues search failed"));
        assertTrue(output.contains("Generic error"));
    }

    @Test
    void testRunWithGenericException() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test issue");
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchIssues((SearchRequest) any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        ToolExecuteResult result = gitCodeIssueSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("Search failed"));
        assertTrue(output.contains("Unexpected error"));
    }

    @Test
    void testRunWithInvalidJson() {
        String invalidJson = "invalid json";

        ToolExecuteResult result = gitCodeIssueSearchTool.run(invalidJson);

        String output = result.getOutput();
        assertTrue(output.contains("Search failed"));
    }
}