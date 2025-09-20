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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.reddit.RedditConfiguration;
import com.alibaba.langengine.reddit.sdk.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class RedditVectorStore extends VectorStore {

    /**
     * 默认最大文档数（防止内存溢出）
     */
    public static final int DEFAULT_MAX_DOCUMENTS = 10000;

    /**
     * Reddit API客户端
     */
    private RedditClient redditClient;

    /**
     * 嵌入模型
     */
    private Embeddings embedding;

    /**
     * 内存存储的搜索结果文档
     */
    private List<RedditDocument> documents;

    /**
     * 最大文档存储数量（防止内存溢出）
     */
    private int maxDocuments = DEFAULT_MAX_DOCUMENTS;

    /**
     * 最大搜索结果数
     */
    private int maxResults = 25;

    /**
     * 默认子论坛
     */
    private String defaultSubreddit;

    /**
     * 默认排序方式
     */
    private String defaultSort = "hot";

    /**
     * 默认时间范围
     */
    private String defaultTimeRange = "day";

    /**
     * 是否包含NSFW内容
     */
    private boolean includeNsfw = false;

    /**
     * Reddit文档内部类
     */
    @Data
    public static class RedditDocument {
        private String id;
        private String content;
        private List<Double> embedding;
        private Map<String, Object> metadata;
        private Double score;
        private RedditPost originalPost;

        public RedditDocument(String id, String content, Map<String, Object> metadata, RedditPost originalPost) {
            this.id = id;
            this.content = content;
            this.metadata = metadata;
            this.originalPost = originalPost;
        }
    }

    /**
     * 构造函数
     *
     * @param configuration Reddit配置
     * @param embedding     嵌入模型
     */
    public RedditVectorStore(RedditConfiguration configuration, Embeddings embedding) {
        this.redditClient = new RedditClient(configuration);
        this.embedding = embedding;
        this.documents = new ArrayList<>();
        log.info("Reddit vector store initialized");
    }

    /**
     * 构造函数
     *
     * @param redditClient Reddit客户端
     * @param embedding    嵌入模型
     */
    public RedditVectorStore(RedditClient redditClient, Embeddings embedding) {
        this.redditClient = redditClient;
        this.embedding = embedding;
        this.documents = new ArrayList<>();
        log.info("Reddit vector store initialized with custom client");
    }

    /**
     * 搜索Reddit并添加结果文档到向量库
     *
     * @param query 搜索查询
     * @throws RedditException 搜索失败时抛出
     */
    public void searchAndAddDocuments(String query) throws RedditException {
        searchAndAddDocuments(query, defaultSubreddit);
    }

    /**
     * 搜索Reddit并添加结果文档到向量库
     *
     * @param query     搜索查询
     * @param subreddit 子论坛
     * @throws RedditException 搜索失败时抛出
     */
    public void searchAndAddDocuments(String query, String subreddit) throws RedditException {
        RedditSearchRequest request = new RedditSearchRequest();
        request.setQuery(query);
        request.setSubreddit(subreddit);
        request.setSort(defaultSort);
        request.setTimeRange(defaultTimeRange);
        request.setLimit(maxResults);
        request.setIncludeOver18(includeNsfw);

        RedditSearchResponse response = redditClient.search(request);

        if (response.getPosts() != null && !response.getPosts().isEmpty()) {
            List<Document> docs = convertRedditPostsToDocuments(response.getPosts());
            addDocuments(docs);
        }
    }

    /**
     * 将Reddit帖子转换为文档
     *
     * @param posts Reddit帖子列表
     * @return 文档列表
     */
    private List<Document> convertRedditPostsToDocuments(List<RedditPost> posts) {
        List<Document> docs = new ArrayList<>();

        for (RedditPost post : posts) {
            try {
                String content = buildPostContent(post);
                Map<String, Object> metadata = buildPostMetadata(post);

                Document doc = new Document(content, metadata);
                docs.add(doc);

                log.debug("Converted Reddit post {} to document", post.getId());
            } catch (Exception e) {
                log.warn("Failed to convert Reddit post {} to document: {}", 
                        post.getId(), e.getMessage());
            }
        }

        return docs;
    }

    /**
     * 构建帖子内容
     *
     * @param post Reddit帖子
     * @return 内容字符串
     */
    private String buildPostContent(RedditPost post) {
        StringBuilder content = new StringBuilder();

        // 添加标题
        if (StringUtils.isNotBlank(post.getTitle())) {
            content.append("Title: ").append(post.getTitle()).append("\n");
        }

        // 添加作者
        if (StringUtils.isNotBlank(post.getAuthor())) {
            content.append("Author: ").append(post.getAuthor()).append("\n");
        }

        // 添加子论坛
        if (StringUtils.isNotBlank(post.getSubreddit())) {
            content.append("Subreddit: r/").append(post.getSubreddit()).append("\n");
        }

        // 添加帖子内容
        if (StringUtils.isNotBlank(post.getContent())) {
            content.append("Content: ").append(post.getContent()).append("\n");
        }

        // 添加URL（如果不是自己的帖子）
        if (StringUtils.isNotBlank(post.getUrl()) && !post.getUrl().contains("reddit.com")) {
            content.append("URL: ").append(post.getUrl()).append("\n");
        }

        // 添加标签
        if (StringUtils.isNotBlank(post.getLinkFlairText())) {
            content.append("Flair: ").append(post.getLinkFlairText()).append("\n");
        }

        // 添加统计信息
        content.append("Score: ").append(post.getScore() != null ? post.getScore() : 0).append(", ");
        content.append("Comments: ").append(post.getNumComments() != null ? post.getNumComments() : 0);

        return content.toString();
    }

    /**
     * 构建帖子元数据
     *
     * @param post Reddit帖子
     * @return 元数据Map
     */
    private Map<String, Object> buildPostMetadata(RedditPost post) {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("id", post.getId());
        metadata.put("title", post.getTitle());
        metadata.put("author", post.getAuthor());
        metadata.put("subreddit", post.getSubreddit());
        metadata.put("subreddit_prefixed", post.getSubredditPrefixed());
        metadata.put("created_utc", post.getCreatedUtc());
        metadata.put("score", post.getScore());
        metadata.put("upvote_ratio", post.getUpvoteRatio());
        metadata.put("num_comments", post.getNumComments());
        metadata.put("url", post.getUrl());
        metadata.put("permalink", "https://reddit.com" + post.getPermalink());
        metadata.put("stickied", post.getStickied());
        metadata.put("over_18", post.getOver18());
        metadata.put("post_hint", post.getPostHint());
        metadata.put("link_flair_text", post.getLinkFlairText());
        metadata.put("domain", post.getDomain());
        metadata.put("removed", post.getRemoved());

        // 添加时间格式化（使用RedditPost的便捷方法）
        if (post.getCreatedUtc() != null) {
            metadata.put("created_date", post.getCreatedDate());
            metadata.put("created_instant", post.getCreatedInstant().toString());
            metadata.put("created_datetime", post.getCreatedDateTime().toString());
        }

        // 保存原始帖子数据
        try {
            ObjectMapper mapper = new ObjectMapper();
            metadata.put("original_post", mapper.writeValueAsString(post));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize original post for {}: {}", post.getId(), e.getMessage());
        }

        return metadata;
    }

    /**
     * 相似性搜索
     *
     * @param query 查询文本
     * @param k     返回结果数
     * @return 相似文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int k) {
        return similaritySearch(query, k, null, null);
    }

    /**
     * 相似性搜索（四参数版本）
     *
     * @param query           查询字符串
     * @param k               返回的结果数量
     * @param maxDistanceValue 最大距离值
     * @param type            类型参数
     * @return 相似的文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isBlank(query)) {
            log.warn("Empty query provided for similarity search");
            return new ArrayList<>();
        }

        if (documents.isEmpty()) {
            log.warn("No documents available for similarity search");
            return new ArrayList<>();
        }

        try {
            // 获取查询向量
            List<String> queryResults = embedding.embedQuery(query, k);
            List<Double> queryVector = parseEmbeddingFromQuery(queryResults);

            if (queryVector == null || queryVector.isEmpty()) {
                log.warn("Failed to get query embedding for: {}", query);
                return new ArrayList<>();
            }

            // 计算相似度并排序
            List<RedditDocument> validDocs = documents.stream()
                    .filter(doc -> doc.getEmbedding() != null && !doc.getEmbedding().isEmpty())
                    .collect(Collectors.toList());

            List<RedditDocument> candidates = new ArrayList<>();
            for (RedditDocument doc : validDocs) {
                double similarity = calculateCosineSimilarity(queryVector, doc.getEmbedding());
                
                // 应用距离阈值过滤
                if (maxDistanceValue == null || (1.0 - similarity) <= maxDistanceValue) {
                    doc.setScore(similarity);
                    candidates.add(doc);
                }
            }

            candidates.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

            // 转换为Document列表
            return candidates.stream()
                    .limit(k)
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error during similarity search", e);
            return new ArrayList<>();
        }
    }

    /**
     * 从查询结果解析嵌入向量
     *
     * @param queryResults 查询结果
     * @return 嵌入向量
     * @throws IllegalArgumentException 当嵌入格式无效时抛出
     */
    private List<Double> parseEmbeddingFromQuery(List<String> queryResults) {
        if (queryResults == null || queryResults.isEmpty()) {
            log.error("Embedding query results are null or empty");
            throw new IllegalArgumentException("Embedding service returned empty results");
        }

        try {
            // 尝试解析第一个结果为JSON数组
            String firstResult = queryResults.get(0);
            if (firstResult.startsWith("[") && firstResult.endsWith("]")) {
                return JSON.parseArray(firstResult, Double.class);
            }

            // 如果不是JSON格式，抛出异常而不是生成随机向量
            log.error("Invalid embedding format from service. Expected JSON array but got: {}", firstResult);
            throw new IllegalArgumentException("Embedding service returned invalid format: " + firstResult);
            
        } catch (Exception e) {
            log.error("Failed to parse embedding from query results: {}", queryResults, e);
            throw new IllegalArgumentException("Failed to parse embedding vector", e);
        }
    }

    /**
     * 计算余弦相似度
     *
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 余弦相似度
     */
    private double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1.size() != vector2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += Math.pow(vector1.get(i), 2);
            norm2 += Math.pow(vector2.get(i), 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 转换RedditDocument为Document
     *
     * @param redditDoc Reddit文档
     * @return Document对象
     */
    private Document convertToDocument(RedditDocument redditDoc) {
        Document doc = new Document(redditDoc.getContent(), redditDoc.getMetadata());
        doc.setUniqueId(redditDoc.getId());
        doc.setScore(redditDoc.getScore());
        doc.setEmbedding(redditDoc.getEmbedding());
        return doc;
    }

    /**
     * 添加文档
     *
     * @param documents 文档列表
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("No documents to add");
            return;
        }

        try {
            // 检查内存限制
            if (this.documents.size() + documents.size() > maxDocuments) {
                int toRemove = this.documents.size() + documents.size() - maxDocuments;
                log.warn("Document limit ({}) would be exceeded. Removing {} oldest documents", maxDocuments, toRemove);
                
                // 移除最早的文档（FIFO策略）
                for (int i = 0; i < toRemove && !this.documents.isEmpty(); i++) {
                    this.documents.remove(0);
                }
            }

            // 生成嵌入向量
            List<Document> embeddedDocs = embedding.embedDocument(documents);

            for (Document doc : embeddedDocs) {
                try {
                    RedditDocument redditDoc = new RedditDocument(
                            doc.getUniqueId(),
                            doc.getPageContent(),
                            doc.getMetadata(),
                            getOriginalPostFromMetadata(doc.getMetadata())
                    );

                    redditDoc.setEmbedding(doc.getEmbedding());

                    if (doc.getScore() != null) {
                        redditDoc.setScore(doc.getScore());
                    }

                    this.documents.add(redditDoc);
                } catch (Exception e) {
                    log.warn("Failed to convert document {}: {}", doc.getUniqueId(), e.getMessage());
                }
            }

            log.info("Added {} documents to Reddit vector store (total: {})", embeddedDocs.size(), this.documents.size());
        } catch (Exception e) {
            log.error("Error adding documents to Reddit vector store", e);
            throw new RuntimeException("Failed to add documents", e);
        }
    }

    /**
     * 从元数据获取原始帖子
     *
     * @param metadata 元数据
     * @return Reddit帖子
     */
    private RedditPost getOriginalPostFromMetadata(Map<String, Object> metadata) {
        try {
            String originalPostJson = (String) metadata.get("original_post");
            if (StringUtils.isNotBlank(originalPostJson)) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(originalPostJson, RedditPost.class);
            }
        } catch (Exception e) {
            log.debug("Failed to deserialize original post from metadata: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 删除所有文档
     */
    public void delete() {
        documents.clear();
        log.info("Cleared all documents from Reddit vector store");
    }

    /**
     * 获取文档数量
     *
     * @return 文档数量
     */
    public int getDocumentCount() {
        return documents.size();
    }

    /**
     * 关闭向量存储
     */
    public void close() {
        if (redditClient != null) {
            redditClient.close();
        }
        log.info("Reddit vector store closed");
    }
}
