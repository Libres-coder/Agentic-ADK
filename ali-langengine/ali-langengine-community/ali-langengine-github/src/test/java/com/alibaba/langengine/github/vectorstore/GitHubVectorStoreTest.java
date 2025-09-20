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
package com.alibaba.langengine.github.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.github.sdk.GitHubClient;
import com.alibaba.langengine.github.sdk.GitHubException;
import com.alibaba.langengine.github.sdk.SearchRequest;
import com.alibaba.langengine.github.sdk.SearchResponse;
import com.alibaba.langengine.github.sdk.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class GitHubVectorStoreTest {

    private GitHubVectorStore vectorStore;
    private TestGitHubClient testClient;
    private TestEmbeddings testEmbeddings;

    @BeforeEach
    void setUp() {
        testClient = new TestGitHubClient();
        testEmbeddings = new TestEmbeddings();
        vectorStore = new GitHubVectorStore(testClient);
        vectorStore.setEmbedding(testEmbeddings);
    }

    @Test
    void testBasicFunctionality() {
        // 测试基本功能
        assertNotNull(vectorStore);
        assertEquals(0, vectorStore.getDocumentCount());
        
        // 测试设置搜索类型
        vectorStore.setSearchType("repositories");
        assertEquals("repositories", vectorStore.getSearchType());
    }

    @Test
    void testAddDocuments() {
        List<Document> documents = Arrays.asList(
            new Document("Test content 1", createMetadata("1", "repo1")),
            new Document("Test content 2", createMetadata("2", "repo2"))
        );
        
        vectorStore.addDocuments(documents);
        assertEquals(2, vectorStore.getDocumentCount());
    }

    @Test
    void testSimilaritySearch() {
        // 添加一些测试文档
        List<Document> documents = Arrays.asList(
            new Document("Java programming tutorial", createMetadata("1", "java-tutorial")),
            new Document("Python machine learning", createMetadata("2", "python-ml")),
            new Document("JavaScript web development", createMetadata("3", "js-web"))
        );
        
        vectorStore.addDocuments(documents);
        
        // 执行相似性搜索
        List<Document> results = vectorStore.similaritySearch("programming", 2);
        assertNotNull(results);
        assertTrue(results.size() <= 2);
    }

    @Test
    void testSearchAndAddDocuments() throws GitHubException {
        // 设置测试响应
        testClient.setTestResponse(createMockSearchResponse());
        
        vectorStore.searchAndAddDocuments("test query");
        assertEquals(2, vectorStore.getDocumentCount());
    }

    @Test
    void testClearDocuments() {
        // 先添加一些文档
        List<Document> documents = Arrays.asList(
            new Document("Test", createMetadata("1", "test"))
        );
        vectorStore.addDocuments(documents);
        assertEquals(1, vectorStore.getDocumentCount());
        
        // 清空文档
        vectorStore.clearDocuments();
        assertEquals(0, vectorStore.getDocumentCount());
    }

    @Test
    void testGetDocumentsByType() {
        List<Document> documents = Arrays.asList(
            new Document("Repo content", createMetadata("1", "repo1", "repositories")),
            new Document("Code content", createMetadata("2", "code1", "code"))
        );
        
        vectorStore.addDocuments(documents);
        
        List<Document> repoDocuments = vectorStore.getDocumentsByType("repositories");
        assertEquals(1, repoDocuments.size());
        assertEquals("repositories", repoDocuments.get(0).getMetadata().get("search_type"));
    }

    @Test
    void testErrorHandling() {
        testClient.setShouldThrowException(true);
        
        assertThrows(GitHubException.class, () -> {
            vectorStore.searchAndAddDocuments("test query");
        });
    }

    @Test
    void testEmptySearchResults() throws GitHubException {
        SearchResponse emptyResponse = new SearchResponse();
        emptyResponse.setTotalCount(0);
        emptyResponse.setItems(Collections.emptyList());
        
        testClient.setTestResponse(emptyResponse);
        
        vectorStore.searchAndAddDocuments("no results query");
        assertEquals(0, vectorStore.getDocumentCount());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GITHUB_API_TOKEN", matches = ".+")
    void testRealIntegration() throws GitHubException {
        // 集成测试，需要真实的GitHub Token
        String token = System.getenv("GITHUB_API_TOKEN");
        GitHubClient realClient = new GitHubClient(token);
        GitHubVectorStore realVectorStore = new GitHubVectorStore(realClient);
        realVectorStore.setEmbedding(testEmbeddings);
        
        realVectorStore.setSearchType("repositories");
        realVectorStore.setMaxResults(5);
        
        realVectorStore.searchAndAddDocuments("language:java stars:>1000");
        assertTrue(realVectorStore.getDocumentCount() > 0);
    }

    // 辅助方法
    private Map<String, Object> createMetadata(String id, String name) {
        return createMetadata(id, name, "repositories");
    }

    private Map<String, Object> createMetadata(String id, String name, String searchType) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", id);
        metadata.put("name", name);
        metadata.put("search_type", searchType);
        return metadata;
    }

    private SearchResponse createMockSearchResponse() {
        SearchResponse response = new SearchResponse();
        response.setTotalCount(2);
        response.setIncompleteResults(false);

        List<SearchResult> items = new ArrayList<>();
        
        SearchResult item1 = new SearchResult();
        item1.setId(1L);
        item1.setName("test-repo-1");
        item1.setFullName("user/test-repo-1");
        item1.setDescription("Test repository 1");
        items.add(item1);

        SearchResult item2 = new SearchResult();
        item2.setId(2L);
        item2.setName("test-repo-2");
        item2.setFullName("user/test-repo-2");
        item2.setDescription("Test repository 2");
        items.add(item2);

        response.setItems(items);
        return response;
    }

    // 测试用的GitHubClient实现
    private static class TestGitHubClient extends GitHubClient {
        private SearchResponse testResponse;
        private boolean shouldThrowException = false;

        public TestGitHubClient() {
            super("test-token");
        }

        public void setTestResponse(SearchResponse response) {
            this.testResponse = response;
        }

        public void setShouldThrowException(boolean shouldThrow) {
            this.shouldThrowException = shouldThrow;
        }

        @Override
        public SearchResponse searchRepositories(SearchRequest request) throws GitHubException {
            if (shouldThrowException) {
                throw new GitHubException("Test exception");
            }
            return testResponse != null ? testResponse : createMockSearchResponse();
        }

        @Override
        public SearchResponse searchCode(SearchRequest request) throws GitHubException {
            if (shouldThrowException) {
                throw new GitHubException("Test exception");
            }
            return testResponse != null ? testResponse : createMockSearchResponse();
        }

        @Override
        public SearchResponse searchUsers(SearchRequest request) throws GitHubException {
            if (shouldThrowException) {
                throw new GitHubException("Test exception");
            }
            return testResponse != null ? testResponse : createMockSearchResponse();
        }

        @Override
        public SearchResponse searchIssues(SearchRequest request) throws GitHubException {
            if (shouldThrowException) {
                throw new GitHubException("Test exception");
            }
            return testResponse != null ? testResponse : createMockSearchResponse();
        }

        private SearchResponse createMockSearchResponse() {
            SearchResponse response = new SearchResponse();
            response.setTotalCount(2);
            response.setIncompleteResults(false);
            
            List<SearchResult> items = new ArrayList<>();
            SearchResult item1 = new SearchResult();
            item1.setId(1L);
            item1.setName("test-repo");
            items.add(item1);
            
            response.setItems(items);
            return response;
        }
    }

    // 测试用的Embeddings实现
    private static class TestEmbeddings extends Embeddings {
        @Override
        public String getModelType() {
            return "test";
        }

        @Override
        public List<Document> embedDocument(List<Document> documents) {
            // 简单的测试实现，为每个文档添加测试向量
            for (Document doc : documents) {
                List<Double> vector = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    vector.add(Math.random());
                }
                doc.setEmbedding(vector);
            }
            return documents;
        }

        @Override
        public List<String> embedQuery(String text, int recommend) {
            // 返回简单的测试查询结果
            List<String> results = new ArrayList<>();
            for (int i = 0; i < recommend; i++) {
                results.add("test_result_" + i);
            }
            return results;
        }
    }
}
