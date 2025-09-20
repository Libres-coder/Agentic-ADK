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

package com.alibaba.langengine.gitee.sdk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GiteeClientTest {

    private GiteeClient giteeClient;

    @BeforeEach
    void setUp() {
        // Use a test token or mock token for testing
        giteeClient = new GiteeClient("test-token");
    }

    @Test
    void testConstructorWithToken() {
        GiteeClient client = new GiteeClient("test-token");
        assertNotNull(client);
    }

    @Test
    void testConstructorWithoutToken() {
        GiteeClient client = new GiteeClient();
        assertNotNull(client);
    }

    @Test
    void testSearchUsers() {
        SearchRequest request = SearchRequest.forUsers("test");

        // Test that the method can be called without throwing unexpected exceptions
        // Expected to throw GiteeException due to invalid/missing token, which is normal behavior
        assertThrows(GiteeException.class, () -> {
            giteeClient.searchUsers(request);
        });
    }

    @Test
    void testSearchUsersSimple() {
        // Test that the simplified method can be called
        // Expected to throw GiteeException due to invalid/missing token
        assertThrows(GiteeException.class, () -> {
            giteeClient.searchUsers("test");
        });
    }

    @Test
    void testSearchUsersWithLimit() {
        // Test that the method with limit parameter can be called
        // Expected to throw GiteeException due to invalid/missing token
        assertThrows(GiteeException.class, () -> {
            giteeClient.searchUsers("test", 10);
        });
    }

    @Test
    void testSearchIssues() {
        SearchRequest request = SearchRequest.forIssues("test");

        // Test that the method can be called without throwing unexpected exceptions
        // Expected to throw GiteeException due to invalid/missing token
        assertThrows(GiteeException.class, () -> {
            giteeClient.searchIssues(request);
        });
    }

    @Test
    void testSearchIssuesSimple() {
        // Test that the simplified method can be called
        // Expected to throw GiteeException due to invalid/missing token
        assertThrows(GiteeException.class, () -> {
            giteeClient.searchIssues("test");
        });
    }

    @Test
    void testSearchIssuesWithLimit() {
        // Test that the method with limit parameter can be called
        // Expected to throw GiteeException due to invalid/missing token
        assertThrows(GiteeException.class, () -> {
            giteeClient.searchIssues("test", 10);
        });
    }

    @Test
    void testSearchRepositories() {
        SearchRequest request = SearchRequest.forRepositories("test");

        // Test that the method can be called without throwing unexpected exceptions
        // Expected to throw GiteeException due to invalid/missing token
        assertThrows(GiteeException.class, () -> {
            giteeClient.searchRepositories(request);
        });
    }

    @Test
    void testSearchRepositoriesSimple() {
        // Test that the simplified method can be called
        // Expected to throw GiteeException due to invalid/missing token
        assertThrows(GiteeException.class, () -> {
            giteeClient.searchRepositories("test");
        });
    }

    @Test
    void testSearchRepositoriesWithLimit() {
        // Test that the method with limit parameter can be called
        // Expected to throw GiteeException due to invalid/missing token
        assertThrows(GiteeException.class, () -> {
            giteeClient.searchRepositories("test", 10);
        });
    }

    @Test
    void testSearchRequestBuilder() {
        SearchRequest request = SearchRequest.forUsers("test")
                .withSort("joined_at", "desc")
                .withPagination(1, 20);

        assertEquals("test", request.getQ());
        assertEquals("joined_at", request.getSort());
        assertEquals("desc", request.getOrder());
        assertEquals(Integer.valueOf(1), request.getPage());
        assertEquals(Integer.valueOf(20), request.getPerPage());
    }

    @Test
    void testRepositorySearchRequestBuilder() {
        SearchRequest request = SearchRequest.forRepositories("spring")
                .withOwner("alibaba")
                .withLanguage("java")
                .withFork(true)
                .withSort("stars_count", "desc")
                .withPagination(1, 10);

        assertEquals("spring", request.getQ());
        assertEquals("alibaba", request.getOwner());
        assertEquals("java", request.getLanguage());
        assertEquals(Boolean.TRUE, request.getFork());
        assertEquals("stars_count", request.getSort());
        assertEquals("desc", request.getOrder());
        assertEquals(Integer.valueOf(1), request.getPage());
        assertEquals(Integer.valueOf(10), request.getPerPage());
    }

    @Test
    void testIssueSearchRequestBuilder() {
        SearchRequest request = SearchRequest.forIssues("bug")
                .withRepo("alibaba/spring-boot")
                .withState("open")
                .withAuthor("test-user")
                .withAssignee("assignee-user")
                .withLabel("bug")
                .withLanguage("java")
                .withSort("created_at", "desc")
                .withPagination(1, 20);

        assertEquals("bug", request.getQ());
        assertEquals("alibaba/spring-boot", request.getRepo());
        assertEquals("open", request.getState());
        assertEquals("test-user", request.getAuthor());
        assertEquals("assignee-user", request.getAssignee());
        assertEquals("bug", request.getLabel());
        assertEquals("java", request.getLanguage());
        assertEquals("created_at", request.getSort());
        assertEquals("desc", request.getOrder());
        assertEquals(Integer.valueOf(1), request.getPage());
        assertEquals(Integer.valueOf(20), request.getPerPage());
    }
}