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
package com.alibaba.langengine.youtube;

import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.youtube.model.YouTubeSearchRequest;
import com.alibaba.langengine.youtube.model.YouTubeSearchResponse;
import com.alibaba.langengine.youtube.sdk.YouTubeClient;
import com.alibaba.langengine.youtube.sdk.YouTubeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * YouTube 搜索工具测试类
 */
class YouTubeSearchToolTest {
    
    @Mock
    private YouTubeClient mockClient;
    
    @Mock
    private ExecutionContext mockExecutionContext;
    
    private YouTubeSearchTool tool;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tool = new YouTubeSearchTool(mockClient);
    }
    
    @Test
    void testToolNameAndDescription() {
        assertEquals("youtube_search", tool.getName());
        assertTrue(tool.getDescription().contains("YouTube"));
        assertTrue(tool.getDescription().contains("videos"));
    }
    
    @Test
    void testParametersSchema() {
        String parameters = tool.getParameters();
        assertNotNull(parameters);
        assertTrue(parameters.contains("query"));
        assertTrue(parameters.contains("count"));
        assertTrue(parameters.contains("sortBy"));
        assertTrue(parameters.contains("timeRange"));
        assertTrue(parameters.contains("videoType"));
        assertTrue(parameters.contains("duration"));
        assertTrue(parameters.contains("quality"));
    }
    
    @Test
    void testRunWithValidInput() throws YouTubeException {
        // 准备测试数据
        String toolInput = "{\"query\": \"machine learning tutorial\", \"count\": 5}";
        YouTubeSearchResponse mockResponse = new YouTubeSearchResponse("machine learning tutorial");
        mockResponse.setReturnedCount(5);
        
        when(mockClient.search(any(YouTubeSearchRequest.class))).thenReturn(mockResponse);
        
        // 执行测试
        var result = tool.run(toolInput, mockExecutionContext);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getResult().contains("YouTube 搜索结果"));
        verify(mockClient).search(any(YouTubeSearchRequest.class));
    }
    
    @Test
    void testRunWithMissingQuery() {
        String toolInput = "{\"count\": 5}";
        
        var result = tool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("错误: 缺少必需的查询参数"));
    }
    
    @Test
    void testRunWithInvalidCount() throws YouTubeException {
        String toolInput = "{\"query\": \"machine learning tutorial\", \"count\": \"invalid\"}";
        YouTubeSearchResponse mockResponse = new YouTubeSearchResponse("machine learning tutorial");
        mockResponse.setReturnedCount(10); // 默认值
        
        when(mockClient.search(any(YouTubeSearchRequest.class))).thenReturn(mockResponse);
        
        var result = tool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("YouTube 搜索结果"));
        verify(mockClient).search(any(YouTubeSearchRequest.class));
    }
    
    @Test
    void testRunWithClientException() throws YouTubeException {
        String toolInput = "{\"query\": \"machine learning tutorial\"}";
        
        when(mockClient.search(any(YouTubeSearchRequest.class)))
                .thenThrow(new YouTubeException("Network error"));
        
        var result = tool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("YouTube 搜索失败"));
    }
    
    @Test
    void testRunWithEmptyResults() throws YouTubeException {
        String toolInput = "{\"query\": \"nonexistent video topic\"}";
        YouTubeSearchResponse mockResponse = new YouTubeSearchResponse("nonexistent video topic");
        mockResponse.setReturnedCount(0);
        
        when(mockClient.search(any(YouTubeSearchRequest.class))).thenReturn(mockResponse);
        
        var result = tool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("未找到相关视频"));
    }
    
    @Test
    void testRunWithAllParameters() throws YouTubeException {
        String toolInput = "{\n" +
                "  \"query\": \"python programming\",\n" +
                "  \"count\": 3,\n" +
                "  \"sortBy\": \"viewCount\",\n" +
                "  \"timeRange\": \"month\",\n" +
                "  \"videoType\": \"video\",\n" +
                "  \"duration\": \"medium\",\n" +
                "  \"quality\": \"hd\",\n" +
                "  \"channel\": \"Tech Channel\",\n" +
                "  \"language\": \"en\",\n" +
                "  \"country\": \"US\",\n" +
                "  \"includeLive\": false,\n" +
                "  \"includeSubtitles\": true,\n" +
                "  \"includeHD\": true\n" +
                "}";
        
        YouTubeSearchResponse mockResponse = new YouTubeSearchResponse("python programming");
        mockResponse.setReturnedCount(3);
        
        when(mockClient.search(any(YouTubeSearchRequest.class))).thenReturn(mockResponse);
        
        var result = tool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("YouTube 搜索结果"));
        verify(mockClient).search(any(YouTubeSearchRequest.class));
    }
    
    @Test
    void testRunWithBooleanParameters() throws YouTubeException {
        String toolInput = "{\n" +
                "  \"query\": \"live stream\",\n" +
                "  \"includeLive\": true,\n" +
                "  \"includeSubtitles\": false,\n" +
                "  \"includeHD\": true\n" +
                "}";
        
        YouTubeSearchResponse mockResponse = new YouTubeSearchResponse("live stream");
        mockResponse.setReturnedCount(5);
        
        when(mockClient.search(any(YouTubeSearchRequest.class))).thenReturn(mockResponse);
        
        var result = tool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("YouTube 搜索结果"));
        verify(mockClient).search(any(YouTubeSearchRequest.class));
    }
    
    @Test
    void testClientGetterAndSetter() {
        YouTubeClient newClient = new YouTubeClient();
        tool.setClient(newClient);
        
        assertEquals(newClient, tool.getClient());
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
    void testIntegrationWithRealClient() throws YouTubeException {
        // 这个测试只在设置了环境变量时运行
        YouTubeClient realClient = new YouTubeClient();
        YouTubeSearchTool realTool = new YouTubeSearchTool(realClient);
        
        String toolInput = "{\"query\": \"machine learning tutorial\", \"count\": 3}";
        
        var result = realTool.run(toolInput, mockExecutionContext);
        
        assertNotNull(result);
        assertTrue(result.getResult().contains("YouTube 搜索结果"));
    }
}
