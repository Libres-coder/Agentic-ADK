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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.github.sdk.*;
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
public class GitHubVectorStore extends VectorStore {

    /**
     * GitHub API客户端
     */
    private GitHubClient gitHubClient;

    /**
     * 嵌入模型
     */
    private Embeddings embedding;

    /**
     * 内存存储的搜索结果文档
     */
    private List<GitHubDocument> documents;

    /**
     * 搜索类型：repositories, code, users, issues
     */
    private String searchType = "repositories";

    /**
     * 每次搜索的最大结果数
     */
    private int maxResults = 30;

    /**
     * 构造函数
     */
    public GitHubVectorStore() {
        this.gitHubClient = new GitHubClient();
        this.documents = new ArrayList<>();
    }

    /**
     * 构造函数
     *
     * @param gitHubClient GitHub客户端
     */
    public GitHubVectorStore(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
        this.documents = new ArrayList<>();
    }

    /**
     * 构造函数
     *
     * @param apiToken GitHub API Token
     */
    public GitHubVectorStore(String apiToken) {
        this.gitHubClient = new GitHubClient(apiToken);
        this.documents = new ArrayList<>();
    }

    /**
     * GitHub文档内部类
     */
    @Data
    public static class GitHubDocument {
        private String id;
        private String content;
        private List<Double> embedding;
        private Map<String, Object> metadata;
        private Double score;
        private SearchResult originalResult;

        public GitHubDocument(String id, String content, Map<String, Object> metadata, SearchResult originalResult) {
            this.id = id;
            this.content = content;
            this.metadata = metadata;
            this.originalResult = originalResult;
        }
    }

    /**
     * 搜索GitHub并添加结果文档到向量库
     *
     * @param query 搜索查询
     * @throws GitHubException 搜索失败时抛出
     */
    public void searchAndAddDocuments(String query) throws GitHubException {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setPerPage(maxResults);

        SearchResponse response;
        switch (searchType.toLowerCase()) {
            case "repositories":
                response = gitHubClient.searchRepositories(request);
                break;
            case "code":
                response = gitHubClient.searchCode(request);
                break;
            case "users":
                response = gitHubClient.searchUsers(request);
                break;
            case "issues":
                response = gitHubClient.searchIssues(request);
                break;
            default:
                throw new GitHubException("Unsupported search type: " + searchType);
        }

        if (response.getItems() != null && !response.getItems().isEmpty()) {
            List<Document> docs = convertSearchResultsToDocuments(response.getItems());
            addDocuments(docs);
        }
    }

    /**
     * 将GitHub搜索结果转换为文档
     *
     * @param searchResults 搜索结果列表
     * @return 文档列表
     */
    private List<Document> convertSearchResultsToDocuments(List<SearchResult> searchResults) {
        List<Document> docs = new ArrayList<>();

        for (SearchResult result : searchResults) {
            Document doc = new Document();
            
            // 设置唯一ID
            doc.setUniqueId(generateDocumentId(result));
            
            // 设置页面内容
            doc.setPageContent(generateDocumentContent(result));
            
            // 设置元数据并保存原始结果
            Map<String, Object> metadata = generateMetadata(result);
            // 将原始SearchResult保存到元数据中，供后续使用
            metadata.put("original_result", result);
            doc.setMetadata(metadata);
            
            // 设置评分
            if (result.getScore() != null) {
                doc.setScore(result.getScore());
            }

            docs.add(doc);
        }

        return docs;
    }

    /**
     * 生成文档ID
     *
     * @param result 搜索结果
     * @return 文档ID
     */
    private String generateDocumentId(SearchResult result) {
        if (result.getId() != null) {
            return searchType + "_" + result.getId();
        }
        return searchType + "_" + UUID.randomUUID().toString();
    }

