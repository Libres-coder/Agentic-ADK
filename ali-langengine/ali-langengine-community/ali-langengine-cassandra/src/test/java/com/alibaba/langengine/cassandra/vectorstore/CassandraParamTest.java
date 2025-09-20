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
package com.alibaba.langengine.cassandra.vectorstore;

import com.alibaba.langengine.cassandra.utils.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CassandraParamTest {

    @Test
    public void testDefaultConstructor() {
        CassandraParam param = new CassandraParam();
        
        assertNotNull(param);
        assertNotNull(param.getInitParam());
        assertEquals(Constants.DEFAULT_FIELD_NAME_UNIQUE_ID, param.getFieldNameUniqueId());
        assertEquals(Constants.DEFAULT_FIELD_NAME_PAGE_CONTENT, param.getFieldNamePageContent());
        assertEquals(Constants.DEFAULT_FIELD_NAME_VECTOR, param.getFieldNameVector());
        assertEquals(Constants.DEFAULT_FIELD_META, param.getFieldMeta());
    }

    @Test
    public void testSettersAndGetters() {
        CassandraParam param = new CassandraParam();
        
        param.setFieldNameUniqueId("custom_id");
        param.setFieldNamePageContent("custom_content");
        param.setFieldNameVector("custom_vector");
        param.setFieldMeta("custom_metadata");
        
        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_vector", param.getFieldNameVector());
        assertEquals("custom_metadata", param.getFieldMeta());
    }

    @Test
    public void testInitParamDefaults() {
        CassandraParam.InitParam initParam = new CassandraParam.InitParam();
        
        assertEquals(Constants.DEFAULT_TABLE_NAME, initParam.getTableName());
        assertEquals(Constants.DEFAULT_VECTOR_DIMENSIONS, initParam.getVectorDimensions());
        assertEquals(Constants.DEFAULT_SIMILARITY_FUNCTION, initParam.getVectorSimilarityFunction());
        assertEquals(Constants.DEFAULT_REPLICATION_FACTOR, initParam.getReplicationFactor());
    }

    @Test
    public void testInitParamSettersAndGetters() {
        CassandraParam.InitParam initParam = new CassandraParam.InitParam();
        
        initParam.setTableName("custom_table");
        initParam.setVectorDimensions(1024);
        initParam.setVectorSimilarityFunction(Constants.SIMILARITY_FUNCTION_DOT_PRODUCT);
        initParam.setReplicationFactor(3);
        
        assertEquals("custom_table", initParam.getTableName());
        assertEquals(1024, initParam.getVectorDimensions());
        assertEquals(Constants.SIMILARITY_FUNCTION_DOT_PRODUCT, initParam.getVectorSimilarityFunction());
        assertEquals(3, initParam.getReplicationFactor());
    }

    @Test
    public void testParamWithCustomInitParam() {
        CassandraParam.InitParam initParam = new CassandraParam.InitParam();
        initParam.setTableName("test_table");
        initParam.setVectorDimensions(512);
        initParam.setVectorSimilarityFunction(Constants.SIMILARITY_FUNCTION_EUCLIDEAN);
        initParam.setReplicationFactor(2);
        
        CassandraParam param = new CassandraParam();
        param.setInitParam(initParam);
        
        assertSame(initParam, param.getInitParam());
        assertEquals("test_table", param.getInitParam().getTableName());
        assertEquals(512, param.getInitParam().getVectorDimensions());
        assertEquals(Constants.SIMILARITY_FUNCTION_EUCLIDEAN, param.getInitParam().getVectorSimilarityFunction());
        assertEquals(2, param.getInitParam().getReplicationFactor());
    }

    @Test
    public void testParamEquality() {
        CassandraParam param1 = new CassandraParam();
        param1.setFieldNameUniqueId("test_id");
        param1.setFieldNamePageContent("test_content");
        
        CassandraParam param2 = new CassandraParam();
        param2.setFieldNameUniqueId("test_id");
        param2.setFieldNamePageContent("test_content");
        
        // Note: Lombok @Data should provide equals/hashCode
        assertEquals(param1, param2);
        assertEquals(param1.hashCode(), param2.hashCode());
    }

    @Test
    public void testInitParamEquality() {
        CassandraParam.InitParam initParam1 = new CassandraParam.InitParam();
        initParam1.setTableName("test");
        initParam1.setVectorDimensions(256);
        
        CassandraParam.InitParam initParam2 = new CassandraParam.InitParam();
        initParam2.setTableName("test");
        initParam2.setVectorDimensions(256);
        
        assertEquals(initParam1, initParam2);
        assertEquals(initParam1.hashCode(), initParam2.hashCode());
    }

    @Test
    public void testParamToString() {
        CassandraParam param = new CassandraParam();
        param.setFieldNameUniqueId("test_id");
        
        String toString = param.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("CassandraParam"));
    }

    @Test
    public void testInitParamToString() {
        CassandraParam.InitParam initParam = new CassandraParam.InitParam();
        initParam.setTableName("test_table");
        
        String toString = initParam.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("InitParam"));
    }

    @Test
    public void testValidSimilarityFunctions() {
        CassandraParam.InitParam initParam = new CassandraParam.InitParam();
        
        // Test all valid similarity functions
        initParam.setVectorSimilarityFunction(Constants.SIMILARITY_FUNCTION_COSINE);
        assertEquals(Constants.SIMILARITY_FUNCTION_COSINE, initParam.getVectorSimilarityFunction());
        
        initParam.setVectorSimilarityFunction(Constants.SIMILARITY_FUNCTION_DOT_PRODUCT);
        assertEquals(Constants.SIMILARITY_FUNCTION_DOT_PRODUCT, initParam.getVectorSimilarityFunction());
        
        initParam.setVectorSimilarityFunction(Constants.SIMILARITY_FUNCTION_EUCLIDEAN);
        assertEquals(Constants.SIMILARITY_FUNCTION_EUCLIDEAN, initParam.getVectorSimilarityFunction());
    }

    @Test
    public void testVectorDimensionValidation() {
        CassandraParam.InitParam initParam = new CassandraParam.InitParam();
        
        // Test positive dimensions
        initParam.setVectorDimensions(128);
        assertEquals(128, initParam.getVectorDimensions());
        
        initParam.setVectorDimensions(1536);
        assertEquals(1536, initParam.getVectorDimensions());
        
        // Test edge cases
        initParam.setVectorDimensions(1);
        assertEquals(1, initParam.getVectorDimensions());
    }

    @Test
    public void testReplicationFactorValidation() {
        CassandraParam.InitParam initParam = new CassandraParam.InitParam();
        
        // Test positive replication factors
        initParam.setReplicationFactor(1);
        assertEquals(1, initParam.getReplicationFactor());
        
        initParam.setReplicationFactor(3);
        assertEquals(3, initParam.getReplicationFactor());
        
        initParam.setReplicationFactor(5);
        assertEquals(5, initParam.getReplicationFactor());
    }

    @Test
    public void testFieldNameCustomization() {
        CassandraParam param = new CassandraParam();
        
        // Test that field names can be customized
        param.setFieldNameUniqueId("document_id");
        param.setFieldNamePageContent("text_content");
        param.setFieldNameVector("embedding_vector");
        param.setFieldMeta("doc_metadata");
        
        assertNotEquals(Constants.DEFAULT_FIELD_NAME_UNIQUE_ID, param.getFieldNameUniqueId());
        assertNotEquals(Constants.DEFAULT_FIELD_NAME_PAGE_CONTENT, param.getFieldNamePageContent());
        assertNotEquals(Constants.DEFAULT_FIELD_NAME_VECTOR, param.getFieldNameVector());
        assertNotEquals(Constants.DEFAULT_FIELD_META, param.getFieldMeta());
        
        assertEquals("document_id", param.getFieldNameUniqueId());
        assertEquals("text_content", param.getFieldNamePageContent());
        assertEquals("embedding_vector", param.getFieldNameVector());
        assertEquals("doc_metadata", param.getFieldMeta());
    }
}
