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
package com.alibaba.langengine.typesense.vectorstore.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.typesense.model.SearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Data
public class TypesenseService {

    private TypesenseClient client;

    public TypesenseService(String serverUrl, String apiKey, String collectionName) {
        this.client = new TypesenseClient(serverUrl, apiKey, collectionName);
    }

    public void initializeCollection(int dimensions) throws Exception {
        client.createCollectionIfNotExists(dimensions);
    }

    public void addDocument(String id, String content, List<Float> embedding, Map<String, Object> metadata) throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("id", id);
        document.put("content", content);
        document.put("embedding", embedding);

        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (!document.containsKey(entry.getKey())) {
                    document.put(entry.getKey(), entry.getValue());
                }
            }
        }

        client.upsertDocument(document);
    }

    public SearchResult searchSimilar(List<Float> queryVector, int numResults) throws Exception {
        return client.searchSimilar(queryVector, numResults);
    }

    public void deleteDocument(String documentId) throws Exception {
        client.deleteDocument(documentId);
    }

    public void deleteCollection() throws Exception {
        client.deleteCollection();
    }
}