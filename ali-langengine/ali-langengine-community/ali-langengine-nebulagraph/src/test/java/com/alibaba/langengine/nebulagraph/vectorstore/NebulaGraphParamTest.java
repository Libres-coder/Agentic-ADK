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
package com.alibaba.langengine.nebulagraph.vectorstore;

import com.alibaba.langengine.nebulagraph.model.NebulaGraphQueryRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NebulaGraph参数测试类
 *
 * @author langengine
 */
public class NebulaGraphParamTest {

    @Test
    public void testCreateDefault() {
        NebulaGraphParam param = NebulaGraphParam.createDefault();

        assertNotNull(param);
        assertNotNull(param.getInitParam());
        assertEquals("content", param.getFieldNamePageContent());
        assertEquals("unique_id", param.getFieldNameUniqueId());
        assertEquals("metadata", param.getFieldMeta());
        assertEquals("vector", param.getFieldVector());

        assertDoesNotThrow(() -> param.validate());
    }

    @Test
    public void testCreateWithDimension() {
        int testDimension = 512;
        NebulaGraphParam param = NebulaGraphParam.createWithDimension(testDimension);

        assertNotNull(param);
        assertEquals(testDimension, param.getInitParam().getDimension());

        assertDoesNotThrow(() -> param.validate());
    }

    @Test
    public void testCreateWithDimensionAndThreshold() {
        int testDimension = 768;
        double testThreshold = 0.8;
        NebulaGraphParam param = NebulaGraphParam.createWithDimensionAndThreshold(testDimension, testThreshold);

        assertNotNull(param);
        assertEquals(testDimension, param.getInitParam().getDimension());
        assertEquals(testThreshold, param.getInitParam().getSimilarityThreshold());

        assertDoesNotThrow(() -> param.validate());
    }

    @Test
    public void testCreateForTesting() {
        NebulaGraphParam param = NebulaGraphParam.createForTesting();

        assertNotNull(param);
        assertEquals(384, param.getInitParam().getDimension());
        assertEquals(10, param.getInitParam().getBatchSize());
        assertEquals(100, param.getInitParam().getMaxCacheSize());
        assertEquals(5, param.getInitParam().getDefaultTopK());
        assertEquals(50, param.getInitParam().getMaxTopK());
        assertEquals(0.5, param.getInitParam().getSimilarityThreshold());
        assertFalse(param.getInitParam().isEnableQueryCache());
        assertEquals(2, param.getInitParam().getConnectionPoolSize());
        assertEquals(10000, param.getInitParam().getTimeoutMs());

        assertDoesNotThrow(() -> param.validate());
    }

    @Test
    public void testInitParamValidation() {
        NebulaGraphParam param = NebulaGraphParam.createDefault();
        NebulaGraphParam.InitParam initParam = param.getInitParam();

        // 测试有效参数
        assertDoesNotThrow(() -> initParam.validate());

        // 测试无效维度
        initParam.setDimension(-1);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setDimension(1536); // 重置

        // 测试无效相似度阈值
        initParam.setSimilarityThreshold(-0.1);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setSimilarityThreshold(1.1);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setSimilarityThreshold(0.7); // 重置

        // 测试无效批处理大小
        initParam.setBatchSize(0);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setBatchSize(-1);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setBatchSize(100); // 重置

        // 测试无效缓存大小
        initParam.setMaxCacheSize(0);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setMaxCacheSize(-1);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setMaxCacheSize(10000); // 重置

        // 测试无效超时时间
        initParam.setTimeoutMs(0);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setTimeoutMs(-1);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setTimeoutMs(30000); // 重置

        // 测试无效TopK值
        initParam.setDefaultTopK(0);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setDefaultTopK(-1);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setDefaultTopK(10); // 重置

        initParam.setMaxTopK(0);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setMaxTopK(5); // 小于defaultTopK
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        initParam.setMaxTopK(1000); // 重置
    }

    @Test
    public void testFieldNameValidation() {
        NebulaGraphParam param = NebulaGraphParam.createDefault();

        // 测试空字段名
        param.setFieldNamePageContent(null);
        assertThrows(IllegalArgumentException.class, () -> param.validate());
        param.setFieldNamePageContent("");
        assertThrows(IllegalArgumentException.class, () -> param.validate());
        param.setFieldNamePageContent("   ");
        assertThrows(IllegalArgumentException.class, () -> param.validate());
        param.setFieldNamePageContent("content"); // 重置

        param.setFieldNameUniqueId(null);
        assertThrows(IllegalArgumentException.class, () -> param.validate());
        param.setFieldNameUniqueId("unique_id"); // 重置

        param.setFieldMeta(null);
        assertThrows(IllegalArgumentException.class, () -> param.validate());
        param.setFieldMeta("metadata"); // 重置

        param.setFieldVector(null);
        assertThrows(IllegalArgumentException.class, () -> param.validate());
        param.setFieldVector("vector"); // 重置
    }

