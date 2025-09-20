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
import org.typesense.api.Client;
import org.typesense.api.Configuration;
import org.typesense.model.CollectionSchema;
import org.typesense.model.Field;
import org.typesense.model.SearchParameters;
import org.typesense.model.SearchResult;
import org.typesense.resources.Node;

import java.time.Duration;
import java.util.*;


@Slf4j
@Data
public class TypesenseClient {

    private Client client;
    private String collectionName;

    public TypesenseClient(String serverUrl, String apiKey, String collectionName) {
        this.collectionName = collectionName;

        List<Node> nodes = new ArrayList<>();
        String host = serverUrl.replace("http://", "").replace("https://", "");
        if (host.contains(":")) {
            String[] parts = host.split(":");
            nodes.add(new Node("http", parts[0], parts[1]));
        } else {
            nodes.add(new Node("http", host, "8108"));
        }

        Configuration configuration = new Configuration(nodes, Duration.ofSeconds(60), apiKey);
        this.client = new Client(configuration);
    }

    public void createCollectionIfNotExists(int dimensions) throws Exception {
        try {
            client.collections(collectionName).retrieve();
            log.info("Collection {} already exists", collectionName);
        } catch (Exception e) {
            log.info("Creating collection {} with dimensions {}", collectionName, dimensions);

            List<Field> fields = new ArrayList<>();
            fields.add(new Field().name("id").type("string"));
            fields.add(new Field().name("content").type("string"));
            fields.add(new Field().name("embedding").type("float[]").numDim(dimensions));

            CollectionSchema collectionSchema = new CollectionSchema();
            collectionSchema.name(collectionName);
            collectionSchema.fields(fields);

            client.collections().create(collectionSchema);
            log.info("Collection {} created successfully", collectionName);
        }
    }

    public void upsertDocument(Map<String, Object> document) throws Exception {
        client.collections(collectionName).documents().upsert(document);
    }

    public SearchResult searchSimilar(List<Float> queryVector, int numResults) throws Exception {
        SearchParameters searchParameters = new SearchParameters()
                .q("*")
                .vectorQuery("embedding:([" + String.join(",", queryVector.stream().map(String::valueOf).toArray(String[]::new)) + "])")
                .perPage(numResults);

        return client.collections(collectionName).documents().search(searchParameters);
    }

    public void deleteDocument(String documentId) throws Exception {
        client.collections(collectionName).documents(documentId).delete();
    }

    public void deleteCollection() throws Exception {
        client.collections(collectionName).delete();
    }
}