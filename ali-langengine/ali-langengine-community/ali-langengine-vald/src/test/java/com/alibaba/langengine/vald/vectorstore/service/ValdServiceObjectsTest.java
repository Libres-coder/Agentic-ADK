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
package com.alibaba.langengine.vald.vectorstore.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ValdServiceObjectsTest {

    @Test
    @DisplayName("测试ValdInsertRequest对象")
    public void testValdInsertRequest() {
        String id = "test-id";
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        String metadata = "test-metadata";

        ValdInsertRequest request = new ValdInsertRequest(id, vector, metadata);

        assertEquals(id, request.getId());
        assertEquals(vector, request.getVector());
        assertEquals(metadata, request.getMetadata());

        // 测试无参构造函数
        ValdInsertRequest emptyRequest = new ValdInsertRequest();
        assertNull(emptyRequest.getId());
        assertNull(emptyRequest.getVector());
        assertNull(emptyRequest.getMetadata());

        // 测试setter
        emptyRequest.setId("new-id");
        emptyRequest.setVector(Arrays.asList(0.4, 0.5));
        emptyRequest.setMetadata("new-metadata");

        assertEquals("new-id", emptyRequest.getId());
        assertEquals(Arrays.asList(0.4, 0.5), emptyRequest.getVector());
        assertEquals("new-metadata", emptyRequest.getMetadata());
    }

    @Test
    @DisplayName("测试ValdSearchRequest对象")
    public void testValdSearchRequest() {
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        int k = 5;
        Double maxDistance = 0.8;

        ValdSearchRequest request = new ValdSearchRequest(vector, k, maxDistance);

        assertEquals(vector, request.getVector());
        assertEquals(k, request.getK());
        assertEquals(maxDistance, request.getMaxDistance());

        // 测试无参构造函数
        ValdSearchRequest emptyRequest = new ValdSearchRequest();
        assertNull(emptyRequest.getVector());
        assertEquals(0, emptyRequest.getK());
        assertNull(emptyRequest.getMaxDistance());
    }

    @Test
    @DisplayName("测试ValdSearchResponse对象")
    public void testValdSearchResponse() {
        ValdSearchResponse.ValdSearchResult result1 = new ValdSearchResponse.ValdSearchResult(
            "id1", 0.1, "metadata1");
        ValdSearchResponse.ValdSearchResult result2 = new ValdSearchResponse.ValdSearchResult(
            "id2", 0.2, "metadata2");

        List<ValdSearchResponse.ValdSearchResult> results = Arrays.asList(result1, result2);
        ValdSearchResponse response = new ValdSearchResponse(results);

        assertEquals(results, response.getResults());
        assertEquals(2, response.getResults().size());

        // 验证结果内容
        ValdSearchResponse.ValdSearchResult firstResult = response.getResults().get(0);
        assertEquals("id1", firstResult.getId());
        assertEquals(0.1, firstResult.getDistance());
        assertEquals("metadata1", firstResult.getMetadata());
    }

    @Test
    @DisplayName("测试ValdSearchResult对象")
    public void testValdSearchResult() {
        String id = "test-result-id";
        double distance = 0.75;
        String metadata = "result-metadata";

        ValdSearchResponse.ValdSearchResult result = new ValdSearchResponse.ValdSearchResult(
            id, distance, metadata);

        assertEquals(id, result.getId());
        assertEquals(distance, result.getDistance());
        assertEquals(metadata, result.getMetadata());

        // 测试无参构造函数
        ValdSearchResponse.ValdSearchResult emptyResult = new ValdSearchResponse.ValdSearchResult();
        assertNull(emptyResult.getId());
        assertEquals(0.0, emptyResult.getDistance());
        assertNull(emptyResult.getMetadata());
    }

    @Test
    @DisplayName("测试对象相等性")
    public void testObjectEquality() {
        List<Double> vector = Arrays.asList(0.1, 0.2);

        ValdInsertRequest req1 = new ValdInsertRequest("id", vector, "meta");
        ValdInsertRequest req2 = new ValdInsertRequest("id", vector, "meta");

        // 注意：由于使用了Lombok @Data注解，会自动生成equals方法
        // 这里只是验证对象可以正确构造
        assertNotNull(req1);
        assertNotNull(req2);
        assertEquals(req1.getId(), req2.getId());
        assertEquals(req1.getVector(), req2.getVector());
        assertEquals(req1.getMetadata(), req2.getMetadata());
    }

    @Test
    @DisplayName("测试边界条件")
    public void testBoundaryConditions() {
        // 测试空向量
        ValdInsertRequest requestWithEmptyVector = new ValdInsertRequest("id", Arrays.asList(), "meta");
        assertTrue(requestWithEmptyVector.getVector().isEmpty());

        // 测试负距离
        ValdSearchResponse.ValdSearchResult negativeDistanceResult =
            new ValdSearchResponse.ValdSearchResult("id", -0.1, "meta");
        assertEquals(-0.1, negativeDistanceResult.getDistance());

        // 测试k=0的搜索请求
        ValdSearchRequest zeroKRequest = new ValdSearchRequest(Arrays.asList(1.0), 0, null);
        assertEquals(0, zeroKRequest.getK());
    }
}