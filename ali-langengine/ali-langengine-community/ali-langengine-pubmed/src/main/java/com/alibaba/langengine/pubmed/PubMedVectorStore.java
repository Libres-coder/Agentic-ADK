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
package com.alibaba.langengine.pubmed;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.util.VectorUtils;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.pubmed.model.PubMedArticle;
import com.alibaba.langengine.pubmed.model.PubMedSearchRequest;
import com.alibaba.langengine.pubmed.model.PubMedSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
public class PubMedVectorStore extends VectorStore {

    /**
     * 默认最大缓存大小
     */
    private static final int DEFAULT_MAX_CACHE_SIZE = 10000;

    /**
     * 默认相似度阈值
     */
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;

    /**
     * 文档元数据键常量
     */
    private static final String METADATA_PMID = "pmid";
    private static final String METADATA_TITLE = "title";
    private static final String METADATA_AUTHORS = "authors";
    private static final String METADATA_JOURNAL = "journal";
    private static final String METADATA_PUBLISH_DATE = "publish_date";
    private static final String METADATA_DOI = "doi";
    private static final String METADATA_URL = "url";
    private static final String METADATA_KEYWORDS = "keywords";
    private static final String METADATA_MESH_TERMS = "mesh_terms";
    private static final String METADATA_ARTICLE_TYPE = "article_type";
    private static final String METADATA_LANGUAGE = "language";
    private static final String METADATA_COUNTRY = "country";

    private final PubMedClient pubmedClient;
    private final Map<String, Document> documentCache;
    private final Map<String, List<Double>> embeddingCache;
    private final int maxCacheSize;
    private final double similarityThreshold;
    private final Embeddings embeddings;

    /**
     * 构造函数
     *
     * @param embeddings 嵌入模型
     * @param configuration PubMed配置
     */
    public PubMedVectorStore(Embeddings embeddings, PubMedConfiguration configuration) {
        this(embeddings, configuration, DEFAULT_MAX_CACHE_SIZE, DEFAULT_SIMILARITY_THRESHOLD);
    }

    /**
     * 构造函数
     *
     * @param embeddings 嵌入模型
     * @param configuration PubMed配置
     * @param maxCacheSize 最大缓存大小
     * @param similarityThreshold 相似度阈值
     */
    public PubMedVectorStore(Embeddings embeddings, PubMedConfiguration configuration, 
                           int maxCacheSize, double similarityThreshold) {
        super();
        
        if (embeddings == null) {
            throw new IllegalArgumentException("Embeddings cannot be null");
        }
        
        if (configuration == null) {
            throw new IllegalArgumentException("PubMed configuration cannot be null");
        }
        
        if (maxCacheSize <= 0) {
            throw new IllegalArgumentException("Max cache size must be positive");
        }
        
        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
        }

        this.embeddings = embeddings;
        this.pubmedClient = new PubMedClient(configuration);
        this.documentCache = new ConcurrentHashMap<>();
        this.embeddingCache = new ConcurrentHashMap<>();
        this.maxCacheSize = maxCacheSize;
        this.similarityThreshold = similarityThreshold;