    /**
     * 生成文档内容
     *
     * @param result 搜索结果
     * @return 文档内容
     */
    private String generateDocumentContent(SearchResult result) {
        StringBuilder content = new StringBuilder();

        switch (searchType.toLowerCase()) {
            case "repositories":
                content.append("Repository: ").append(result.getFullName() != null ? result.getFullName() : result.getName()).append("\n");
                if (StringUtils.isNotBlank(result.getDescription())) {
                    content.append("Description: ").append(result.getDescription()).append("\n");
                }
                if (StringUtils.isNotBlank(result.getLanguage())) {
                    content.append("Language: ").append(result.getLanguage()).append("\n");
                }
                if (result.getTopics() != null && !result.getTopics().isEmpty()) {
                    content.append("Topics: ").append(String.join(", ", result.getTopics())).append("\n");
                }
                break;
            case "code":
                content.append("File: ").append(result.getPath() != null ? result.getPath() : result.getName()).append("\n");
                if (result.getRepository() != null) {
                    content.append("Repository: ").append(result.getRepository().getFullName()).append("\n");
                }
                if (StringUtils.isNotBlank(result.getContent())) {
                    content.append("Content: ").append(result.getContent()).append("\n");
                }
                break;
            case "users":
                content.append("User: ").append(result.getName() != null ? result.getName() : "").append("\n");
                if (result.getOwner() != null && StringUtils.isNotBlank(result.getOwner().getLogin())) {
                    content.append("Login: ").append(result.getOwner().getLogin()).append("\n");
                }
                break;
            case "issues":
                content.append("Issue: ").append(result.getName() != null ? result.getName() : "").append("\n");
                if (StringUtils.isNotBlank(result.getDescription())) {
                    content.append("Description: ").append(result.getDescription()).append("\n");
                }
                break;
            default:
                content.append("Title: ").append(result.getName() != null ? result.getName() : "").append("\n");
                if (StringUtils.isNotBlank(result.getDescription())) {
                    content.append("Description: ").append(result.getDescription()).append("\n");
                }
        }

        return content.toString();
    }

    /**
     * 生成元数据
     *
     * @param result 搜索结果
     * @return 元数据Map
     */
    private Map<String, Object> generateMetadata(SearchResult result) {
        Map<String, Object> metadata = new HashMap<>();
        
        metadata.put("search_type", searchType);
        metadata.put("id", result.getId());
        metadata.put("name", result.getName());
        metadata.put("url", result.getHtmlUrl());
        metadata.put("score", result.getScore());

        switch (searchType.toLowerCase()) {
            case "repositories":
                metadata.put("full_name", result.getFullName());
                metadata.put("language", result.getLanguage());
                metadata.put("stars", result.getStargazersCount());
                metadata.put("forks", result.getForksCount());
                metadata.put("watchers", result.getWatchersCount());
                metadata.put("size", result.getSize());
                metadata.put("default_branch", result.getDefaultBranch());
                metadata.put("is_private", result.getIsPrivate());
                metadata.put("is_fork", result.getFork());
                metadata.put("topics", result.getTopics());
                metadata.put("created_at", result.getCreatedAt());
                metadata.put("updated_at", result.getUpdatedAt());
                metadata.put("pushed_at", result.getPushedAt());
                if (result.getOwner() != null) {
                    metadata.put("owner_login", result.getOwner().getLogin());
                    metadata.put("owner_type", result.getOwner().getType());
                }
                if (result.getLicense() != null) {
                    metadata.put("license", result.getLicense().getName());
                }
                break;
            case "code":
                metadata.put("path", result.getPath());
                metadata.put("git_url", result.getGitUrl());
                if (result.getRepository() != null) {
                    metadata.put("repository_name", result.getRepository().getFullName());
                    metadata.put("repository_url", result.getRepository().getHtmlUrl());
                    metadata.put("repository_private", result.getRepository().getIsPrivate());
                }
                break;
            case "users":
                if (result.getOwner() != null) {
                    metadata.put("login", result.getOwner().getLogin());
                    metadata.put("type", result.getOwner().getType());
                    metadata.put("avatar_url", result.getOwner().getAvatarUrl());
                }
                break;
            case "issues":
                // 添加issue特定的元数据
                break;
        }

        return metadata;
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        // 使用嵌入模型计算向量
        List<Document> embeddedDocuments = embedding.embedDocument(documents);

        for (Document document : embeddedDocuments) {
            // 从元数据中提取原始结果
            SearchResult originalResult = null;
            if (document.getMetadata() != null && document.getMetadata().get("original_result") instanceof SearchResult) {
                originalResult = (SearchResult) document.getMetadata().get("original_result");
            }
            
            GitHubDocument ghDoc = new GitHubDocument(
                    document.getUniqueId(),
                    document.getPageContent(),
                    document.getMetadata(),
                    originalResult
            );
            
            if (document.getEmbedding() != null) {
                ghDoc.setEmbedding(document.getEmbedding());
            }
            
            if (document.getScore() != null) {
                ghDoc.setScore(document.getScore());
            }

            this.documents.add(ghDoc);
        }

        log.info("Added {} documents to GitHub vector store", embeddedDocuments.size());
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isBlank(query) || embedding == null) {
            return new ArrayList<>();
        }

