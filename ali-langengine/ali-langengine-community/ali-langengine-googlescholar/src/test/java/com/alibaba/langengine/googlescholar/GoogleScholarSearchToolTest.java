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
package com.alibaba.langengine.googlescholar;

import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.googlescholar.model.GoogleScholarSearchRequest;
import com.alibaba.langengine.googlescholar.model.GoogleScholarSearchResponse;
import com.alibaba.langengine.googlescholar.sdk.GoogleScholarClient;
import com.alibaba.langengine.googlescholar.sdk.GoogleScholarException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Google Scholar 搜索工具测试类
 */
class GoogleScholarSearchToolTest {
    
    @Mock
    private GoogleScholarClient mockClient;
    
    @Mock
    private ExecutionContext mockExecutionContext;
    
    private GoogleScholarSearchTool tool;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tool = new GoogleScholarSearchTool(mockClient);
    }
    
    @Test
    void testToolNameAndDescription() {
        assertEquals("google_scholar_search", tool.getName());
        assertTrue(tool.getDescription().contains("Google Scholar"));
        assertTrue(tool.getDescription().contains("academic papers"));
    }
    
    @Test
    void testParametersSchema() {
        String parameters = tool.getParameters();
        assertNotNull(parameters);
        assertTrue(parameters.contains("query"));
        assertTrue(parameters.contains("count"));
        assertTrue(parameters.contains("sortBy"));
        assertTrue(parameters.contains("timeRange"));
        assertTrue(parameters.contains("documentType"));
    }
    
    @Test
    void testRunWithValidInput() throws GoogleScholarException {
        // 准备测试数据
        String toolInput = "{\"query\": \"machine learning\", \"count\": 5}";
        GoogleScholarSearchResponse mockResponse = new GoogleScholarSearchResponse("machine learning");
        mockResponse.setReturnedCount(5);
        
        when(mockClient.search(any(GoogleScholarSearchRequest.class))).thenReturn(mockResponse);
        
        // 执行测试
        var result = tool.run(toolInput, mockExecutionContext);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getResult().contains("Google Scholar 搜索结果"));
        verify(mockClient).search(any(GoogleScholarSearchRequest.class));
    }
    
    @Test
    void testRunWithMissingQuery() {
        String toolInput = "{\"count\": 5}";
        
        var result = tool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("错误: 缺少必需的查询参数"));
    }
    
    @Test
    void testRunWithInvalidCount() throws GoogleScholarException {
        String toolInput = "{\"query\": \"machine learning\", \"count\": \"invalid\"}";
        GoogleScholarSearchResponse mockResponse = new GoogleScholarSearchResponse("machine learning");
        mockResponse.setReturnedCount(10); // 默认值
        
        when(mockClient.search(any(GoogleScholarSearchRequest.class))).thenReturn(mockResponse);
        
        var result = tool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("Google Scholar 搜索结果"));
        verify(mockClient).search(any(GoogleScholarSearchRequest.class));
    }
    
    @Test
    void testRunWithClientException() throws GoogleScholarException {
        String toolInput = "{\"query\": \"machine learning\"}";
        
        when(mockClient.search(any(GoogleScholarSearchRequest.class)))
                .thenThrow(new GoogleScholarException("Network error"));
        
        var result = tool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("Google Scholar 搜索失败"));
    }
    
    @Test
    void testRunWithEmptyResults() throws GoogleScholarException {
        String toolInput = "{\"query\": \"nonexistent topic\"}";
        GoogleScholarSearchResponse mockResponse = new GoogleScholarSearchResponse("nonexistent topic");
        mockResponse.setReturnedCount(0);
        
        when(mockClient.search(any(GoogleScholarSearchRequest.class))).thenReturn(mockResponse);
        
        var result = tool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("未找到相关论文"));
    }
    
    @Test
    void testRunWithAllParameters() throws GoogleScholarException {
        String toolInput = "{\n" +
                "  \"query\": \"artificial intelligence\",\n" +
                "  \"count\": 3,\n" +
                "  \"sortBy\": \"date\",\n" +
                "  \"timeRange\": \"y\",\n" +
                "  \"documentType\": \"articles\",\n" +
                "  \"author\": \"John Doe\",\n" +
                "  \"journal\": \"Nature\",\n" +
                "  \"language\": \"en\",\n" +
                "  \"country\": \"us\"\n" +
                "}";
        
        GoogleScholarSearchResponse mockResponse = new GoogleScholarSearchResponse("artificial intelligence");
        mockResponse.setReturnedCount(3);
        
        when(mockClient.search(any(GoogleScholarSearchRequest.class))).thenReturn(mockResponse);
        
        var result = tool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("Google Scholar 搜索结果"));
        verify(mockClient).search(any(GoogleScholarSearchRequest.class));
    }
    
    @Test
    void testClientGetterAndSetter() {
        GoogleScholarClient newClient = new GoogleScholarClient();
        tool.setClient(newClient);
        
        assertEquals(newClient, tool.getClient());
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
    void testIntegrationWithRealClient() throws GoogleScholarException {
        // 这个测试只在设置了环境变量时运行
        GoogleScholarClient realClient = new GoogleScholarClient();
        GoogleScholarSearchTool realTool = new GoogleScholarSearchTool(realClient);
        
        String toolInput = "{\"query\": \"machine learning\", \"count\": 3}";
        
        var result = realTool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("Google Scholar 搜索结果"));
    }
}
