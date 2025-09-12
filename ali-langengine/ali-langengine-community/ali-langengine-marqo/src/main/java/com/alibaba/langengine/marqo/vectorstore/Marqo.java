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
package com.alibaba.langengine.marqo.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.marqo.MarqoConfiguration;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.List;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Marqo extends VectorStore {

    private Embeddings embedding;
    private final String indexName;
    private final MarqoService marqoService;

    public Marqo(String indexName) {
        this(indexName, null);
    }

    public Marqo(String indexName, MarqoParam marqoParam) {
        this.indexName = indexName;

        String serverUrl = MarqoConfiguration.getMarqoServerUrl();
        if (StringUtils.isBlank(serverUrl)) {
            serverUrl = "http://localhost:8882";
        }
        if (!serverUrl.contains("://")) {
            serverUrl = "http://" + serverUrl;
        }

        String apiKey = MarqoConfiguration.getMarqoApiKey();

        try {
            URI uri = URI.create(serverUrl);
            String normalizedUrl = uri.getScheme() + "://" + uri.getAuthority();
            
            this.marqoService = new MarqoService(normalizedUrl, apiKey, indexName, marqoParam);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse Marqo server URL: {}", serverUrl, e);
            throw new IllegalArgumentException("Invalid Marqo server URL: " + serverUrl, e);
        } catch (Exception e) {
            log.error("Failed to create Marqo service", e);
            throw new RuntimeException("Failed to create Marqo service", e);
        }
    }

    public void init() {
        try {
            marqoService.init();
        } catch (Exception e) {
            log.error("Failed to initialize Marqo", e);
            throw new RuntimeException("Failed to initialize Marqo", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            if (embedding != null) {
                documents = embedding.embedDocument(documents);
            }
            
            marqoService.addDocuments(documents);
        } catch (Exception e) {
            log.error("Failed to add documents to Marqo", e);
            throw new RuntimeException("Failed to add documents", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isBlank(query)) {
            return Lists.newArrayList();
        }

        try {
            return marqoService.similaritySearch(query, k, maxDistanceValue, type);
        } catch (Exception e) {
            log.error("Failed to perform similarity search in Marqo", e);
            throw new RuntimeException("Failed to perform similarity search", e);
        }
    }


    public void dropIndex() {
        try {
            marqoService.dropIndex();
        } catch (Exception e) {
            log.error("Failed to drop Marqo index", e);
            throw new RuntimeException("Failed to drop index", e);
        }
    }

    public MarqoService getMarqoService() {
        return marqoService;
    }
}
