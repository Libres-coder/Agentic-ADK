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
import com.alibaba.langengine.gitee.sdk.Issue;
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
class GiteeIssueSearchToolTest {

    @Mock
    private GiteeClient mockGiteeClient;

    private GiteeIssueSearchTool tool;

    @BeforeEach
    void setUp() {
        tool = new GiteeIssueSearchTool(mockGiteeClient);
    }

    @Test
    void testToolConfiguration() {
        assertEquals("GiteeIssueSearchTool", tool.getName());
        assertNotNull(tool.getDescription());
        assertNotNull(tool.getParameters());
        assertTrue(tool.getDescription().contains("Gitee Issues search tool"));
    }

    @Test
    void testSearchIssuesSuccess() throws GiteeException {
        // Mock issue data
        Issue issue1 = createMockIssue(1, "Issue 1", "open", "Test issue 1");
        Issue issue2 = createMockIssue(2, "Issue 2", "closed", "Test issue 2");

        List<Issue> mockIssues = Arrays.asList(issue1, issue2);

        when(mockGiteeClient.searchIssues(any(SearchRequest.class))).thenReturn(mockIssues);

        String input = "{\"query\":\"test\",\"page\":1,\"perPage\":20}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().contains("Found 2 related issues"));
        assertTrue(result.getOutput().contains("Issue 1"));
        assertTrue(result.getOutput().contains("Issue 2"));
        
        verify(mockGiteeClient, times(1)).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testSearchIssuesNoResults() throws GiteeException {
        when(mockGiteeClient.searchIssues(any(SearchRequest.class))).thenReturn(Collections.emptyList());

        String input = "{\"query\":\"nonexistent\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertEquals("No related issues found", result.getOutput());
        
        verify(mockGiteeClient, times(1)).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testSearchIssuesEmptyQuery() throws GiteeException {
        String input = "{\"query\":\"\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Error: Search keywords cannot be empty"));
        
        verify(mockGiteeClient, never()).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testSearchIssuesWithAllParameters() throws GiteeException {
        Issue issue = createMockIssue(1, "Bug Issue", "open", "This is a bug");

        when(mockGiteeClient.searchIssues(any(SearchRequest.class))).thenReturn(Arrays.asList(issue));

        String input = "{\"query\":\"bug\",\"repo\":\"owner/repo\",\"state\":\"open\",\"language\":\"java\",\"label\":\"bug\",\"author\":\"testuser\",\"assignee\":\"assignee\",\"sort\":\"created_at\",\"order\":\"desc\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Repository: owner/repo"));
        assertTrue(result.getOutput().contains("State: open"));
        assertTrue(result.getOutput().contains("Language: java"));
        assertTrue(result.getOutput().contains("Label: bug"));
        assertTrue(result.getOutput().contains("Author: testuser"));
        assertTrue(result.getOutput().contains("Assignee: assignee"));
        assertTrue(result.getOutput().contains("Sort: created_at desc"));
        
        verify(mockGiteeClient, times(1)).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testSearchIssuesWithPriority() throws GiteeException {
        Issue issue = createMockIssue(1, "Critical Issue", "open", "Critical issue");
        issue.setPriority(4); // Critical

        when(mockGiteeClient.searchIssues(any(SearchRequest.class))).thenReturn(Arrays.asList(issue));

        String input = "{\"query\":\"critical\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Priority: Critical"));
        
        verify(mockGiteeClient, times(1)).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testSearchIssuesApiException() throws GiteeException {
        GiteeException exception = new GiteeException("API Error", 403, "Forbidden");
        when(mockGiteeClient.searchIssues(any(SearchRequest.class))).thenThrow(exception);

        String input = "{\"query\":\"test\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Gitee Issues search failed"));
        assertTrue(result.getOutput().contains("Access forbidden"));
        
        verify(mockGiteeClient, times(1)).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testSearchIssuesGenericException() throws GiteeException {
        when(mockGiteeClient.searchIssues(any(SearchRequest.class))).thenThrow(new RuntimeException("Generic error"));

        String input = "{\"query\":\"test\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Search failed: Generic error"));
        
        verify(mockGiteeClient, times(1)).searchIssues(any(SearchRequest.class));
    }

    @Test
    void testPageParameterValidation() throws GiteeException {
        Issue issue = createMockIssue(1, "Test Issue", "open", "Test");

        when(mockGiteeClient.searchIssues(any(SearchRequest.class))).thenReturn(Arrays.asList(issue));

        // Test with invalid page numbers
        String input = "{\"query\":\"test\",\"page\":0,\"perPage\":200}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Page: 1"));
        assertTrue(result.getOutput().contains("Per Page: 20"));
        
        verify(mockGiteeClient, times(1)).searchIssues(any(SearchRequest.class));
    }

    private Issue createMockIssue(Integer id, String title, String state, String body) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setNumber(String.valueOf(id));
        issue.setTitle(title);
        issue.setState(state);
        issue.setBody(body);
        issue.setHtmlUrl("https://gitee.com/owner/repo/issues/" + id);
        issue.setCreatedAt("2023-01-01T00:00:00Z");
        issue.setUpdatedAt("2023-01-01T00:00:00Z");
        issue.setComments(0);

        // Mock repository
        Repository repository = new Repository();
        repository.setFullName("owner/repo");
        repository.setHtmlUrl("https://gitee.com/owner/repo");
        issue.setRepository(repository);

        // Mock user
        User user = new User();
        user.setLogin("testuser");
        issue.setUser(user);

        return issue;
    }
}