    @Test
    public void testDistanceFunctionEnum() {
        NebulaGraphParam param = NebulaGraphParam.createDefault();
        NebulaGraphParam.InitParam initParam = param.getInitParam();

        // 测试设置距离函数
        initParam.setDistanceFunctionEnum(NebulaGraphQueryRequest.DistanceFunction.COSINE);
        assertEquals(NebulaGraphQueryRequest.DistanceFunction.COSINE, initParam.getDistanceFunctionEnum());
        assertEquals("cosine", initParam.getDistanceFunction());

        initParam.setDistanceFunctionEnum(NebulaGraphQueryRequest.DistanceFunction.L2);
        assertEquals(NebulaGraphQueryRequest.DistanceFunction.L2, initParam.getDistanceFunctionEnum());
        assertEquals("l2", initParam.getDistanceFunction());

        initParam.setDistanceFunctionEnum(NebulaGraphQueryRequest.DistanceFunction.INNER_PRODUCT);
        assertEquals(NebulaGraphQueryRequest.DistanceFunction.INNER_PRODUCT, initParam.getDistanceFunctionEnum());
        assertEquals("inner_product", initParam.getDistanceFunction());
    }

    @Test
    public void testCopyMethod() {
        NebulaGraphParam original = NebulaGraphParam.createForTesting();

        // 修改一些字段
        original.setFieldTitle("custom_title");
        original.setFieldDocType("custom_doc_type");
        original.getInitParam().setDimension(999);
        original.getInitParam().setSimilarityThreshold(0.9);

        NebulaGraphParam copy = original.copy();

        // 验证复制是否正确
        assertNotSame(original, copy);
        assertNotSame(original.getInitParam(), copy.getInitParam());

        assertEquals(original.getFieldNamePageContent(), copy.getFieldNamePageContent());
        assertEquals(original.getFieldNameUniqueId(), copy.getFieldNameUniqueId());
        assertEquals(original.getFieldMeta(), copy.getFieldMeta());
        assertEquals(original.getFieldVector(), copy.getFieldVector());
        assertEquals(original.getFieldTitle(), copy.getFieldTitle());
        assertEquals(original.getFieldDocType(), copy.getFieldDocType());

        assertEquals(original.getInitParam().getDimension(), copy.getInitParam().getDimension());
        assertEquals(original.getInitParam().getSimilarityThreshold(), copy.getInitParam().getSimilarityThreshold());
        assertEquals(original.getInitParam().getBatchSize(), copy.getInitParam().getBatchSize());
        assertEquals(original.getInitParam().isEnableQueryCache(), copy.getInitParam().isEnableQueryCache());

        // 验证是深拷贝
        original.getInitParam().setDimension(111);
        assertNotEquals(original.getInitParam().getDimension(), copy.getInitParam().getDimension());
    }

    @Test
    public void testCopyWithNullInitParam() {
        NebulaGraphParam original = new NebulaGraphParam();
        original.setInitParam(null);

        NebulaGraphParam copy = original.copy();

        assertNotNull(copy);
        // 由于 NebulaGraphParam 构造函数会初始化 initParam，所以不会是 null
        assertNotNull(copy.getInitParam());
    }

    @Test
    public void testDefaultValues() {
        NebulaGraphParam param = new NebulaGraphParam();

        assertEquals("content", param.getFieldNamePageContent());
        assertEquals("unique_id", param.getFieldNameUniqueId());
        assertEquals("metadata", param.getFieldMeta());
        assertEquals("vector", param.getFieldVector());
        assertEquals("title", param.getFieldTitle());
        assertEquals("doc_index", param.getFieldDocIndex());
        assertEquals("doc_type", param.getFieldDocType());
        assertEquals("tags", param.getFieldTags());
        assertEquals("custom_fields", param.getFieldCustomFields());
        assertEquals("created_at", param.getFieldCreatedAt());
        assertEquals("updated_at", param.getFieldUpdatedAt());

        assertNotNull(param.getInitParam());
    }

    @Test
    public void testInitParamDefaultValues() {
        NebulaGraphParam.InitParam initParam = new NebulaGraphParam.InitParam();

        assertTrue(initParam.getDimension() > 0);
        assertNotNull(initParam.getDistanceFunction());
        assertTrue(initParam.getSimilarityThreshold() >= 0.0 && initParam.getSimilarityThreshold() <= 1.0);
        assertTrue(initParam.getBatchSize() > 0);
        assertTrue(initParam.getMaxCacheSize() > 0);
        assertTrue(initParam.getTimeoutMs() > 0);
        assertTrue(initParam.getDefaultTopK() > 0);
        assertTrue(initParam.getMaxTopK() >= initParam.getDefaultTopK());
        assertTrue(initParam.getConnectionPoolSize() > 0);
        assertTrue(initParam.getRetryCount() >= 0);
        assertTrue(initParam.getRetryIntervalMs() >= 0);
    }

    @Test
    public void testSettersAndGetters() {
        NebulaGraphParam param = new NebulaGraphParam();

        // 测试字段名设置
        param.setFieldNamePageContent("test_content");
        assertEquals("test_content", param.getFieldNamePageContent());

        param.setFieldNameUniqueId("test_id");
        assertEquals("test_id", param.getFieldNameUniqueId());

        param.setFieldMeta("test_meta");
        assertEquals("test_meta", param.getFieldMeta());

        param.setFieldVector("test_vector");
        assertEquals("test_vector", param.getFieldVector());

        // 测试InitParam设置
        NebulaGraphParam.InitParam newInitParam = new NebulaGraphParam.InitParam();
        newInitParam.setDimension(512);
        param.setInitParam(newInitParam);

        assertEquals(newInitParam, param.getInitParam());
        assertEquals(512, param.getInitParam().getDimension());
    }
}