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
class GiteeUserSearchToolTest {

    @Mock
    private GiteeClient mockGiteeClient;

    private GiteeUserSearchTool tool;

    @BeforeEach
    void setUp() {
        tool = new GiteeUserSearchTool(mockGiteeClient);
    }

    @Test
    void testToolConfiguration() {
        assertEquals("GiteeUserSearchTool", tool.getName());
        assertNotNull(tool.getDescription());
        assertNotNull(tool.getParameters());
        assertTrue(tool.getDescription().contains("Gitee user search tool"));
    }

    @Test
    void testSearchUsersSuccess() throws GiteeException {
        // Mock user data
        User user1 = new User();
        user1.setId(1);
        user1.setLogin("testuser1");
        user1.setName("Test User 1");
        user1.setHtmlUrl("https://gitee.com/testuser1");
        
        User user2 = new User();
        user2.setId(2);
        user2.setLogin("testuser2");
        user2.setName("Test User 2");
        user2.setHtmlUrl("https://gitee.com/testuser2");

        List<User> mockUsers = Arrays.asList(user1, user2);

        when(mockGiteeClient.searchUsers(any(SearchRequest.class))).thenReturn(mockUsers);

        String input = "{\"query\":\"test\",\"page\":1,\"perPage\":20}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().contains("Found 2 related users"));
        assertTrue(result.getOutput().contains("testuser1"));
        assertTrue(result.getOutput().contains("testuser2"));
        
        verify(mockGiteeClient, times(1)).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testSearchUsersNoResults() throws GiteeException {
        when(mockGiteeClient.searchUsers(any(SearchRequest.class))).thenReturn(Collections.emptyList());

        String input = "{\"query\":\"nonexistent\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertEquals("No related users found", result.getOutput());
        
        verify(mockGiteeClient, times(1)).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testSearchUsersEmptyQuery() throws GiteeException {
        String input = "{\"query\":\"\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Error: Search keywords cannot be empty"));
        
        verify(mockGiteeClient, never()).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testSearchUsersMissingQuery() throws GiteeException {
        String input = "{\"page\":1,\"perPage\":20}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Error: Search keywords cannot be empty"));
        
        verify(mockGiteeClient, never()).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testSearchUsersWithSorting() throws GiteeException {
        User user = new User();
        user.setId(1);
        user.setLogin("testuser");
        user.setName("Test User");

        when(mockGiteeClient.searchUsers(any(SearchRequest.class))).thenReturn(Arrays.asList(user));

        String input = "{\"query\":\"test\",\"sort\":\"joined_at\",\"order\":\"desc\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Sort: joined_at desc"));
        
        verify(mockGiteeClient, times(1)).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testSearchUsersApiException() throws GiteeException {
        GiteeException exception = new GiteeException("API Error", 401, "Unauthorized");
        when(mockGiteeClient.searchUsers(any(SearchRequest.class))).thenThrow(exception);

        String input = "{\"query\":\"test\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Gitee user search failed"));
        assertTrue(result.getOutput().contains("Authentication failed"));
        
        verify(mockGiteeClient, times(1)).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testSearchUsersGenericException() throws GiteeException {
        when(mockGiteeClient.searchUsers(any(SearchRequest.class))).thenThrow(new RuntimeException("Generic error"));

        String input = "{\"query\":\"test\"}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Search failed: Generic error"));
        
        verify(mockGiteeClient, times(1)).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testInvalidJsonInput() throws GiteeException {
        String input = "invalid json";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Search failed"));
        
        verify(mockGiteeClient, never()).searchUsers(any(SearchRequest.class));
    }

    @Test
    void testPageParameterValidation() throws GiteeException {
        User user = new User();
        user.setId(1);
        user.setLogin("testuser");

        when(mockGiteeClient.searchUsers(any(SearchRequest.class))).thenReturn(Arrays.asList(user));

        // Test with invalid page numbers
        String input = "{\"query\":\"test\",\"page\":-1,\"perPage\":200}";
        ToolExecuteResult result = tool.run(input);

        assertNotNull(result);
        assertTrue(result.getOutput().contains("Page: 1"));
        assertTrue(result.getOutput().contains("Per Page: 20"));
        
        verify(mockGiteeClient, times(1)).searchUsers(any(SearchRequest.class));
    }
}