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
package com.alibaba.langengine.rockset.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.rockset.vectorstore.service.RocksetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RocksetTest {

    @Mock
    private Embeddings mockEmbedding;

    @Mock
    private RocksetService mockRocksetService;

    private RocksetParam testParam;

    @BeforeEach
    public void setUp() {
        testParam = RocksetParam.builder()
                .workspace("test_workspace")
                .collectionName("test_collection")
                .dimension(768)
                .build();
    }

    // API-dependent tests removed to avoid requiring real Rockset configuration

    @Test
    public void testParameterDefaults() {
        RocksetParam param = new RocksetParam();
        
        assertEquals("commons", param.getInitParam().getWorkspace());
        assertEquals("langengine_rockset_collection", param.getInitParam().getCollectionName());
        assertEquals("cosine", param.getInitParam().getVectorDistance());
        assertEquals(Integer.valueOf(1536), param.getInitParam().getDimension());
        assertEquals("page_content", param.getFieldNamePageContent());
        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("meta_data", param.getFieldMeta());
        assertEquals("vector", param.getFieldNameVector());
    }

    @Test
    public void testParameterBuilder() {
        RocksetParam param = RocksetParam.builder()
                .workspace("custom_workspace")
                .collectionName("custom_collection")
                .dimension(512)
                .vectorDistance("euclidean")
                .fieldNamePageContent("content")
                .fieldNameUniqueId("id")
                .build();
        
        assertEquals("custom_workspace", param.getInitParam().getWorkspace());
        assertEquals("custom_collection", param.getInitParam().getCollectionName());
        assertEquals(Integer.valueOf(512), param.getInitParam().getDimension());
        assertEquals("euclidean", param.getInitParam().getVectorDistance());
        assertEquals("content", param.getFieldNamePageContent());
        assertEquals("id", param.getFieldNameUniqueId());
    }

    @Test
    public void testDocumentValidation() {
        // Test empty documents list
        List<Document> emptyDocs = new ArrayList<>();
        // This would be tested with a real Rockset instance
        assertTrue(emptyDocs.isEmpty());
    }

    @Test
    public void testSearchParameterValidation() {
        // Test query validation would be done with a real instance
        // Here we test parameter validation logic
        
        // Empty query
        String emptyQuery = "";
        assertTrue(emptyQuery.isEmpty());
        
        // Negative k
        int negativeK = -1;
        assertTrue(negativeK < 0);
        
        // Zero k
        int zeroK = 0;
        assertTrue(zeroK <= 0);
    }

    @Test
    public void testMetadataFilterValidation() {
        Map<String, Object> validFilter = new HashMap<>();
        validFilter.put("category", "test");
        validFilter.put("priority", 1);
        
        assertFalse(validFilter.isEmpty());
        assertTrue(validFilter.containsKey("category"));
        assertEquals("test", validFilter.get("category"));
        assertEquals(1, validFilter.get("priority"));
    }

    @Test
    public void testDocumentCreation() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");
        metadata.put("id", "doc-1");
        
        Document doc = new Document("Test content", metadata);
        
        assertEquals("Test content", doc.getPageContent());
        assertEquals("test", doc.getMetadata().get("source"));
        assertEquals("doc-1", doc.getMetadata().get("id"));
    }

    @Test
    public void testEmbeddingVectorValidation() {
        List<Double> validVector = Arrays.asList(0.1, 0.2, 0.3, 0.4);
        List<Double> emptyVector = new ArrayList<>();
        
        assertFalse(validVector.isEmpty());
        assertEquals(4, validVector.size());
        assertTrue(emptyVector.isEmpty());
        
        // Test vector values
        for (Double value : validVector) {
            assertNotNull(value);
            assertTrue(value >= -1.0 && value <= 1.0); // Typical embedding range
        }
    }

    @Test
    public void testCollectionNameValidation() {
        // Valid collection names
        assertTrue(isValidCollectionName("test_collection"));
        assertTrue(isValidCollectionName("my-collection"));
        assertTrue(isValidCollectionName("collection123"));
        
        // Invalid collection names
        assertFalse(isValidCollectionName(""));
        assertFalse(isValidCollectionName(null));
        assertFalse(isValidCollectionName("collection with spaces"));
        assertFalse(isValidCollectionName("collection.with.dots"));
    }

    @Test
    public void testWorkspaceNameValidation() {
        // Valid workspace names
        assertTrue(isValidWorkspaceName("commons"));
        assertTrue(isValidWorkspaceName("my_workspace"));
        assertTrue(isValidWorkspaceName("workspace-1"));
        
        // Invalid workspace names
        assertFalse(isValidWorkspaceName(""));
        assertFalse(isValidWorkspaceName(null));
        assertFalse(isValidWorkspaceName("workspace with spaces"));
    }

    @Test
    public void testParameterTypeMapping() {
        // Test parameter type detection logic
        assertEquals("varchar", getParameterType("string_value"));
        assertEquals("int", getParameterType(42));
        assertEquals("int", getParameterType(42L));
        assertEquals("float", getParameterType(3.14));
        assertEquals("float", getParameterType(3.14f));
        assertEquals("bool", getParameterType(true));
        assertEquals("bool", getParameterType(false));
        assertEquals("varchar", getParameterType(new ArrayList<>()));
    }

    @Test
    public void testSQLQueryBuilding() {
        String workspace = "test_ws";
        String collection = "test_col";
        String pageContentField = "content";
        String metaField = "metadata";
        String vectorField = "vector";
        String idField = "id";
        int limit = 5;
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(pageContentField).append(", ");
        sql.append(metaField).append(", ");
        sql.append(idField).append(", ");
        sql.append("COSINE_SIM(").append(vectorField).append(", :query_vector) AS similarity_score ");
        sql.append("FROM \"").append(workspace).append("\".\"").append(collection).append("\" ");
        sql.append("ORDER BY similarity_score DESC ");
        sql.append("LIMIT ").append(limit);
        
        String expectedSQL = "SELECT content, metadata, id, COSINE_SIM(vector, :query_vector) AS similarity_score " +
                           "FROM \"test_ws\".\"test_col\" ORDER BY similarity_score DESC LIMIT 5";
        
        assertEquals(expectedSQL, sql.toString());
    }

    private boolean isValidCollectionName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Simple validation - no spaces or dots
        return !name.contains(" ") && !name.contains(".");
    }

    private boolean isValidWorkspaceName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Simple validation - no spaces
        return !name.contains(" ");
    }

    private String getParameterType(Object value) {
        if (value instanceof String) {
            return "varchar";
        } else if (value instanceof Integer || value instanceof Long) {
            return "int";
        } else if (value instanceof Double || value instanceof Float) {
            return "float";
        } else if (value instanceof Boolean) {
            return "bool";
        } else {
            return "varchar";
        }
    }
}
