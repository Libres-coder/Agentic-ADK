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
package com.alibaba.langengine.vespa.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import static com.alibaba.langengine.vespa.VespaConfiguration.*;


@Slf4j
@EqualsAndHashCode(callSuper = false)
@Data
public class Vespa extends VectorStore {

    private Embeddings embedding;

    private final String documentType;
    
    private final String namespace;

    private final VespaService vespaService;

    public Vespa(String documentType) {
        this(documentType, "default", null);
    }

    public Vespa(String documentType, String namespace) {
        this(documentType, namespace, null);
    }

    public Vespa(String documentType, String namespace, VespaParam vespaParam) {
        this.documentType = documentType;
        this.namespace = namespace;

        String queryUrl = VESPA_QUERY_URL;
        String feedUrl = VESPA_FEED_URL;
        
        if (queryUrl == null) {
            queryUrl = "http://localhost:8080";
        }
        if (feedUrl == null) {
            feedUrl = "http://localhost:8080";
        }

        this.vespaService = new VespaService(queryUrl, feedUrl, namespace, documentType, vespaParam);
    }

    public void init() {
        try {
            vespaService.init();
        } catch (Exception e) {
            log.error("init vespa failed", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        documents = embedding.embedDocument(documents);
        vespaService.addDocuments(documents);
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
            return Lists.newArrayList();
        }
        List<Float> vec = JSON.parseArray(embeddingStrings.get(0), Float.class);
        return vespaService.similaritySearch(vec, k, maxDistanceValue, type);
    }

    public void close() {
        if (vespaService != null) {
            vespaService.close();
        }
    }
}
