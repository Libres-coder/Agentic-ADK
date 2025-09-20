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
package com.alibaba.langengine.reddit.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.reddit.RedditConfiguration;
import com.alibaba.langengine.reddit.sdk.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class RedditVectorStoreTest {

    @Mock
    private RedditClient mockRedditClient;

    @Mock
    private Embeddings mockEmbedding;

    private RedditConfiguration configuration;
    private RedditVectorStore vectorStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        configuration = new RedditConfiguration();
        vectorStore = new RedditVectorStore(mockRedditClient, mockEmbedding);
    }

    @Test
    void testVectorStoreInitialization() {
        assertNotNull(vectorStore);
        assertEquals(0, vectorStore.getDocumentCount());
        assertEquals(25, vectorStore.getMaxResults());
        assertEquals("hot", vectorStore.getDefaultSort());
        assertEquals("day", vectorStore.getDefaultTimeRange());
        assertFalse(vectorStore.isIncludeNsfw());
    }

    @Test
    void testVectorStoreInitializationWithConfiguration() {
        RedditVectorStore configStore = new RedditVectorStore(configuration, mockEmbedding);
        assertNotNull(configStore);
        assertEquals(0, configStore.getDocumentCount());
    }

    @Test
    void testAddDocuments() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 模拟embedding行为
        when(mockEmbedding.embedDocument(any())).thenReturn(documents);

        // 执行测试
        vectorStore.addDocuments(documents);

        // 验证结果
        assertEquals(2, vectorStore.getDocumentCount());
        verify(mockEmbedding, times(1)).embedDocument(documents);
    }

    @Test
    void testAddEmptyDocuments() {
        List<Document> emptyDocs = new ArrayList<>();
        
        vectorStore.addDocuments(emptyDocs);
        
        assertEquals(0, vectorStore.getDocumentCount());
        verify(mockEmbedding, never()).embedDocument(any());
    }

    @Test
    void testAddNullDocuments() {
        vectorStore.addDocuments(null);
        
        assertEquals(0, vectorStore.getDocumentCount());
        verify(mockEmbedding, never()).embedDocument(any());
    }

    @Test
    void testSimilaritySearch() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        when(mockEmbedding.embedDocument(any())).thenReturn(documents);
        when(mockEmbedding.embedQuery(anyString(), anyInt())).thenReturn(Arrays.asList("[0.1, 0.2, 0.3]"));

        // 添加文档
        vectorStore.addDocuments(documents);

        // 执行相似性搜索
        List<Document> results = vectorStore.similaritySearch("test query", 2);

        // 验证结果
        assertNotNull(results);
        assertTrue(results.size() <= 2);
        verify(mockEmbedding, times(1)).embedQuery("test query", 2);
    }

    @Test
    void testSimilaritySearchWithEmptyQuery() {
        List<Document> results = vectorStore.similaritySearch("", 5);
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSimilaritySearchWithNullQuery() {
        List<Document> results = vectorStore.similaritySearch(null, 5);
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSimilaritySearchWithNoDocuments() {
        List<Document> results = vectorStore.similaritySearch("test query", 5);
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testDeleteDocuments() {
        // 先添加一些文档
        List<Document> documents = createTestDocuments();
        when(mockEmbedding.embedDocument(any())).thenReturn(documents);
        vectorStore.addDocuments(documents);
        
        assertEquals(2, vectorStore.getDocumentCount());
        
        // 删除所有文档
        vectorStore.delete();
        
        assertEquals(0, vectorStore.getDocumentCount());
    }

    @Test
    void testSearchAndAddDocuments() throws RedditException {
        // 准备模拟数据
        RedditSearchResponse mockResponse = createMockSearchResponse();
        when(mockRedditClient.search(any())).thenReturn(mockResponse);
        when(mockEmbedding.embedDocument(any())).thenReturn(createTestDocuments());

        // 执行搜索
        vectorStore.searchAndAddDocuments("test query");

        // 验证
        verify(mockRedditClient, times(1)).search(any());
        assertTrue(vectorStore.getDocumentCount() > 0);
    }

    @Test
    void testSearchAndAddDocumentsWithSubreddit() throws RedditException {
        // 准备模拟数据
        RedditSearchResponse mockResponse = createMockSearchResponse();
        when(mockRedditClient.search(any())).thenReturn(mockResponse);
        when(mockEmbedding.embedDocument(any())).thenReturn(createTestDocuments());

        // 执行搜索
        vectorStore.searchAndAddDocuments("java", "programming");

        // 验证
        verify(mockRedditClient, times(1)).search(any());
    }

    @Test
    void testSearchAndAddDocumentsWithException() throws RedditException {
        // 模拟异常
        when(mockRedditClient.search(any())).thenThrow(new RedditException("API Error"));

        // 验证异常被抛出
        assertThrows(RedditException.class, () -> {
            vectorStore.searchAndAddDocuments("test query");
        });
    }

    @Test
    void testSearchAndAddDocumentsWithEmptyResponse() throws RedditException {
        // 准备空响应
        RedditSearchResponse emptyResponse = new RedditSearchResponse();
        emptyResponse.setData(new RedditSearchResponse.DataWrapper());
        emptyResponse.getData().setChildren(new ArrayList<>());
        
        when(mockRedditClient.search(any())).thenReturn(emptyResponse);

        // 执行搜索
        vectorStore.searchAndAddDocuments("test query");

        // 验证没有添加文档
        assertEquals(0, vectorStore.getDocumentCount());
    }

    @Test
    void testVectorStoreConfiguration() {
        // 测试各种配置设置
        vectorStore.setMaxResults(50);
        vectorStore.setDefaultSubreddit("test");
        vectorStore.setDefaultSort("new");
        vectorStore.setDefaultTimeRange("week");
        vectorStore.setIncludeNsfw(true);

        assertEquals(50, vectorStore.getMaxResults());
        assertEquals("test", vectorStore.getDefaultSubreddit());
        assertEquals("new", vectorStore.getDefaultSort());
        assertEquals("week", vectorStore.getDefaultTimeRange());
        assertTrue(vectorStore.isIncludeNsfw());
    }

    @Test
    void testClose() {
        assertDoesNotThrow(() -> {
            vectorStore.close();
        });
        
        verify(mockRedditClient, times(1)).close();
    }

    @Test
    void testDocumentConversion() {
        // 创建RedditPost
        RedditPost post = createTestRedditPost();
        
        // 测试内容构建（通过添加文档间接测试）
        List<Document> docs = Arrays.asList(new Document("test content", new HashMap<>()));
        when(mockEmbedding.embedDocument(any())).thenReturn(docs);
        
        vectorStore.addDocuments(docs);
        
        assertEquals(1, vectorStore.getDocumentCount());
    }

    @Test
    void testRedditDocumentClass() {
        // 测试RedditDocument内部类
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("test", "value");
        
        RedditPost post = createTestRedditPost();
        
        RedditVectorStore.RedditDocument redditDoc = new RedditVectorStore.RedditDocument(
                "test-id", "test content", metadata, post);
        
        assertEquals("test-id", redditDoc.getId());
        assertEquals("test content", redditDoc.getContent());
        assertEquals(metadata, redditDoc.getMetadata());
        assertEquals(post, redditDoc.getOriginalPost());
    }

    @Test
    void testEmbeddingHandling() {
        // 测试embedding处理错误情况
        when(mockEmbedding.embedQuery(anyString(), anyInt())).thenReturn(Arrays.asList("invalid_format"));
        
        List<Document> results = vectorStore.similaritySearch("test", 5);
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testMultipleSearchAndAdd() throws RedditException {
        // 准备模拟数据
        RedditSearchResponse mockResponse1 = createMockSearchResponse();
        RedditSearchResponse mockResponse2 = createMockSearchResponse();
        when(mockRedditClient.search(any())).thenReturn(mockResponse1, mockResponse2);
        when(mockEmbedding.embedDocument(any())).thenReturn(createTestDocuments());

        // 执行多次搜索
        vectorStore.searchAndAddDocuments("query1");
        vectorStore.searchAndAddDocuments("query2");

        // 验证
        verify(mockRedditClient, times(2)).search(any());
        assertTrue(vectorStore.getDocumentCount() > 0);
    }

    // 辅助方法：创建测试文档
    private List<Document> createTestDocuments() {
        List<Document> docs = new ArrayList<>();
        
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("id", "doc1");
        metadata1.put("title", "Test Document 1");
        Document doc1 = new Document("Content of document 1", metadata1);
        doc1.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        docs.add(doc1);
        
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("id", "doc2");
        metadata2.put("title", "Test Document 2");
        Document doc2 = new Document("Content of document 2", metadata2);
        doc2.setEmbedding(Arrays.asList(0.4, 0.5, 0.6));
        docs.add(doc2);
        
        return docs;
    }

    // 辅助方法：创建模拟搜索响应
    private RedditSearchResponse createMockSearchResponse() {
        RedditSearchResponse response = new RedditSearchResponse();
        RedditSearchResponse.DataWrapper data = new RedditSearchResponse.DataWrapper();
        
        List<RedditSearchResponse.PostWrapper> children = new ArrayList<>();
        
        RedditSearchResponse.PostWrapper wrapper1 = new RedditSearchResponse.PostWrapper();
        wrapper1.setKind("t3");
        wrapper1.setData(createTestRedditPost());
        children.add(wrapper1);
        
        RedditSearchResponse.PostWrapper wrapper2 = new RedditSearchResponse.PostWrapper();
        wrapper2.setKind("t3");
        wrapper2.setData(createTestRedditPost2());
        children.add(wrapper2);
        
        data.setChildren(children);
        data.setAfter("t3_12345");
        response.setData(data);
        
        return response;
    }

    // 辅助方法：创建测试Reddit帖子
    private RedditPost createTestRedditPost() {
        RedditPost post = new RedditPost();
        post.setId("test_post_1");
        post.setTitle("Test Reddit Post 1");
        post.setContent("This is test content for Reddit post 1");
        post.setAuthor("test_author_1");
        post.setSubreddit("test");
        post.setSubredditPrefixed("r/test");
        post.setCreatedUtc(System.currentTimeMillis() / 1000);
        post.setScore(100);
        post.setUpvoteRatio(0.95);
        post.setNumComments(25);
        post.setUrl("https://reddit.com/r/test/comments/123/test");
        post.setPermalink("/r/test/comments/123/test");
        post.setStickied(false);
        post.setOver18(false);
        post.setLinkFlairText("Discussion");
        post.setDomain("self.test");
        post.setRemoved(false);
        return post;
    }

    // 辅助方法：创建第二个测试Reddit帖子
    private RedditPost createTestRedditPost2() {
        RedditPost post = new RedditPost();
        post.setId("test_post_2");
        post.setTitle("Test Reddit Post 2");
        post.setContent("This is test content for Reddit post 2");
        post.setAuthor("test_author_2");
        post.setSubreddit("programming");
        post.setSubredditPrefixed("r/programming");
        post.setCreatedUtc(System.currentTimeMillis() / 1000);
        post.setScore(250);
        post.setUpvoteRatio(0.88);
        post.setNumComments(45);
        post.setUrl("https://example.com/article");
        post.setPermalink("/r/programming/comments/456/article");
        post.setStickied(false);
        post.setOver18(false);
        post.setLinkFlairText("News");
        post.setDomain("example.com");
        post.setRemoved(false);
        return post;
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
            // 返回JSON数组格式的测试向量，符合parseEmbeddingFromQuery的期望
            List<String> results = new ArrayList<>();
            for (int i = 0; i < recommend; i++) {
                // 生成10维测试向量的JSON表示
                StringBuilder jsonVector = new StringBuilder("[");
                for (int j = 0; j < 10; j++) {
                    if (j > 0) jsonVector.append(", ");
                    jsonVector.append(String.format("%.6f", Math.random()));
                }
                jsonVector.append("]");
                results.add(jsonVector.toString());
            }
            return results;
        }
    }
}