        log.info("PubMed vector store initialized with max cache size: {}, similarity threshold: {}", 
                maxCacheSize, similarityThreshold);
    }

    @Override
    public void addDocuments(List<Document> documents) throws PubMedException {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        // 首先为所有文档生成嵌入向量
        try {
            List<Document> embeddedDocuments = embeddings.embedDocument(documents);
            
            for (Document document : embeddedDocuments) {
                try {
                    String id = generateDocumentId(document);
                    
                    // 检查缓存大小限制
                    enforceMemoryLimit();
                    
                    // 缓存文档
                    documentCache.put(id, document);
                    
                    // 缓存嵌入向量
                    if (document.getEmbedding() != null) {
                        embeddingCache.put(id, document.getEmbedding());
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to add document: {}", e.getMessage(), e);
                    throw new PubMedException("Failed to add document", e);
                }
            }
            
            log.info("Added {} documents with embeddings to PubMed vector store", embeddedDocuments.size());
            
        } catch (Exception e) {
            log.error("Failed to generate embeddings for documents: {}", e.getMessage(), e);
            throw new PubMedException("Failed to generate embeddings for documents", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) throws PubMedException {
        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }
        
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }

        try {
            log.info("Performing similarity search for query: '{}' with k={}", query, k);
            
            // 1. 生成查询向量
            List<Double> queryEmbedding = embeddings.embedQuery(query);
            
            // 2. 如果缓存为空，先从PubMed搜索一些候选文档
            if (documentCache.isEmpty()) {
                PubMedSearchRequest request = PubMedSearchRequest.withLimit(query, Math.max(k * 3, 50));
                pubmedClient.enforceRateLimit(); // 强制执行速率限制
                PubMedSearchResponse response = pubmedClient.search(request);
                
                if (response.isSuccessful() && response.hasResults()) {
                    List<Document> candidateDocuments = convertArticlesToDocuments(response.getArticles());
                    addDocuments(candidateDocuments); // 这会生成嵌入并添加到缓存
                }
            }
            
            // 3. 计算相似度并排序
            List<DocumentWithScore> scoredDocuments = new ArrayList<>();
            
            for (Map.Entry<String, Document> entry : documentCache.entrySet()) {
                Document document = entry.getValue();
                List<Double> docEmbedding = embeddingCache.get(entry.getKey());
                
                if (docEmbedding != null) {
                    double similarity = VectorUtils.calculateCosineSimilarity(queryEmbedding, docEmbedding);
                    double distance = 1.0 - similarity; // 转换为距离
                    
                    // 应用距离阈值过滤
                    if (maxDistanceValue == null || distance <= maxDistanceValue) {
                        scoredDocuments.add(new DocumentWithScore(document, similarity, distance));
                    }
                }
            }
            
            // 4. 按相似度排序并返回前k个结果
            List<Document> results = scoredDocuments.stream()
                    .sorted((a, b) -> Double.compare(b.similarity, a.similarity)) // 按相似度降序
                    .limit(k)
                    .map(ds -> {
                        // 添加相似度分数到元数据
                        Map<String, Object> metadata = new HashMap<>(ds.document.getMetadata());
                        metadata.put("similarity_score", ds.similarity);
                        metadata.put("distance", ds.distance);
                        return new Document(ds.document.getPageContent(), metadata);
                    })
                    .collect(Collectors.toList());
            
            log.info("Similarity search completed, returning {} documents", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Similarity search failed: {}", e.getMessage(), e);
            throw new PubMedException("Similarity search failed", e);
        }
    }

    /**
     * 搜索PubMed文章
     *
     * @param request 搜索请求
     * @return 文档列表
     */
    public List<Document> searchPubMed(PubMedSearchRequest request) throws PubMedException {
        if (request == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }

        try {
            log.info("Searching PubMed with request: {}", request.getQuery());
            
            pubmedClient.enforceRateLimit(); // 强制执行速率限制
            PubMedSearchResponse response = pubmedClient.search(request);
            
            if (!response.isSuccessful()) {
                log.warn("PubMed search failed: {}", response.getErrorMessage());
                throw new PubMedException("PubMed search failed: " + response.getErrorMessage());
            }
            
            if (!response.hasResults()) {
                log.info("No results found for PubMed search");
                return new ArrayList<>();
            }
            
            List<Document> documents = convertArticlesToDocuments(response.getArticles());
            
            log.info("PubMed search completed, found {} articles", documents.size());
            return documents;
            
        } catch (Exception e) {
            log.error("PubMed search failed: {}", e.getMessage(), e);
            throw new PubMedException("PubMed search failed", e);
        }
    }

    /**
     * 将PubMed文章转换为文档
     *
     * @param articles 文章列表
     * @return 文档列表
     */
    private List<Document> convertArticlesToDocuments(List<PubMedArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return new ArrayList<>();
        }

        return articles.stream()
                .filter(Objects::nonNull)
                .map(this::convertArticleToDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 将PubMed文章转换为文档
     *
     * @param article 文章对象
     * @return 文档对象
     */
    private Document convertArticleToDocument(PubMedArticle article) {
        if (article == null) {
            return null;
        }

        try {
            // 构建文档内容
            String content = buildDocumentContent(article);
            
            // 构建元数据
            Map<String, Object> metadata = buildDocumentMetadata(article);
            
            return new Document(content, metadata);
            
        } catch (Exception e) {
            log.warn("Failed to convert article to document: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 构建文档内容
     *
     * @param article 文章对象
     * @return 文档内容
     */
    private String buildDocumentContent(PubMedArticle article) {
        StringBuilder content = new StringBuilder();
        
        if (StringUtils.isNotBlank(article.getTitle())) {
            content.append("Title: ").append(article.getTitle()).append("\n");
        }
        
        if (StringUtils.isNotBlank(article.getAbstractText())) {
            content.append("Abstract: ").append(article.getAbstractText()).append("\n");
        }
        
        if (article.getAuthorNames() != null && !article.getAuthorNames().isEmpty()) {
            content.append("Authors: ").append(String.join(", ", article.getAuthorNames())).append("\n");
        }
        
        if (StringUtils.isNotBlank(article.getJournal())) {
            content.append("Journal: ").append(article.getJournal()).append("\n");
        }
        
        if (StringUtils.isNotBlank(article.getPublishDate())) {
            content.append("Publication Date: ").append(article.getPublishDate()).append("\n");
        }
        
        if (article.getKeywords() != null && !article.getKeywords().isEmpty()) {
            content.append("Keywords: ").append(String.join(", ", article.getKeywords())).append("\n");
        }
        
        if (article.getMeshTerms() != null && !article.getMeshTerms().isEmpty()) {
            content.append("MeSH Terms: ").append(String.join(", ", article.getMeshTerms())).append("\n");
        }
        
        return content.toString().trim();
    }

    /**
     * 构建文档元数据
     *
     * @param article 文章对象
     * @return 元数据映射
     */
    private Map<String, Object> buildDocumentMetadata(PubMedArticle article) {
        Map<String, Object> metadata = new HashMap<>();
        
        if (StringUtils.isNotBlank(article.getPmid())) {
            metadata.put(METADATA_PMID, article.getPmid());
        }
        
        if (StringUtils.isNotBlank(article.getTitle())) {
            metadata.put(METADATA_TITLE, article.getTitle());
        }
        
        if (article.getAuthorNames() != null && !article.getAuthorNames().isEmpty()) {
            metadata.put(METADATA_AUTHORS, article.getAuthorNames());
        }
        
        if (StringUtils.isNotBlank(article.getJournal())) {
            metadata.put(METADATA_JOURNAL, article.getJournal());
        }
        
        if (StringUtils.isNotBlank(article.getPublishDate())) {
            metadata.put(METADATA_PUBLISH_DATE, article.getPublishDate());
        }
        
        if (StringUtils.isNotBlank(article.getDoi())) {
            metadata.put(METADATA_DOI, article.getDoi());
        }
        
        String url = article.getPubMedUrl();
        if (StringUtils.isNotBlank(url)) {
            metadata.put(METADATA_URL, url);
        }
        
        if (article.getKeywords() != null && !article.getKeywords().isEmpty()) {
            metadata.put(METADATA_KEYWORDS, article.getKeywords());
        }
        
        if (article.getMeshTerms() != null && !article.getMeshTerms().isEmpty()) {
            metadata.put(METADATA_MESH_TERMS, article.getMeshTerms());
        }
        
        if (StringUtils.isNotBlank(article.getArticleType())) {
            metadata.put(METADATA_ARTICLE_TYPE, article.getArticleType());
        }
        
        if (StringUtils.isNotBlank(article.getLanguage())) {
            metadata.put(METADATA_LANGUAGE, article.getLanguage());
        }
        
        if (StringUtils.isNotBlank(article.getCountry())) {
            metadata.put(METADATA_COUNTRY, article.getCountry());
        }
        
        return metadata;
    }

    /**
     * 生成文档ID
     *
     * @param document 文档
     * @return 文档ID
     */
    private String generateDocumentId(Document document) {
        // 优先使用PMID作为ID
        Object pmid = document.getMetadata().get(METADATA_PMID);
        if (pmid != null) {
            return "pmid_" + pmid.toString();
        }
        
        // 否则使用内容哈希作为ID
        return "doc_" + Math.abs(document.getPageContent().hashCode());
    }

    /**
     * 强制执行内存限制
     */
    private void enforceMemoryLimit() {
        if (documentCache.size() >= maxCacheSize) {
            // 使用FIFO策略移除最旧的条目
            int removeCount = maxCacheSize / 4; // 移除25%的条目
            
            List<String> keysToRemove = documentCache.keySet().stream()
                    .limit(removeCount)
                    .collect(Collectors.toList());
            
            for (String key : keysToRemove) {
                documentCache.remove(key);
                embeddingCache.remove(key);
            }
            
            log.debug("Removed {} cached items to enforce memory limit", removeCount);
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public String getCacheStatistics() {
        return String.format("Document cache: %d/%d, Embedding cache: %d/%d", 
                documentCache.size(), maxCacheSize,
                embeddingCache.size(), maxCacheSize);
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        documentCache.clear();
        embeddingCache.clear();
        log.info("PubMed vector store cache cleared");
    }

    /**
     * 关闭向量存储
     */
    public void close() {
        clearCache();
        if (pubmedClient != null) {
            pubmedClient.close();
        }
        log.info("PubMed vector store closed");
    }

    /**
     * 带分数的文档包装类
     */
    private static class DocumentWithScore {
        final Document document;
        final double similarity;
        final double distance;
        
        DocumentWithScore(Document document, double similarity, double distance) {
            this.document = document;
            this.similarity = similarity;
            this.distance = distance;
        }
    }
}