        // 生成查询向量
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (embeddingStrings.isEmpty()) {
            return new ArrayList<>();
        }

        String embeddingString = embeddingStrings.get(0);
        
        // 更健壮的向量解析
        List<Double> queryEmbedding;
        try {
            // 尝试解析JSON数组格式的向量
            if (embeddingString.trim().startsWith("[") && embeddingString.trim().endsWith("]")) {
                queryEmbedding = JSON.parseArray(embeddingString, Double.class);
            } else {
                log.warn("Unexpected embedding format: {}", embeddingString);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Failed to parse embedding vector: {}", embeddingString, e);
            return new ArrayList<>();
        }

        if (queryEmbedding.isEmpty()) {
            return new ArrayList<>();
        }

        // 计算相似度并排序
        List<GitHubDocument> candidates = new ArrayList<>();
        
        for (GitHubDocument doc : this.documents) {
            if (doc.getEmbedding() != null) {
                double similarity = cosineSimilarity(queryEmbedding, doc.getEmbedding());
                
                // 应用距离阈值过滤
                if (maxDistanceValue == null || (1.0 - similarity) <= maxDistanceValue) {
                    doc.setScore(similarity);
                    candidates.add(doc);
                }
            }
        }

        // 按相似度排序并取前k个
        candidates.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        int limit = Math.min(k, candidates.size());
        List<GitHubDocument> topResults = candidates.subList(0, limit);

        // 转换为Document对象
        return topResults.stream().map(ghDoc -> {
            Document doc = new Document();
            doc.setUniqueId(ghDoc.getId());
            doc.setPageContent(ghDoc.getContent());
            doc.setMetadata(ghDoc.getMetadata());
            doc.setScore(ghDoc.getScore());
            doc.setEmbedding(ghDoc.getEmbedding());
            return doc;
        }).collect(Collectors.toList());
    }

    /**
     * 计算余弦相似度
     *
     * @param vectorA 向量A
     * @param vectorB 向量B
     * @return 余弦相似度
     */
    private double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vector dimensions must be equal");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += vectorA.get(i) * vectorA.get(i);
            normB += vectorB.get(i) * vectorB.get(i);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 清空所有文档
     */
    public void clearDocuments() {
        this.documents.clear();
        log.info("Cleared all documents from GitHub vector store");
    }

    /**
     * 获取文档数量
     *
     * @return 文档数量
     */
    public int getDocumentCount() {
        return this.documents.size();
    }

    /**
     * 通过搜索类型过滤文档
     *
     * @param searchType 搜索类型
     * @return 过滤后的文档列表
     */
    public List<Document> getDocumentsByType(String searchType) {
        return this.documents.stream()
                .filter(doc -> searchType.equals(doc.getMetadata().get("search_type")))
                .map(ghDoc -> {
                    Document doc = new Document();
                    doc.setUniqueId(ghDoc.getId());
                    doc.setPageContent(ghDoc.getContent());
                    doc.setMetadata(ghDoc.getMetadata());
                    doc.setScore(ghDoc.getScore());
                    doc.setEmbedding(ghDoc.getEmbedding());
                    return doc;
                })
                .collect(Collectors.toList());
    }
}
