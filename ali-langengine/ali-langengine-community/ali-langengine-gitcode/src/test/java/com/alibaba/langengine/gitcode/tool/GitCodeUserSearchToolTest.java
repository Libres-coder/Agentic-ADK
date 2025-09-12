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

class GitCodeUserSearchToolTest {

    @Mock
    private GitCodeClient mockGitCodeClient;

    private GitCodeUserSearchTool gitCodeUserSearchTool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gitCodeUserSearchTool = new GitCodeUserSearchTool(mockGitCodeClient);
    }

    @Test
    void testGetName() {
        assertEquals("GitCodeUserSearchTool", gitCodeUserSearchTool.getName());
    }

    @Test
    void testGetDescription() {
        String description = gitCodeUserSearchTool.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("GitCode user search tool"));
        assertTrue(description.contains("query"));
        assertTrue(description.contains("sort"));
        assertTrue(description.contains("order"));
    }

    @Test
    void testRunWithEmptyQuery() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "");
        String toolInput = JSON.toJSONString(input);

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        assertEquals("Error: Search keywords cannot be empty", result.getOutput());
        verify(mockGitCodeClient, never()).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testRunWithNullQuery() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        String toolInput = JSON.toJSONString(input);

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        assertEquals("Error: Search keywords cannot be empty", result.getOutput());
        verify(mockGitCodeClient, never()).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testRunWithValidQueryAndNoResults() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test user");
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenReturn(Collections.emptyList());

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        assertEquals("No related users found", result.getOutput());
        verify(mockGitCodeClient, times(1)).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testRunWithValidQueryAndResults() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "alibaba");
        input.put("sort", "joined_at");
        input.put("order", "desc");
        input.put("page", 1);
        input.put("perPage", 20);
        String toolInput = JSON.toJSONString(input);

        User user1 = new User();
        user1.setId("1");
        user1.setLogin("alibaba-admin");
        user1.setName("Alibaba Administrator");
        user1.setHtmlUrl("https://gitcode.com/alibaba-admin");
        user1.setAvatarUrl("https://gitcode.com/avatars/alibaba-admin.jpg");
        user1.setCreatedAt("2024-01-01T10:00:00Z");
        user1.setType("User");

        User user2 = new User();
        user2.setId("2");
        user2.setLogin("alibaba-org");
        user2.setName("Alibaba Organization");
        user2.setHtmlUrl("https://gitcode.com/alibaba-org");
        user2.setAvatarUrl("https://gitcode.com/avatars/alibaba-org.jpg");
        user2.setCreatedAt("2023-12-01T10:00:00Z");
        user2.setType("Organization");

        List<User> users = Arrays.asList(user1, user2);

        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenReturn(users);

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("Found 2 related users"));
        assertTrue(output.contains("User 1:"));
        assertTrue(output.contains("User 2:"));
        assertTrue(output.contains("User ID: 1"));
        assertTrue(output.contains("User ID: 2"));
        assertTrue(output.contains("Login: alibaba-admin"));
        assertTrue(output.contains("Login: alibaba-org"));
        assertTrue(output.contains("Name: Alibaba Administrator"));
        assertTrue(output.contains("Name: Alibaba Organization"));
        assertTrue(output.contains("Profile URL: https://gitcode.com/alibaba-admin"));
        assertTrue(output.contains("Profile URL: https://gitcode.com/alibaba-org"));
        assertTrue(output.contains("Avatar: https://gitcode.com/avatars/alibaba-admin.jpg"));
        assertTrue(output.contains("Avatar: https://gitcode.com/avatars/alibaba-org.jpg"));
        assertTrue(output.contains("Created At: 2024-01-01T10:00:00Z"));
        assertTrue(output.contains("Created At: 2023-12-01T10:00:00Z"));
        assertTrue(output.contains("User Type: User"));
        assertTrue(output.contains("User Type: Organization"));
        assertTrue(output.contains("Search Parameters:"));
        assertTrue(output.contains("Keywords: alibaba"));
        assertTrue(output.contains("Page: 1"));
        assertTrue(output.contains("Per Page: 20"));
        assertTrue(output.contains("Sort: joined_at desc"));

        verify(mockGitCodeClient, times(1)).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testRunWithInvalidPage() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test user");
        input.put("page", -1);
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenReturn(Collections.emptyList());

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        assertEquals("No related users found", result.getOutput());
        verify(mockGitCodeClient, times(1)).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testRunWithInvalidPerPage() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test user");
        input.put("perPage", 100);
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenReturn(Collections.emptyList());

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        assertEquals("No related users found", result.getOutput());
        verify(mockGitCodeClient, times(1)).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testRunWithAuthenticationError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test user");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Authentication failed", 401, "");
        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode user search failed"));
        assertTrue(output.contains("Authentication failed"));
        assertTrue(output.contains("GITCODE_ACCESS_TOKEN"));
    }

    @Test
    void testRunWithRateLimitError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test user");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Rate limit exceeded", 429, "");
        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode user search failed"));
        assertTrue(output.contains("Rate limit exceeded"));
    }

    @Test
    void testRunWithForbiddenError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test user");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Access forbidden", 403, "");
        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode user search failed"));
        assertTrue(output.contains("Access forbidden"));
    }

    @Test
    void testRunWithNotFoundError() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test user");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Resource not found", 404, "");
        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode user search failed"));
        assertTrue(output.contains("Resource not found"));
    }

    @Test
    void testRunWithGenericGitCodeException() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test user");
        String toolInput = JSON.toJSONString(input);

        GitCodeException exception = new GitCodeException("Generic error");
        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenThrow(exception);

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("GitCode user search failed"));
        assertTrue(output.contains("Generic error"));
    }

    @Test
    void testRunWithGenericException() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test user");
        String toolInput = JSON.toJSONString(input);

        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        String output = result.getOutput();
        assertTrue(output.contains("Search failed"));
        assertTrue(output.contains("Unexpected error"));
    }

    @Test
    void testRunWithInvalidJson() {
        String invalidJson = "invalid json";

        ToolExecuteResult result = gitCodeUserSearchTool.run(invalidJson);

        String output = result.getOutput();
        assertTrue(output.contains("Search failed"));
    }

    @Test
    void testRunWithMinimalUserData() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "minimal user");
        String toolInput = JSON.toJSONString(input);

        User user = new User();
        user.setId("1");
        user.setLogin("minimal");

        List<User> users = Collections.singletonList(user);

        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenReturn(users);

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("Found 1 related users"));
        assertTrue(output.contains("User ID: 1"));
        assertTrue(output.contains("Login: minimal"));

        verify(mockGitCodeClient, times(1)).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testRunWithDefaultSort() throws GitCodeException {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "test user");
        String toolInput = JSON.toJSONString(input);

        User user = new User();
        user.setId("1");
        user.setLogin("testuser");

        List<User> users = Collections.singletonList(user);

        when(mockGitCodeClient.searchUsers(any(SearchRequest.class)))
                .thenReturn(users);

        ToolExecuteResult result = gitCodeUserSearchTool.run(toolInput);

        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("Found 1 related users"));
        assertFalse(output.contains("Sort:"));

        verify(mockGitCodeClient, times(1)).searchUsers(any(SearchRequest.class));
    }
}