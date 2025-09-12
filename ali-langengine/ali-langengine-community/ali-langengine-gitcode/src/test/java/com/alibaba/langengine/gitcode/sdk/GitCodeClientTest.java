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

package com.alibaba.langengine.gitcode.sdk;

import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GitCodeClientTest {

    @Mock
    private OkHttpClient mockClient;

    @Mock
    private Call mockCall;

    @Mock
    private Response mockResponse;

    @Mock
    private ResponseBody mockResponseBody;

    private GitCodeClient gitCodeClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gitCodeClient = new GitCodeClient("test-token", mockClient);
    }

    @Test
    void testSearchUsers_Success() throws Exception {
        String responseJson = "[{\"id\":\"123\",\"login\":\"testuser\",\"name\":\"Test User\",\"avatar_url\":\"https://example.com/avatar.png\",\"html_url\":\"https://gitcode.com/testuser\",\"created_at\":\"2023-01-01T00:00:00Z\"}]";
        
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.string()).thenReturn(responseJson);

        SearchRequest request = SearchRequest.forUsers("testuser");
        List<User> users = gitCodeClient.searchUsers(request);

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getLogin());
        assertEquals("Test User", users.get(0).getName());
        verify(mockClient, times(1)).newCall(any(Request.class));
    }

    @Test
    void testSearchUsers_SimpleQuery() throws Exception {
        String responseJson = "[{\"id\":\"123\",\"login\":\"testuser\",\"name\":\"Test User\",\"avatar_url\":\"https://example.com/avatar.png\",\"html_url\":\"https://gitcode.com/testuser\",\"created_at\":\"2023-01-01T00:00:00Z\"}]";
        
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.string()).thenReturn(responseJson);

        List<User> users = gitCodeClient.searchUsers("testuser");

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getLogin());
    }

    @Test
    void testSearchIssues_Success() throws Exception {
        String responseJson = "[{\"id\":1,\"number\":\"1\",\"title\":\"Test Issue\",\"body\":\"Test issue body\",\"state\":\"open\",\"html_url\":\"https://gitcode.com/repo/issues/1\",\"created_at\":\"2023-01-01T00:00:00Z\",\"updated_at\":\"2023-01-01T00:00:00Z\",\"comments\":0,\"priority\":0,\"parent_id\":0}]";
        
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.string()).thenReturn(responseJson);

        SearchRequest request = SearchRequest.forIssues("test").withState("open");
        List<Issue> issues = gitCodeClient.searchIssues(request);

        assertNotNull(issues);
        assertEquals(1, issues.size());
        assertEquals("Test Issue", issues.get(0).getTitle());
        assertEquals("open", issues.get(0).getState());
        verify(mockClient, times(1)).newCall(any(Request.class));
    }

    @Test
    void testSearchRepositories_Success() throws Exception {
        String responseJson = "[{\"id\":1,\"name\":\"test-repo\",\"full_name\":\"user/test-repo\",\"description\":\"Test repository\",\"html_url\":\"https://gitcode.com/user/test-repo\",\"created_at\":\"2023-01-01T00:00:00Z\",\"updated_at\":\"2023-01-01T00:00:00Z\",\"stargazers_count\":10,\"forks_count\":5,\"open_issues_count\":2,\"default_branch\":\"main\",\"fork\":0,\"private\":0,\"public\":1}]";
        
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.string()).thenReturn(responseJson);

        SearchRequest request = SearchRequest.forRepositories("test").withLanguage("java");
        List<Repository> repositories = gitCodeClient.searchRepositories(request);

        assertNotNull(repositories);
        assertEquals(1, repositories.size());
        assertEquals("test-repo", repositories.get(0).getName());
        assertEquals("user/test-repo", repositories.get(0).getFullName());
        verify(mockClient, times(1)).newCall(any(Request.class));
    }

    @Test
    void testSearchUsers_AuthenticationError() throws Exception {
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.code()).thenReturn(401);
        when(mockResponse.message()).thenReturn("Unauthorized");
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.string()).thenReturn("{\"error\":\"Invalid token\"}");

        SearchRequest request = SearchRequest.forUsers("testuser");
        
        GitCodeException exception = assertThrows(GitCodeException.class, () -> {
            gitCodeClient.searchUsers(request);
        });

        assertTrue(exception.isAuthenticationError());
        assertEquals(401, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("authentication failed"));
    }

    @Test
    void testSearchUsers_RateLimitError() throws Exception {
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.code()).thenReturn(429);
        when(mockResponse.message()).thenReturn("Too Many Requests");
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.string()).thenReturn("{\"error\":\"Rate limit exceeded\"}");

        SearchRequest request = SearchRequest.forUsers("testuser");
        
        GitCodeException exception = assertThrows(GitCodeException.class, () -> {
            gitCodeClient.searchUsers(request);
        });

        assertTrue(exception.isRateLimitError());
        assertEquals(429, exception.getStatusCode());
    }

    @Test
    void testSearchUsers_NetworkError() throws Exception {
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));

        SearchRequest request = SearchRequest.forUsers("testuser");
        
        GitCodeException exception = assertThrows(GitCodeException.class, () -> {
            gitCodeClient.searchUsers(request);
        });

        assertTrue(exception.getMessage().contains("Error occurred during GitCode API call"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void testSearchRequest_BuilderMethods() {
        SearchRequest request = SearchRequest.forUsers("testuser")
                .withSort("joined_at", "desc")
                .withPagination(1, 20);

        assertEquals("testuser", request.getQ());
        assertEquals("joined_at", request.getSort());
        assertEquals("desc", request.getOrder());
        assertEquals(Integer.valueOf(1), request.getPage());
        assertEquals(Integer.valueOf(20), request.getPerPage());
    }

    @Test
    void testSearchRequest_IssuesWithFilters() {
        SearchRequest request = SearchRequest.forIssues("bug")
                .withRepo("user/repo")
                .withState("open");

        assertEquals("bug", request.getQ());
        assertEquals("user/repo", request.getRepo());
        assertEquals("open", request.getState());
    }

    @Test
    void testSearchRequest_RepositoriesWithFilters() {
        SearchRequest request = SearchRequest.forRepositories("java")
                .withOwner("testuser")
                .withLanguage("java")
                .withFork("true");

        assertEquals("java", request.getQ());
        assertEquals("testuser", request.getOwner());
        assertEquals("java", request.getLanguage());
        assertEquals("true", request.getFork());
    }

    @Test
    void testDefaultConstructor() {
        GitCodeClient defaultClient = new GitCodeClient();
        assertNotNull(defaultClient);
    }

    @Test
    void testTokenConstructor() {
        GitCodeClient tokenClient = new GitCodeClient("custom-token");
        assertNotNull(tokenClient);
    }
}