/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.qdrant.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.alibaba.langengine.qdrant.QdrantConfiguration.QDRANT_SERVER_URL;

/**
 * @author zh_xiaoji
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Qdrant extends VectorStore {

    /**
     * Embedding model for vector generation
     */
    private Embeddings embedding;

    /**
     * Collection name for vector storage
     */
    private final String collectionName;

    /**
     * Vector dimension size
     */
    private final int vectorSize;

    /**
     * Distance metric for similarity calculation
     */
    private final String distanceMetric;

    /**
     * Internal service for Qdrant operations
     * -- GETTER --
     *  get Qdrant instance

     */
    private final QdrantService qdrantService;

    public Qdrant(String collectionName) {
        this(collectionName, 1536, "Cosine");
    }

    public Qdrant(String collectionName, int vectorSize, String distanceMetric) {
        this.collectionName = collectionName != null ? collectionName : UUID.randomUUID().toString();
        this.vectorSize = vectorSize;
        this.distanceMetric = distanceMetric;

        // If you need to specify port, set it in qdrant_server_url property with host:port format, e.g.: 127.0.0.1:6333
        String serverUrl = QDRANT_SERVER_URL;
        if (StringUtils.isEmpty(serverUrl)) {
            serverUrl = "http://localhost:6333";
        }

        this.qdrantService = new QdrantService(serverUrl);

        // Auto-create collection if it doesn't exist
        initializeCollection();
    }


    /**
     * Initialize collection
     */
    private void initializeCollection() {
        try {
            if (!qdrantService.collectionExists(collectionName)) {
                boolean created = qdrantService.createCollection(collectionName, vectorSize, distanceMetric);
                if (created) {
                    log.info("Collection '{}' created successfully", collectionName);
                } else {
                    log.warn("Failed to create collection '{}'", collectionName);
                }
            } else {
                log.info("Collection '{}' already exists", collectionName);
            }
        } catch (Exception e) {
            log.error("Error initializing collection '{}': {}", collectionName, e.getMessage());
        }
    }

    /**
     * Add documents to Qdrant vector store
     *
     * @param documents List of documents to add
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        try {
            // generate embedding for documents
            documents.stream()
                    .filter(document -> !StringUtils.isEmpty(document.getPageContent()))
                    .forEach(document -> {
                        if (StringUtils.isEmpty(document.getUniqueId())) {
                            document.setUniqueId(UUID.randomUUID().toString());
                        }
                        if (document.getMetadata() == null) {
                            document.setMetadata(new HashMap<String, Object>());
                        }

                        // Generate embedding if not provided and embedding model is available
                        if ((document.getEmbedding() == null || document.getEmbedding().isEmpty()) && embedding != null) {
                            List<Document> embeddedDocs = embedding.embedTexts(Collections.singletonList(document.getPageContent()));
                            if (!embeddedDocs.isEmpty()) {
                                document.setEmbedding(embeddedDocs.get(0).getEmbedding());
                            }
                        }
                    });

            // Add to Qdrant
            boolean success = qdrantService.addPoints(collectionName, documents);
            if (!success) {
                throw new RuntimeException("Failed to add documents to Qdrant collection: " + collectionName);
            }
        } catch (Exception e) {
            log.error("Error adding documents to Qdrant: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Perform similarity search in Qdrant vector store
     *
     * @param query            Search query text
     * @param k                Number of results to return
     * @param maxDistanceValue Maximum distance threshold
     * @param type             Search type (unused)
     * @return List of similar documents
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isEmpty(query) || embedding == null) {
            return new ArrayList<>();
        }

        try {
            // Generate query vector
            List<Float> queryVector = null;
            List<Document> embeddedQuery = embedding.embedTexts(List.of(query));
            if (!embeddedQuery.isEmpty() && embeddedQuery.get(0).getEmbedding() != null) {
                queryVector = embeddedQuery.get(0).getEmbedding().stream()
                        .map(Double::floatValue)
                        .collect(Collectors.toList());
            }

            if (queryVector == null || queryVector.isEmpty()) {
                log.error("Failed to generate query vector for query '{}'", query);
                return new ArrayList<>();
            }
            // Execute similarity search
            return qdrantService.searchSimilar(collectionName, queryVector, k, maxDistanceValue);
        } catch (Exception e) {
            log.error("Error performing similarity search: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Add text vectors to the collection
     *
     * @param texts    Text content to add
     * @param metadata Metadata for each text
     * @param ids      Document IDs
     * @return List of document IDs
     */
    public List<String> addTexts(
            Iterable<String> texts,
            List<Map<String, Object>> metadata,
            List<String> ids
    ) {
        List<String> textsList = new ArrayList<>();
        texts.forEach(textsList::add);
        // Handle the case where the user doesn't provide ids
        if (ids == null) {
            ids = textsList.stream()
                    .map(text -> UUID.randomUUID().toString())
                    .collect(Collectors.toList());
        }

        // Create Document list using IntStream
        List<String> finalIds = ids;
        List<Document> documents = IntStream.range(0, textsList.size())
                .mapToObj(i -> {
                    Document document = new Document();
                    document.setUniqueId(i < finalIds.size() ? finalIds.get(i) : UUID.randomUUID().toString());
                    document.setPageContent(textsList.get(i));
                    if (metadata != null && i < metadata.size()) {
                        document.setMetadata(metadata.get(i));
                    }
                    return document;
                })
                .collect(Collectors.toList());

        // Add documents
        addDocuments(documents);
        return ids;
    }

    /**
     * Create collection if not exists
     */
    public boolean createCollection(int vectorSize, String distanceMetric) {
        return qdrantService.createCollection(collectionName, vectorSize, distanceMetric);
    }

    /**
     * Check if collection exists
     */
    public boolean collectionExists() {
        return qdrantService.collectionExists(collectionName);
    }

    /**
     * Get collection info
     */
    public Map<String, Object> getCollectionInfo() {
        return qdrantService.getCollectionInfo(collectionName);
    }

    /**
     * Delete collection
     */
    public boolean deleteCollection() {
        return qdrantService.deleteCollection(collectionName);
    }

    /**
     * Count points in collection
     */
    public long countPoints() {
        return qdrantService.countPoints(collectionName);
    }

    /**
     * Test connection to Qdrant service
     */
    public boolean testConnection() {
        return qdrantService.isHealthy();
    }

    /**
     * Get cluster info
     */
    public String getClusterInfo() {
        return qdrantService.getClusterInfo();
    }

    /**
     * List all collections
     */
    public String listCollections() {
        return qdrantService.listCollections();
    }

}
