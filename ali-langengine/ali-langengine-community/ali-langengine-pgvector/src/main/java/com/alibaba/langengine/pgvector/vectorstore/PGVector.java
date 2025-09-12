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
package com.alibaba.langengine.pgvector.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.alibaba.langengine.pgvector.PGVectorConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class PGVector extends VectorStore {

    private Embeddings embedding;

    private final String tableName;

    private final PGVectorService pgVectorService;

    public PGVector(String tableName) {
        this(tableName, null);
    }

    public PGVector(String tableName, PGVectorParam pgVectorParam) {
        this.tableName = tableName;

        String url = PGVECTOR_URL;
        String username = PGVECTOR_USERNAME != null ? PGVECTOR_USERNAME : "postgres";
        String password = PGVECTOR_PASSWORD != null ? PGVECTOR_PASSWORD : "";

        this.pgVectorService = new PGVectorService(url, username, password, tableName, pgVectorParam);
    }

    public void init() {
        try {
            pgVectorService.init();
        } catch (Exception e) {
            log.error("init pgvector failed", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        documents = embedding.embedDocument(documents);
        pgVectorService.addDocuments(documents);
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
            return Lists.newArrayList();
        }
        List<Float> vec = JSON.parseArray(embeddingStrings.get(0), Float.class);
        return pgVectorService.similaritySearch(vec, k, maxDistanceValue, type);
    }
}
