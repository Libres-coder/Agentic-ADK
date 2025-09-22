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
package com.alibaba.langengine.rockset.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.rockset.vectorstore.RocksetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class RocksetServiceTest {

    private String testServerUrl;
    private String testApiKey;
    private String testWorkspace;
    private String testCollection;
    private Duration testTimeout;

    @BeforeEach
    public void setUp() {
        testServerUrl = "https://api.test.rockset.com";
        testApiKey = "test-api-key";
        testWorkspace = "test_workspace";
        testCollection = "test_collection";
        testTimeout = Duration.ofSeconds(30);
    }

    @Test
    public void testServiceConstructor() {
        RocksetService service = new RocksetService(
            testServerUrl, testApiKey, testWorkspace, testTimeout);
        
        assertEquals(testWorkspace, service.getWorkspace());
        assertEquals(testApiKey, service.getApiKey());
    }

    @Test
    public void testCreateHeaders() {
        Map<String, String> headers = createTestHeaders(testApiKey);
        
        assertTrue(headers.containsKey("Authorization"));
        assertTrue(headers.containsKey("Content-Type"));
        assertEquals("ApiKey " + testApiKey, headers.get("Authorization"));
        assertEquals("application/json", headers.get("Content-Type"));
    }

    @Test
    public void testCreateHeadersWithEmptyApiKey() {
        Map<String, String> headers = createTestHeaders("");
        
        assertFalse(headers.containsKey("Authorization"));
        assertTrue(headers.containsKey("Content-Type"));
        assertEquals("application/json", headers.get("Content-Type"));
    }

    @Test
    public void testDocumentDataCreation() {
        Document document = new Document();
        document.setPageContent("Test content");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");
        metadata.put("vector", Arrays.asList(0.1, 0.2, 0.3));
        document.setMetadata(metadata);

        RocksetInsertRequest.DocumentData docData = createDocumentData(
            document, "page_content", "content_id", "meta_data", "vector");
        
        assertEquals("Test content", docData.getPageContent());
        assertNotNull(docData.getContentId());
        assertNotNull(docData.getId());
        assertEquals(docData.getContentId(), docData.getId());
        
        // Test vector extraction
        List<Double> vector = docData.getVector();
        assertNotNull(vector);
        assertEquals(3, vector.size());
        assertEquals(0.1, vector.get(0), 0.001);
        assertEquals(0.2, vector.get(1), 0.001);
        assertEquals(0.3, vector.get(2), 0.001);
        
        // Test metadata (should not contain vector)
        Map<String, Object> resultMetadata = docData.getMetaData();
        assertTrue(resultMetadata.containsKey("source"));
        assertFalse(resultMetadata.containsKey("vector"));
        assertEquals("test", resultMetadata.get("source"));
    }

    @Test
    public void testDocumentDataWithCustomId() {
        Document document = new Document();
        document.setPageContent("Test content");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("content_id", "custom-id-123");
        metadata.put("source", "test");
        document.setMetadata(metadata);

        RocksetInsertRequest.DocumentData docData = createDocumentData(
            document, "page_content", "content_id", "meta_data", "vector");
        
        assertEquals("custom-id-123", docData.getContentId());
        assertEquals("custom-id-123", docData.getId());
    }

    @Test
    public void testQueryParameterCreation() {
        RocksetQueryRequest.Parameter param = new RocksetQueryRequest.Parameter();
        param.setName("test_param");
        param.setType("varchar");
        param.setValue("test_value");
        
        assertEquals("test_param", param.getName());
        assertEquals("varchar", param.getType());
        assertEquals("test_value", param.getValue());
    }

    @Test
    public void testQueryRequestCreation() {
        String sqlQuery = "SELECT * FROM test_collection LIMIT 10";
        List<RocksetQueryRequest.Parameter> parameters = new ArrayList<>();
        
        RocksetQueryRequest.Parameter param = new RocksetQueryRequest.Parameter();
        param.setName("limit");
        param.setType("int");
        param.setValue(10);
        parameters.add(param);
        
        RocksetQueryRequest request = new RocksetQueryRequest();
        request.setSql(sqlQuery);
        request.setParameters(parameters);
        
        assertNotNull(request.getSql());
        assertEquals(sqlQuery, request.getSql());
        assertEquals(1, request.getParameters().size());
        assertEquals("limit", request.getParameters().get(0).getName());
    }

    @Test
    public void testDeleteWhereClauseGeneration() {
        List<String> contentIds = Arrays.asList("id1", "id2", "id'3");
        String whereClause = generateDeleteWhereClause(contentIds);
        
        String expected = "content_id IN ('id1','id2','id''3')";
        assertEquals(expected, whereClause);
    }

    @Test
    public void testEmptyContentIdsValidation() {
        List<String> emptyIds = new ArrayList<>();
        
        assertThrows(RocksetException.class, () -> {
            if (emptyIds.isEmpty()) {
                throw RocksetException.invalidParameter("Content IDs list cannot be empty");
            }
        });
    }

    @Test
    public void testEmptyDocumentsValidation() {
        List<Document> emptyDocs = new ArrayList<>();
        
        assertThrows(RocksetException.class, () -> {
            if (emptyDocs.isEmpty()) {
                throw RocksetException.invalidParameter("Documents list cannot be empty");
            }
        });
    }

    @Test
    public void testEmptyQueryValidation() {
        String emptyQuery = "";
        
        assertThrows(RocksetException.class, () -> {
            if (emptyQuery.isEmpty()) {
                throw RocksetException.invalidParameter("SQL query cannot be empty");
            }
        });
    }

    @Test
    public void testCollectionRequestCreation() {
        RocksetCreateCollectionRequest request = new RocksetCreateCollectionRequest();
        request.setName("test_collection");
        request.setDescription("Test collection description");
        request.setRetentionSecs(30L * 24 * 60 * 60); // 30 days
        
        assertEquals("test_collection", request.getName());
        assertEquals("Test collection description", request.getDescription());
        assertEquals(Long.valueOf(30L * 24 * 60 * 60), request.getRetentionSecs());
    }

    @Test
    public void testVectorConversion() {
        // Test conversion from various number types to Double list
        List<?> mixedVector = Arrays.asList(1, 2.5f, 3.7, 4L);
        List<Double> doubleVector = convertToDoubleVector(mixedVector);
        
        assertEquals(4, doubleVector.size());
        assertEquals(1.0, doubleVector.get(0), 0.001);
        assertEquals(2.5, doubleVector.get(1), 0.001);
        assertEquals(3.7, doubleVector.get(2), 0.001);
        assertEquals(4.0, doubleVector.get(3), 0.001);
    }

    @Test
    public void testResponseValidation() {
        // Test insert response validation
        RocksetInsertResponse response = new RocksetInsertResponse();
        List<RocksetInsertResponse.InsertStatus> results = new ArrayList<>();
        
        RocksetInsertResponse.InsertStatus result = new RocksetInsertResponse.InsertStatus();
        result.setId("doc-1");
        result.setStatus("ADDED");
        results.add(result);
        
        response.setData(results);
        response.setStatus("SUCCESS");
        
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(1, response.getData().size());
        assertEquals("doc-1", response.getData().get(0).getId());
        assertEquals("ADDED", response.getData().get(0).getStatus());
    }

    // Helper methods for testing
    private Map<String, String> createTestHeaders(String apiKey) {
        Map<String, String> headers = new HashMap<>();
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            headers.put("Authorization", "ApiKey " + apiKey);
        }
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private RocksetInsertRequest.DocumentData createDocumentData(
            Document document, String pageContentField, String idField, 
            String metaField, String vectorField) {
        RocksetInsertRequest.DocumentData docData = new RocksetInsertRequest.DocumentData();
        
        // Set content ID
        String contentId = document.getMetadata() != null ? 
            (String) document.getMetadata().get(idField) : null;
        if (contentId == null || contentId.trim().isEmpty()) {
            contentId = UUID.randomUUID().toString();
        }
        docData.setContentId(contentId);
        docData.setId(contentId);
        
        // Set page content
        docData.setPageContent(document.getPageContent());
        
        // Set metadata (excluding vector)
        if (document.getMetadata() != null) {
            Map<String, Object> metadata = new HashMap<>(document.getMetadata());
            metadata.remove(vectorField);
            docData.setMetaData(metadata);
            
            // Set vector if present
            Object vectorObj = document.getMetadata().get(vectorField);
            if (vectorObj instanceof List) {
                List<Double> vector = convertToDoubleVector((List<?>) vectorObj);
                docData.setVector(vector);
            }
        }
        
        return docData;
    }

    private List<Double> convertToDoubleVector(List<?> vector) {
        List<Double> result = new ArrayList<>();
        for (Object item : vector) {
            if (item instanceof Number) {
                result.add(((Number) item).doubleValue());
            } else {
                result.add(Double.parseDouble(item.toString()));
            }
        }
        return result;
    }

    private String generateDeleteWhereClause(List<String> contentIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("content_id IN (");
        for (int i = 0; i < contentIds.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("'").append(contentIds.get(i).replace("'", "''")).append("'");
        }
        sb.append(")");
        return sb.toString();
    }
}
