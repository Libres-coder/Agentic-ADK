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
package com.alibaba.langengine.cassandra.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ConstantsTest {

    @Test
    public void testDefaultTableName() {
        assertEquals("langengine_documents", Constants.DEFAULT_TABLE_NAME);
    }

    @Test
    public void testDefaultKeyspace() {
        assertEquals("langengine", Constants.DEFAULT_KEYSPACE);
    }

    @Test
    public void testDefaultContactPoint() {
        assertEquals("127.0.0.1", Constants.DEFAULT_CONTACT_POINT);
    }

    @Test
    public void testDefaultPort() {
        assertEquals(9042, Constants.DEFAULT_PORT);
    }

    @Test
    public void testDefaultDatacenter() {
        assertEquals("datacenter1", Constants.DEFAULT_DATACENTER);
    }

    @Test
    public void testDefaultVectorDimensions() {
        assertEquals(1536, Constants.DEFAULT_VECTOR_DIMENSIONS);
    }

    @Test
    public void testDefaultSimilarityFunction() {
        assertEquals("cosine", Constants.DEFAULT_SIMILARITY_FUNCTION);
    }

    @Test
    public void testDefaultReplicationFactor() {
        assertEquals(1, Constants.DEFAULT_REPLICATION_FACTOR);
    }

    @Test
    public void testDefaultFieldNames() {
        assertEquals("id", Constants.DEFAULT_FIELD_NAME_UNIQUE_ID);
        assertEquals("content", Constants.DEFAULT_FIELD_NAME_PAGE_CONTENT);
        assertEquals("vector", Constants.DEFAULT_FIELD_NAME_VECTOR);
        assertEquals("metadata", Constants.DEFAULT_FIELD_META);
    }

    @Test
    public void testSimilarityFunctionConstants() {
        assertEquals("cosine", Constants.SIMILARITY_FUNCTION_COSINE);
        assertEquals("dot_product", Constants.SIMILARITY_FUNCTION_DOT_PRODUCT);
        assertEquals("euclidean", Constants.SIMILARITY_FUNCTION_EUCLIDEAN);
    }

    @Test
    public void testConstantsAreNotNull() {
        assertNotNull(Constants.DEFAULT_TABLE_NAME);
        assertNotNull(Constants.DEFAULT_KEYSPACE);
        assertNotNull(Constants.DEFAULT_CONTACT_POINT);
        assertNotNull(Constants.DEFAULT_DATACENTER);
        assertNotNull(Constants.DEFAULT_SIMILARITY_FUNCTION);
        assertNotNull(Constants.DEFAULT_FIELD_NAME_UNIQUE_ID);
        assertNotNull(Constants.DEFAULT_FIELD_NAME_PAGE_CONTENT);
        assertNotNull(Constants.DEFAULT_FIELD_NAME_VECTOR);
        assertNotNull(Constants.DEFAULT_FIELD_META);
        assertNotNull(Constants.SIMILARITY_FUNCTION_COSINE);
        assertNotNull(Constants.SIMILARITY_FUNCTION_DOT_PRODUCT);
        assertNotNull(Constants.SIMILARITY_FUNCTION_EUCLIDEAN);
    }

    @Test
    public void testConstantsAreNotEmpty() {
        assertFalse(Constants.DEFAULT_TABLE_NAME.isEmpty());
        assertFalse(Constants.DEFAULT_KEYSPACE.isEmpty());
        assertFalse(Constants.DEFAULT_CONTACT_POINT.isEmpty());
        assertFalse(Constants.DEFAULT_DATACENTER.isEmpty());
        assertFalse(Constants.DEFAULT_SIMILARITY_FUNCTION.isEmpty());
        assertFalse(Constants.DEFAULT_FIELD_NAME_UNIQUE_ID.isEmpty());
        assertFalse(Constants.DEFAULT_FIELD_NAME_PAGE_CONTENT.isEmpty());
        assertFalse(Constants.DEFAULT_FIELD_NAME_VECTOR.isEmpty());
        assertFalse(Constants.DEFAULT_FIELD_META.isEmpty());
        assertFalse(Constants.SIMILARITY_FUNCTION_COSINE.isEmpty());
        assertFalse(Constants.SIMILARITY_FUNCTION_DOT_PRODUCT.isEmpty());
        assertFalse(Constants.SIMILARITY_FUNCTION_EUCLIDEAN.isEmpty());
    }

    @Test
    public void testNumericConstantsArePositive() {
        assertTrue(Constants.DEFAULT_PORT > 0);
        assertTrue(Constants.DEFAULT_VECTOR_DIMENSIONS > 0);
        assertTrue(Constants.DEFAULT_REPLICATION_FACTOR > 0);
    }

    @Test
    public void testReasonableDefaults() {
        // Test that default values make sense
        assertTrue(Constants.DEFAULT_PORT >= 1024 && Constants.DEFAULT_PORT <= 65535);
        assertTrue(Constants.DEFAULT_VECTOR_DIMENSIONS >= 1);
        assertTrue(Constants.DEFAULT_REPLICATION_FACTOR >= 1);
        assertTrue(Constants.DEFAULT_VECTOR_DIMENSIONS == 1536); // Common embedding size
    }

    @Test
    public void testSimilarityFunctionValues() {
        // Test that similarity function values are lowercase
        assertEquals("cosine", Constants.SIMILARITY_FUNCTION_COSINE);
        assertEquals("dot_product", Constants.SIMILARITY_FUNCTION_DOT_PRODUCT);
        assertEquals("euclidean", Constants.SIMILARITY_FUNCTION_EUCLIDEAN);
        
        // Test that they use underscores for multi-word functions
        assertTrue(Constants.SIMILARITY_FUNCTION_DOT_PRODUCT.contains("_"));
    }

    @Test
    public void testFieldNameConsistency() {
        // Test that field names are consistent and reasonable
        assertFalse(Constants.DEFAULT_FIELD_NAME_UNIQUE_ID.contains(" "));
        assertFalse(Constants.DEFAULT_FIELD_NAME_PAGE_CONTENT.contains(" "));
        assertFalse(Constants.DEFAULT_FIELD_NAME_VECTOR.contains(" "));
        assertFalse(Constants.DEFAULT_FIELD_META.contains(" "));
        
        // Test that field names are lowercase
        assertEquals(Constants.DEFAULT_FIELD_NAME_UNIQUE_ID, Constants.DEFAULT_FIELD_NAME_UNIQUE_ID.toLowerCase());
        assertEquals(Constants.DEFAULT_FIELD_NAME_PAGE_CONTENT, Constants.DEFAULT_FIELD_NAME_PAGE_CONTENT.toLowerCase());
        assertEquals(Constants.DEFAULT_FIELD_NAME_VECTOR, Constants.DEFAULT_FIELD_NAME_VECTOR.toLowerCase());
        assertEquals(Constants.DEFAULT_FIELD_META, Constants.DEFAULT_FIELD_META.toLowerCase());
    }

    @Test
    public void testDefaultsMatchExpectedValues() {
        // Test that defaults match common Cassandra/Vector store conventions
        assertEquals("langengine_documents", Constants.DEFAULT_TABLE_NAME);
        assertEquals("langengine", Constants.DEFAULT_KEYSPACE);
        assertEquals("127.0.0.1", Constants.DEFAULT_CONTACT_POINT);
        assertEquals(9042, Constants.DEFAULT_PORT); // Standard Cassandra port
        assertEquals("datacenter1", Constants.DEFAULT_DATACENTER); // Standard datacenter name
        assertEquals(1536, Constants.DEFAULT_VECTOR_DIMENSIONS); // OpenAI embedding size
        assertEquals("cosine", Constants.DEFAULT_SIMILARITY_FUNCTION); // Most common similarity function
        assertEquals(1, Constants.DEFAULT_REPLICATION_FACTOR); // Safe default for single node
    }
}
