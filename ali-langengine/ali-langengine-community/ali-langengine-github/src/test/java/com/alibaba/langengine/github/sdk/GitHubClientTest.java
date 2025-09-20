/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.github.sdk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;


class GitHubClientTest {

    private GitHubClient client;

    @BeforeEach
    void setUp() {
        client = new GitHubClient();
    }

    @Test
    void testGitHubClientConstruction() {
        // 测试默认构造函数
        assertDoesNotThrow(() -> new GitHubClient());
        
        // 测试带API token的构造函数
        assertDoesNotThrow(() -> new GitHubClient("test-api-token"));
    }

    @Test
    void testSearchRequestConstruction() {
        SearchRequest request = new SearchRequest();
        assertNotNull(request);
        
        SearchRequest requestWithQuery = new SearchRequest("test query");
        assertEquals("test query", requestWithQuery.getQuery());
        
        SearchRequest requestWithType = new SearchRequest("test query", "repositories");
        assertEquals("test query", requestWithType.getQuery());
        assertEquals("repositories", requestWithType.getType());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GITHUB_API_TOKEN", matches = ".*")
    void testSearchRepositories() {
        assertDoesNotThrow(() -> {
            SearchResponse response = client.searchRepositories("language:java");
            assertNotNull(response);
            assertNotNull(response.getTotalCount());
            assertTrue(response.getTotalCount() >= 0);
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GITHUB_API_TOKEN", matches = ".*")
    void testSearchRepositoriesWithCount() {
        assertDoesNotThrow(() -> {
            SearchResponse response = client.searchRepositories("language:java", 5);
            assertNotNull(response);
            assertNotNull(response.getItems());
            assertTrue(response.getItems().size() <= 5);
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GITHUB_API_TOKEN", matches = ".*")
    void testSearchCode() {
        assertDoesNotThrow(() -> {
            SearchResponse response = client.searchCode("class extension:java");
            assertNotNull(response);
            assertNotNull(response.getTotalCount());
            assertTrue(response.getTotalCount() >= 0);
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GITHUB_API_TOKEN", matches = ".*")
    void testSearchWithRequestObject() {
        SearchRequest request = new SearchRequest();
        request.setQuery("language:java");
        request.setPerPage(3);
        request.setSort("stars");
        request.setOrder("desc");
        
        assertDoesNotThrow(() -> {
            SearchResponse response = client.searchRepositories(request);
            assertNotNull(response);
            assertNotNull(response.getItems());
            assertTrue(response.getItems().size() <= 3);
        });
    }

    @Test
    void testSearchRequestValidation() {
        SearchRequest request = new SearchRequest();
        request.setQuery("test");
        request.setPerPage(50);
        request.setPage(1);
        request.setSort("stars");
        request.setOrder("desc");
        request.setType("repositories");
        
        assertEquals("test", request.getQuery());
        assertEquals(Integer.valueOf(50), request.getPerPage());
        assertEquals(Integer.valueOf(1), request.getPage());
        assertEquals("stars", request.getSort());
        assertEquals("desc", request.getOrder());
        assertEquals("repositories", request.getType());
    }

    @Test
    void testInvalidTokenHandling() {
        GitHubClient invalidClient = new GitHubClient("invalid-token");
        
        // 测试无效token时的异常处理
        GitHubException exception = assertThrows(GitHubException.class, () -> {
            invalidClient.searchRepositories("test");
        });
        
        // 验证异常包含有意义的错误信息
        assertTrue(exception.getMessage().contains("Error occurred during GitHub API call") ||
                  exception.getMessage().contains("Bad credentials") ||
                  exception.getMessage().contains("Unauthorized"));
    }

    @Test
    void testEmptyQueryHandling() {
        // 测试空查询的处理
        GitHubException exception = assertThrows(GitHubException.class, () -> {
            client.searchRepositories("");
        });
        
        // 验证异常信息合理
        assertNotNull(exception.getMessage());
    }

    @Test
    void testNullQueryHandling() {
        // 测试null查询的处理
        assertThrows(GitHubException.class, () -> {
            client.searchRepositories((String) null);
        });
    }

    @Test
    void testInvalidSearchRequestHandling() {
        // 测试无效的搜索请求
        SearchRequest invalidRequest = new SearchRequest();
        // 不设置query，应该抛出异常
        
        assertThrows(GitHubException.class, () -> {
            client.searchRepositories(invalidRequest);
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GITHUB_API_TOKEN", matches = ".*")
    void testSearchUsers() {
        assertDoesNotThrow(() -> {
            SearchRequest request = new SearchRequest("location:china");
            SearchResponse response = client.searchUsers(request);
            assertNotNull(response);
            assertNotNull(response.getTotalCount());
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GITHUB_API_TOKEN", matches = ".*")
    void testSearchIssues() {
        assertDoesNotThrow(() -> {
            SearchRequest request = new SearchRequest("state:open label:bug");
            SearchResponse response = client.searchIssues(request);
            assertNotNull(response);
            assertNotNull(response.getTotalCount());
        });
    }

    @Test
    void testRateLimitHandling() {
        GitHubClient rateLimitClient = new GitHubClient("valid-but-limited-token");
        
        // 这里我们只是测试客户端能够正确构造，实际的频率限制测试需要真实的环境
        assertNotNull(rateLimitClient);
    }

    @Test
    void testSearchResponseParsing() {
        // 测试搜索响应对象的基本功能
        SearchResponse response = new SearchResponse();
        response.setTotalCount(100);
        response.setIncompleteResults(false);
        
        assertEquals(Integer.valueOf(100), response.getTotalCount());
        assertEquals(Boolean.FALSE, response.getIncompleteResults());
    }

    @Test
    void testSearchResultBasicFields() {
        // 测试搜索结果对象的基本字段
        SearchResult result = new SearchResult();
        result.setId(12345L);
        result.setName("test-repo");
        result.setFullName("user/test-repo");
        result.setDescription("A test repository");
        result.setStargazersCount(100);
        
        assertEquals(Long.valueOf(12345L), result.getId());
        assertEquals("test-repo", result.getName());
        assertEquals("user/test-repo", result.getFullName());
        assertEquals("A test repository", result.getDescription());
        assertEquals(Integer.valueOf(100), result.getStargazersCount());
    }
}
